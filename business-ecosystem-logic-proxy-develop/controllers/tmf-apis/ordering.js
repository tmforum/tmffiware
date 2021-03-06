var async = require('async'),
    config = require('./../../config'),
    equal = require('deep-equal'),
    request = require('request'),
    storeClient = require('./../../lib/store').storeClient,
    tmfUtils = require('./../../lib/tmfUtils'),
    url = require('url'),
    utils = require('./../../lib/utils');

var ordering = (function(){

    var CUSTOMER = 'Customer';
    var SELLER = 'Seller';

    var ACKNOWLEDGED = 'Acknowledged';
    var IN_PROGRESS = 'InProgress';
    var COMPLETED = 'Completed';
    var FAILED = 'Failed';
    var PARTIAL = 'Partial';

    //////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////// COMMON ///////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    var makeRequest = function(url, errMsg, callback) {

        request(url, function(err, response, body) {

            if (err || response.statusCode >= 400) {
                callback({
                    status: 400,
                    message: errMsg
                });
            } else {
                callback(null, JSON.parse(body));
            }
        });
    };

    var getBillingAccountUrl = function(billingAccount) {
        var billingAccountPath = url.parse(billingAccount.href).pathname;
        return utils.getAPIURL(config.appSsl, config.appHost, config.endpoints.billing.port, billingAccountPath);
    };


    //////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////// RETRIEVAL //////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    var validateRetrieving = function(req, callback) {
        tmfUtils.filterRelatedPartyFields(req, callback);
    };


    //////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////// CREATION //////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    var completeRelatedPartyInfo = function(individualCollectionUrl, item, user, callback) {

        if (!item.product) {

            callback({
                status: 400,
                message: 'The product order item ' + item.id + ' must contain a product field'
            });

            return;
        }

        if (!item.productOffering) {

            callback({
                status: 400,
                message: 'The product order item ' + item.id + ' must contain a productOffering field'
            });

            return;
        }

        if (!item.product.relatedParty) {
            item.product.relatedParty = [];
        }
        var itemCustCheck = tmfUtils.isOrderingCustomer(user, item.product);

        if (itemCustCheck[0] && !itemCustCheck[1]) {
            callback({
                status: 403,
                message: 'The customer specified in the order item ' + item.id + ' is not the user making the request'
            });
            return;
        }

        if (!itemCustCheck[0]) {
            item.product.relatedParty.push({
                id: user.id,
                role: CUSTOMER,
                href: individualCollectionUrl + user.id
            });
        }

        // Inject customer and seller related parties in the order items in order to make this info
        // available thought the inventory API

        var errorMessageOffer = 'The system fails to retrieve the offering attached to the ordering item ' + item.id;
        var errorMessageProduct = 'The system fails to retrieve the product attached to the ordering item ' + item.id;

        var offeringUrl = utils.getAPIURL(config.appSsl, config.appHost, config.endpoints.catalog.port,
                url.parse(item.productOffering.href).path);

        makeRequest(offeringUrl, errorMessageOffer, function(err, offering) {

            if (err) {
                callback(err);
            } else {

                var productUrl = utils.getAPIURL(config.appSsl, config.appHost, config.endpoints.catalog.port,
                        url.parse(offering.productSpecification.href).path);

                makeRequest(productUrl, errorMessageProduct, function(err, product) {

                    if (err) {
                        callback(err);
                    } else {

                        var owners = product.relatedParty.filter(function (relatedParty) {
                            return relatedParty['role'].toLowerCase() === 'owner';
                        });

                        if (!owners.length) {
                            callback({
                                status: 400,
                                message: 'You cannot order a product without owners'
                            });

                        } else {
                            owners.forEach(function (owner) {
                                item.product.relatedParty.push({
                                    id: owner.id,
                                    role: SELLER,
                                    href: individualCollectionUrl + owner.id
                                });
                            });

                            callback(null, item);
                        }
                    }
                });

            }
        });
    };

    var validateCreation = function(req, callback) {
        var body;

        // The request body may not be well formed
        try {
            body = JSON.parse(req.body);
        } catch (e) {

            callback({
                status: 400,
                message: 'The resource is not a valid JSON document'
            });

            return; // EXIT
        }

        // Check that the related party field has been included
        if (!body.relatedParty) {

            callback({
                status: 400,
                message: 'A product order must contain a relatedParty field'
            });

            return;
        }

        // Check that the user has the customer role
        if (config.customerRoleRequired && !utils.hasRole(req.user, config.oauth2.roles.customer)) {

            callback({
                status: 403,
                message: 'You are not authorized to order products'
            });

            return; // EXIT
        }

        // Check that the user is the specified customer
        var customerCheck = tmfUtils.isOrderingCustomer(req.user, body);
        if (!customerCheck[0]) {
            callback({
                status: 403,
                message: 'It is required to specify a customer in the relatedParty field'
            });

            return; // EXIT
        }

        if (!customerCheck[1]) {
            callback({
                status: 403,
                message: 'The customer specified in the product order is not the user making the request'
            });

            return; // EXIT
        }

        if (!body.orderItem || !body.orderItem.length) {
            callback({
                status: 400,
                message: 'A product order must contain an orderItem field'
            });

            return;
        }

        var asyncTasks = [];
        var individualCollectionUrl = tmfUtils.getIndividualURL(req);

        body.orderItem.forEach(function(item) {
            asyncTasks.push(completeRelatedPartyInfo.bind(this, individualCollectionUrl, item, req.user));
        });

        async.series(asyncTasks, function(err/*, results*/) {

            if (err) {
                callback(err);

            } else {

                // Include sellers as related party in the ordering

                var pushedSellers = [];

                body.orderItem.forEach(function(item) {

                    var sellers = item.product.relatedParty.filter(function(party) {
                        return party.role.toLowerCase() === SELLER.toLowerCase();
                    });

                    sellers.forEach(function(seller) {

                        if (pushedSellers.indexOf(seller.id) < 0) {
                            body.relatedParty.push(seller);
                            pushedSellers.push(seller.id);
                        }

                    });
                });

                utils.updateBody(req, body);

                checkBillingAccounts(req, body, callback);

            }
        });
    };

    var checkBillingAccounts = function(req, ordering, callback) {

        // PLEASE NOTE: Billing account cannot be updated till the ordering has been created

        // Check that all the billing accounts for all the items are the same
        var initialBillingAccount;

        if (ordering.orderItem[0].billingAccount && ordering.orderItem[0].billingAccount.length) {
            initialBillingAccount = ordering.orderItem[0].billingAccount[0]
        }

        if (!initialBillingAccount || !initialBillingAccount.href) {
            return callback({
                status: 422,
                message: 'Billing Account is required'
            });
        }

        var error = false;

        for (var i = 1; i < ordering.orderItem.length && !error; i++) {
            error = !ordering.orderItem[i].billingAccount || !ordering.orderItem[i].billingAccount.length ||
                !equal(initialBillingAccount, ordering.orderItem[i].billingAccount[0]);
        }

        if (error) {
            return callback({
                status: 422,
                message: 'Billing Accounts must be the same for all the order items contained in the ordering'
            });
        }

        // Verify that the billing account exists and that the user is the owner of that billing account
        var billingAccountUrl = getBillingAccountUrl(initialBillingAccount);

        request(billingAccountUrl, function(err, response, body) {

            if (!err && response.statusCode === 200) {

                var billingAccount = JSON.parse(body);

                if (tmfUtils.hasPartyRole(req, billingAccount.relatedParty, config.billingAccountOwnerRole)) {
                    callback(null);
                } else {
                    callback({
                        status: 403,
                        message: 'Unauthorized to use non-owned billing accounts'
                    });
                }

            } else {

                if (response && response.statusCode === 404) {
                    callback({
                        status: 422,
                        message: 'The given billing account does not exist'
                    });
                } else {
                    callback({
                        status: 500,
                        message: 'There was an unexpected error at the time of retrieving the provided billing account'
                    });
                }
            }
        });
    };


    //////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////// UPDATE ///////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    var updateItemsState = function(req, updatedOrdering, previousOrdering, includeOtherFields, callback) {

        var error = null;

        if (previousOrdering.state.toLowerCase() !== 'inprogress') {
            error = {
                status: 403,
                message: previousOrdering.state + ' orders cannot be manually modified'
            };
        }

        for (var i = 0; i < updatedOrdering.orderItem.length && !error; i++) {

            var updatedItem = updatedOrdering.orderItem[i];
            var previousOrderItem = previousOrdering.orderItem.filter(function (item) {
                // id is supposed to be unique
                return item.id === updatedItem.id;
            })[0];

            if (!previousOrderItem) {

                error = {
                    status: 400,
                    message: 'You are trying to edit an non-existing item'
                };

            } else {

                // Check that fields are not added or removed
                if (Object.keys(updatedItem).length !== Object.keys(previousOrderItem).length) {

                    error = {
                        status: 403,
                        message: 'The fields of an order item cannot be modified'
                    };

                } else {

                    for (var field in previousOrderItem) {

                        if (field.toLowerCase() !== 'state' && !equal(previousOrderItem[field], updatedItem[field])) {

                            error = {
                                status: 403,
                                message: 'The value of the field ' + field + ' cannot be changed'
                            };

                            break;
                        }
                    }

                    if (!error) {

                        var isSeller = tmfUtils.hasPartyRole(req, previousOrderItem.product.relatedParty, SELLER);

                        // If the user is not the seller and the state is changed
                        if (!isSeller && previousOrderItem['state'] != updatedItem['state']) {
                            error = {
                                status: 403,
                                message: 'You cannot modify an order item if you are not seller'
                            };
                        } else {
                            // If no errors, the state can be updated!
                            previousOrderItem['state'] = updatedItem['state'];
                        }
                    }
                }
            }
        }

        if (!error) {

            // Sellers can only modify the 'orderItem' field...
            // State is automatically calculated
            var finalBody = includeOtherFields ? updatedOrdering : {};
            finalBody['orderItem'] = previousOrdering.orderItem;

            utils.updateBody(req, finalBody);

            callback(null);

        } else {
            callback(error);
        }
    };

    var validateUpdate = function(req, callback) {

        try {

            var ordering = JSON.parse(req.body);
            var orderingUrl = utils.getAPIURL(config.appSsl, config.appHost, config.endpoints.ordering.port, req.apiUrl);

            makeRequest(orderingUrl, 'The requested ordering cannot be retrieved', function(err, previousOrdering) {
                if (err) {
                    callback(err);
                } else {

                    var isCustomer = tmfUtils.hasPartyRole(req, previousOrdering.relatedParty, CUSTOMER);
                    var isSeller = tmfUtils.hasPartyRole(req, previousOrdering.relatedParty, SELLER);

                    if (isCustomer) {

                        if ('relatedParty' in ordering) {
                            callback({
                                status: 403,
                                message: 'Related parties cannot be modified'
                            });
                        } else if ('orderItem' in ordering) {

                            if (isSeller) {
                                // Customers can be sellers at the same time
                                updateItemsState(req, ordering, previousOrdering, true, callback);
                            } else {
                                // Customers cannot modify the status of the order items
                                callback({
                                    status: 403,
                                    message: 'Order items can only be modified by sellers'
                                });
                            }
                        } else if ('state' in ordering && ordering['state'].toLowerCase() === 'cancelled') {

                            // Orderings can only be cancelled when all items are marked as Acknowledged
                            var productsInAckState = previousOrdering.orderItem.filter(function(item) {
                                return 'acknowledged' === item.state.toLowerCase();
                            });

                            if (productsInAckState.length != previousOrdering.orderItem.length) {
                                callback({
                                    status: 403,
                                    message: 'Orderings can only be cancelled when all Order items are in Acknowledged state'
                                });
                            } else {

                                // Otherwise, the charges has to be refunded to the user.
                                // If the sales cannot be refunded, the callback will be called with
                                // the error parameter so the pre validation will fail and the state
                                // won't be changed.
                                storeClient.refund(previousOrdering.id, req.user, function(err) {

                                    if (err) {
                                        callback(err);
                                    } else {
                                        // Cancel all order items
                                        previousOrdering.orderItem.forEach(function(item) {
                                            item.state = 'Cancelled';
                                        });

                                        // Included order items will be ignored
                                        ordering.orderItem = previousOrdering.orderItem;
                                        utils.updateBody(req, ordering);

                                        callback();
                                    }
                                });
                            }

                        } else {
                            callback(null);
                        }

                    } else if (isSeller) {

                        if (Object.keys(ordering).length == 1 && 'orderItem' in ordering) {
                            updateItemsState(req, ordering, previousOrdering, false, callback);
                        } else {
                            callback({
                                status: 403,
                                message: 'Sellers can only modify order items'
                            });
                        }

                    } else {
                        callback({
                            status: 403,
                            message: 'You are not authorized to modify this ordering'
                        });
                    }
                }
            });

        } catch (e) {

            callback({
                status: 400,
                message: 'The resource is not a valid JSON document'
            });

        }
    };


    //////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// PRE-VALIDATION ///////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    var validators = {
        'GET': [ utils.validateLoggedIn, tmfUtils.ensureRelatedPartyIncluded, validateRetrieving ],
        'POST': [ utils.validateLoggedIn, validateCreation ],
        'PATCH': [ utils.validateLoggedIn, validateUpdate ],
        'PUT': [ utils.methodNotAllowed ],
        'DELETE': [ utils.methodNotAllowed ]
    };

    var checkPermissions = function (req, callback) {

        var reqValidators = [];

        for (var i in validators[req.method]) {
            reqValidators.push(validators[req.method][i].bind(this, req));
        }

        async.series(reqValidators, callback);
    };


    //////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// POST-VALIDATION //////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    var filterOrderItems = function(req, callback) {

        var body = JSON.parse(req.body);

        var orderings = [];
        var isArray = true;

        if (!Array.isArray(body)) {
            orderings = [body];
            isArray = false;
        } else {
            orderings = body;
        }

        // This array is needed as the length of the array cannot be modified while it's being iterated
        var orderingsToRemove = [];
        orderings.forEach(function(ordering) {

            var customer = tmfUtils.hasPartyRole(req, ordering.relatedParty, CUSTOMER);
            var seller = tmfUtils.hasPartyRole(req, ordering.relatedParty, SELLER);

            if (!customer && !seller) {
                // This can happen when a user ask for a specific ordering.
                orderingsToRemove.push(ordering);
            } else if (!customer && seller) {

                // When a user is involved only as a seller in an ordering, only the order items
                // where the user is a seller have to be returned

                ordering.orderItem = ordering.orderItem.filter(function(item) {
                    return tmfUtils.hasPartyRole(req, item.product.relatedParty, SELLER);
                });
            }
            // ELSE: If the user is the customer, order items don't have to be filtered

        });

        orderings = orderings.filter(function(ordering) {
            return orderingsToRemove.indexOf(ordering) < 0;
        });

        if (!isArray) {

            if (orderings.length === 0) {
                callback({
                    status: 403,
                    message: 'You are not authorized to retrieve the specified ordering'
                });
            } else {
                utils.updateBody(req, orderings[0]);
                callback(null);
            }

        } else {
            utils.updateBody(req, orderings);
            callback(null);
        }
    };

    var includeSellersInBillingAccount = function(req, callback) {

        // PLEASE NOTE: Billing Accounts have been checked in the checkPermissions step.

        var ordering = JSON.parse(req.body);

        var billingAccountUrl = getBillingAccountUrl(ordering.orderItem[0].billingAccount[0]);

        request(billingAccountUrl, function(err, response, rawBillingAccount) {

            if (!err && response.statusCode === 200) {

                var billingAccount = JSON.parse(rawBillingAccount);
                var billingAccountRelatedParties = billingAccount.relatedParty;
                var currentUsers = [];

                billingAccountRelatedParties.forEach(function(party) {
                   currentUsers.push(party.id);
                });

                //var modified = false;

                ordering.relatedParty.forEach(function(party) {

                    if (currentUsers.indexOf(party.id) < 0) {

                        billingAccountRelatedParties.push({
                            id: party.id,
                            href: party.href,
                            role: 'bill responsible'
                        });

                        //modified = true;
                    }
                });

                // if (modified) {

                request(billingAccountUrl, {
                    method: 'PATCH',
                    json: { relatedParty: billingAccountRelatedParties }
                }, function(err, response) {

                    if (err || response.statusCode >= 400) {
                        callback({
                            status: 500,
                            message: 'Unexpected error when updating the given billing account'
                        })
                    } else {
                        callback(null);
                    }

                });

                // } else {
                //     callback(null);
                // }

            } else {
                callback({
                    status: 500,
                    message: 'Unexpected error when checking the given billing account'
                });
            }

        });

    };

    var notifyOrder = function(req, callback) {

        var body = JSON.parse(req.body);

        // Send ordering notification to the store
        storeClient.notifyOrder(body, req.user, function(err, res) {

            if (res) {

                var parsedResp = JSON.parse(res.body);

                if (parsedResp.redirectUrl) {
                    req.headers['X-Redirect-URL'] = parsedResp.redirectUrl;
                }

                callback(null);

            } else {
                callback(err);
            }
        });
    };

    var executePostValidation = function(req, callback) {

        if (['GET', 'PUT', 'PATCH'].indexOf(req.method.toUpperCase()) >= 0) {

            filterOrderItems(req, callback);

        } else if (req.method === 'POST') {

            var tasks = [];
            tasks.push(notifyOrder.bind(this, req));
            tasks.push(includeSellersInBillingAccount.bind(this, req));

            async.series(tasks, callback);

        } else {
            callback(null);
        }
    };


    //////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////// COMMON ///////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    return {
        checkPermissions: checkPermissions,
        executePostValidation: executePostValidation
    };

})();

exports.ordering = ordering;