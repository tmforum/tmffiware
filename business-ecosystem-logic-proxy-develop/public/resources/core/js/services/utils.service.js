/**
 * @author Aitor Magán <amagan@conwet.com>
 */


(function () {

    'use strict';

    angular
        .module('app')
        .factory('Utils', UtilsService);

    function UtilsService() {

        function getGlassfishErrorHtmlProperty(html, property) {
            var regExp = new RegExp('<p><b>' + property + '</b>(.*?)</p>');
            return regExp.exec(html)[1];    // Exception thrown if not match
        }

        return {
            parseError: parseError
        };

        function parseError(response, defaultMessage) {

            var finalErrorMessage = defaultMessage;

            var data = response['data'];

            if (typeof(data) === 'string') {
                // HTML

                try {
                    var type = getGlassfishErrorHtmlProperty(data, 'type');
                    var message = getGlassfishErrorHtmlProperty(data, 'message');
                    var description = getGlassfishErrorHtmlProperty(data, 'description');

                    finalErrorMessage = type + ' - ' + message + ': ' + description;

                } catch (e) {
                    finalErrorMessage = data;
                }

            } else if (data !== null && typeof(data) === 'object' && 'error' in data) {
                // JSON
                if (typeof(data['error']) === 'object' && 'title' in data['error']) {
                    finalErrorMessage = data['error']['title'];
                } else {
                    finalErrorMessage = data['error'];
                }
            }

            return finalErrorMessage;
        }

    }

})();