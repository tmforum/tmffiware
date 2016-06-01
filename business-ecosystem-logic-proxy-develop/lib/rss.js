var config = require('./../config'),
    request = require('request'),
    utils = require('./utils');

var rssClient = (function() {

    var createProvider = function(userInfo, callback) {
        var headers = {
            'content-type': 'application/json',
            'X-Nick-Name': 'proxyAdmin',
            'X-Roles': config.oauth2.roles.admin,
            'X-Email': 'proxy@email.com'
        };

        var url = utils.getAPIURL(config.appSsl, config.appHost, config.endpoints.rss.port, config.endpoints.rss.path + '/rss/providers');
        var providerInfo = {
            'providerId': userInfo.id,
            'providerName': userInfo.displayName
        };

        // Make the request
        var options = {
            url: url,
            method: 'POST',
            headers: headers,
            body: JSON.stringify(providerInfo)
        };

        request(options, function(err, response) {
            var resp = (err || (response.statusCode >= 400 && response.statusCode != 409)) ? {} : null;
            callback(resp);
        });
    };

    var makeModelsRequest = function(userInfo, model, query, callback) {
        var headers = {
            'content-type': 'application/json',
            'Accept': 'application/json'
        };

        utils.attachUserHeaders(headers, userInfo);

        var url = utils.getAPIURL(config.appSsl, config.appHost, config.endpoints.rss.port, config.endpoints.rss.path + '/rss/models' + query);

        var options = {
            url: url,
            method: 'GET',
            headers: headers
        };

        if (model) {
            options.method = 'POST';
            options.body = JSON.stringify(model);
        }

        request(options, function(err, response, body) {
            if (err || response.statusCode >= 400) {
                var status = response ? response.statusCode : 504;
                var message = 'An unexpected error prevented your default RS model to be created';

                if (response && [400, 401, 403, 404].indexOf(response.statusCode) >= 0) {
                    var parsedResp = JSON.parse(body);
                    message = parsedResp['exceptionText'];
                }

                callback({
                    status: status,
                    message: message
                });
            } else {
                callback(null, {
                    status: response.statusCode,
                    headers: response.headers,
                    body: body
                });
            }
        });
    };

    var createDefaultModel = function(userInfo, callback) {
        var defaultModel = {
            'aggregatorValue': config.revenueModel,
            'ownerValue': (100 - config.revenueModel),
            'algorithmType': 'FIXED_PERCENTAGE',
            'productClass': 'defaultRevenue'
        };

        makeModelsRequest(userInfo, defaultModel, '', callback);
    };

    var retrieveRSModel = function(userInfo, productClass, callback) {
        makeModelsRequest(userInfo, null, '?productClass=' + productClass, callback);
    };

    return {
        createProvider: createProvider,
        createDefaultModel: createDefaultModel,
        retrieveRSModel: retrieveRSModel
    };
})();

exports.rssClient = rssClient;
