/**
 *
 */

(function () {

    'use strict';

    angular
        .module('app')
        .factory('User', UserService);

    function UserService($resource, $injector, $location, URLS, PARTY_ROLES) {
        var resource = $resource(URLS.USER, {
            username: '@id'
        }, {
            updatePartial: {
                method: 'PATCH'
            }
        });

        var loggedUser = $injector.has('LOGGED_USER') ? $injector.get('LOGGED_USER') : null;

        return {
            detail: detail,
            updatePartial: updatePartial,
            loggedUser: loggedUser,
            isAuthenticated: isAuthenticated,
            serialize: serialize,
            serializeBasic: serializeBasic
        };

        function detail(next) {
            resource.get({username: loggedUser.id}, next);
        }

        function updatePartial(data, next) {
            resource.updatePartial(data, next);
        }

        function isAuthenticated() {
            return angular.isObject(loggedUser);
        }

        function serialize() {
            var userInfo = serializeBasic();
            userInfo.role = PARTY_ROLES.OWNER;

            return userInfo;
        }

        function serializeBasic() {
            return {
                id: loggedUser.id,
                href: $location.protocol() + '://' + $location.host() + ':' + $location.port() + loggedUser.href,
            };
        }
    }

})();
