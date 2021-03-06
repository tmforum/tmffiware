var nock = require('nock'),
    proxyquire =  require('proxyquire'),
    testUtils = require('../../utils');

describe('RSS API', function() {

    var config = testUtils.getDefaultConfig();
    var SERVER = (config.appSsl ? 'https' : 'http') + '://' + config.appHost + ':' + config.endpoints.rss.port;

    var getRSSAPI = function(rssClient, tmfUtils, utils) {
        return proxyquire('../../../controllers/tmf-apis/rss', {
            './../../config': config,
            './../../lib/logger': testUtils.emptyLogger,
            './../../lib/rss': rssClient,
            './../../lib/utils': utils
        }).rss;
    };

    beforeEach(function() {
        nock.cleanAll();
    });

    describe('Get Permissions', function() {

        //////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////// NOT AUTHENTICATED /////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////

        describe('Not Authenticated Requests', function () {

            var validateLoggedError = function (req, callback) {
                callback({
                    status: 401,
                    message: 'You need to be authenticated to create/update/delete resources'
                });
            };

            var testNotLoggedIn = function (method, done) {

                var utils = {
                    validateLoggedIn: validateLoggedError
                };

                var rssApi = getRSSAPI({}, {}, utils);
                var path = '/rss';

                // Call the method
                var req = {
                    method: method,
                    url: path
                };

                rssApi.checkPermissions(req, function (err) {

                    expect(err).not.toBe(null);
                    expect(err.status).toBe(401);
                    expect(err.message).toBe('You need to be authenticated to create/update/delete resources');

                    done();
                });
            };

            it('should reject not authenticated GET requests', function (done) {
                testNotLoggedIn('GET', done);
            });

            it('should reject not authenticated POST requests', function (done) {
                testNotLoggedIn('POST', done);
            });

            it('should reject not authenticated PUT requests', function (done) {
                testNotLoggedIn('PUT', done);
            });

            it('should reject not authenticated DELETE requests', function (done) {
                testNotLoggedIn('DELETE', done);
            });
        });

        it('should reject not allowed PATCH requests', function(done) {
            var methodNotAllowedStatus = 405;
            var methodNotAllowedMessage = 'This method used is not allowed in the accessed API';

            var utils = {
                methodNotAllowed: function (req, callback) {
                    callback({
                        status: methodNotAllowedStatus,
                        message: methodNotAllowedMessage
                    });
                }
            };

            var rssApi = getRSSAPI({}, {}, utils);
            var path = '/rss';

            // Call the method
            var req = {
                method: 'PATCH',
                url: path
            };

            rssApi.checkPermissions(req, function (err) {

                expect(err).not.toBe(null);
                expect(err.status).toBe(methodNotAllowedStatus);
                expect(err.message).toBe(methodNotAllowedMessage);

                done();
            });
        });
    });

    var mockCreateProvider = function(response) {
        return function(user, callback) {
            // Ensure that the create provider method has been called with the request user
            expect(user.id).toBe('username');
            callback(response);
        }
    };

    var testCheckPermissions = function(req, provResponse, validator, done) {
        var rssClient = {
            rssClient: {
                createProvider: mockCreateProvider(provResponse)
            }
        };

        var utils = {
            log: function() {}
        };
        var rssApi = getRSSAPI(rssClient, {}, utils);

        rssApi.checkPermissions(req, function(err) {
            validator(err);
            done();
        });
    };

    describe('GET', function() {
        //////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////// GET requests ///////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////

        it('should call the callback without errors when the user is authorized to retrieve models', function(done) {
            var req = {
                method: 'GET',
                apiUrl: '/rss/models',
                user: {
                    id: 'username',
                    roles: [{
                        name: 'seller'
                    }]
                }
            };
            var validator = function(err) {
                expect(err).toBe(null);
            };
            testCheckPermissions(req, null, validator, done);
        });

        it('should call the callback with error if the user is trying to access a private API', function(done) {
            var req = {
                method: 'GET',
                apiUrl: '/rss/aggregators',
                user: {}
            };

            var validator = function(err) {
                expect(err).not.toBe(null);
                expect(err.status).toBe(403);
                expect(err.message).toBe('This API is private');
            };
            testCheckPermissions(req, null, validator, done);
        });

        it('should call the callback with error if the user is not a seller', function(done) {
            var req = {
                method: 'GET',
                apiUrl: '/rss/models',
                user: {
                    id: 'username',
                    roles: []
                }
            };
            var validator = function(err) {
                expect(err).not.toBe(null);
                expect(err.status).toBe(403);
                expect(err.message).toBe('You are not authorized to access the RSS API');
            };
            testCheckPermissions(req, null, validator, done);
        });

        it('should call the callback with error if the RSS fails creating the user provider', function(done) {
            var req = {
                method: 'GET',
                apiUrl: '/rss/models',
                user: {
                    id: 'username',
                    roles: [{
                        name: 'seller'
                    }]
                }
            };
            var validator = function(err) {
                expect(err).not.toBe(null);
                expect(err.status).toBe(500);
                expect(err.message).toBe('An unexpected error in the RSS API prevented your request to be processed');
            };
            testCheckPermissions(req, {}, validator, done);
        });
    });

    describe('Create', function() {

        //////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////// POST requests ////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////

        it('should call the callback without error after creating a RS model', function(done) {
            var req = {
                method: 'POST',
                apiUrl: '/rss/models',
                user: {
                    id: 'username',
                    roles: [{
                        name: 'seller'
                    }]
                },
                body: JSON.stringify({aggregatorValue: 0}),
                headers: {}
            };
            var validator = function(err) {
                expect(err).toBe(null);
                var body = JSON.parse(req.body);
                expect(body.aggregatorValue).toBe(config.revenueModel);
            };
            testCheckPermissions(req, null, validator, done);
        });

        it('should call the callback with error if trying to access the CDRs API using POST', function(done) {
            var req = {
                method: 'POST',
                apiUrl: '/rss/cdrs',
                user: {
                    id: 'username',
                        roles: [{
                        name: 'seller'
                    }]
                }
            };
            var validator = function(err) {
                expect(err).not.toBe(null);
                expect(err.status).toBe(403);
                expect(err.message).toBe('This API can only be accessed with GET requests');
            };
            testCheckPermissions(req, null, validator, done);
        });

        it('should call the callback with error if the body is not a valid JSON document', function(done) {
            var req = {
                method: 'POST',
                apiUrl: '/rss/models',
                user: {
                    id: 'username',
                    roles: [{
                        name: 'seller'
                    }]
                },
                body: {}
            };
            var validator = function(err) {
                expect(err).not.toBe(null);
                expect(err.status).toBe(400);
                expect(err.message).toBe('The provided body is not a valid JSON');
            };
            testCheckPermissions(req, null, validator, done);
        });
    });

    describe('Post validation', function() {

        //////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////// Post validation ///////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////

        var testRSSPostValidationNotApply = function(req, done) {
            var rssAPI = getRSSAPI({}, {}, {});

            rssAPI.executePostValidation(req, function(err) {
                expect(err).not.toBe(null);
                done();
            });
        };

        it('should call the callback if the request was not a model retrieving', function(done){
            var req = {
                method: 'POST'
            };
            testRSSPostValidationNotApply(req, done);
        });

        it('should call the callback if the response contained models', function(done) {
            var req = {
                method: 'GET',
                apiUrl: '/rss/models',
                body: JSON.stringify([{}])
            };
            testRSSPostValidationNotApply(req, done);
        });


        it('should call the callback if the server response body is not a valid JSON', function(done){
            var req = {
                method: 'GET',
                apiUrl: '/rss/models',
                body: [{}]
            };
            testRSSPostValidationNotApply(req, done);
        });

        it('should call the callback and create the default RS model if the response contains an empty model list', function(done) {
            var req = {
                method: 'GET',
                apiUrl: '/rss/models',
                body: JSON.stringify([]),
                user: {
                    id: 'username'
                },
                headers: {}
            };

            var newModel = {
                ownerProviderId: 'provider'
            };

            var rssClient = {
                rssClient: {
                    createDefaultModel: function(userInfo, callback) {
                        expect(userInfo.id).toBe('username');
                        callback(null, {
                            body: JSON.stringify(newModel)
                        });
                    }
                }
            };

            var rssAPI = getRSSAPI(rssClient, {}, {});

            rssAPI.executePostValidation(req, function(err) {
                expect(err).toBe(undefined);

                var body = JSON.parse(req.body);
                expect(body).toEqual([newModel]);
                done();
            });

        });

        it('should call the callback with error if the server fails creating the default RS model', function(done) {
            var errorStatus = 500;
            var errorMessage = 'Error creating default RS model'
            var req = {
                method: 'GET',
                apiUrl: '/rss/models',
                body: JSON.stringify([]),
                user: {
                    id: 'username'
                },
                headers: {}
            };

            var rssClient = {
                rssClient: {
                    createDefaultModel: function(userInfo, callback) {
                        expect(userInfo.id).toBe('username');
                        callback({
                            status: errorStatus,
                            message: errorMessage
                        });
                    }
                }
            };

            var rssAPI = getRSSAPI(rssClient, {}, {});

            rssAPI.executePostValidation(req, function(err) {
                expect(err).not.toBe(undefined);
                expect(err.status).toBe(errorStatus);
                expect(err.message).toBe(errorMessage);
                done();
            });
        });
    });
});
