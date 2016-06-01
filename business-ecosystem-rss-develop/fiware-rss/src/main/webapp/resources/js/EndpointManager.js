/**
 * Copyright (C) 2015, CoNWeT Lab., Universidad Politécnica de Madrid
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

(function () {

    var endpoints = {
        'AGGREGATOR_COLLECTION': '/rss/aggregators',
        'PROVIDER_COLLECTION': '/rss/providers',
        'ALGORITHM_COLLECTION': '/rss/algorithms',
        'RSMODEL_COLLECTION': '/rss/models',
        'CDR_COLLECTION': '/rss/cdrs',
        'SETTLEMENT_COLLECTION': '/rss/settlement',
        'REPORTS_COLLECTION': '/rss/settlement/reports'
    };
    
    EndpointManager = function EndpointManager () {
        
    };

    EndpointManager.prototype.getEndpoint = function (endpoint) {
        return CONTEXT_PATH + endpoints[endpoint];
    }
})();


