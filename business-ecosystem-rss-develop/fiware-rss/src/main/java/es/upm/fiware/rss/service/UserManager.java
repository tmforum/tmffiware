/**
 * Revenue Settlement and Sharing System GE
 * Copyright (C) 2015 - 2016, CoNWeT Lab., Universidad Politécnica de Madrid
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

package es.upm.fiware.rss.service;

import es.upm.fiware.rss.common.properties.AppProperties;
import java.util.Iterator;

import es.upm.fiware.rss.dao.UserDao;
import es.upm.fiware.rss.exception.RSSException;
import es.upm.fiware.rss.exception.UNICAExceptionType;
import es.upm.fiware.rss.model.Aggregator;
import es.upm.fiware.rss.model.RSSProvider;
import es.upm.fiware.rss.model.RSUser;
import es.upm.fiware.rss.model.Role;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author fdelavega
 */
@Service
@Transactional
public class UserManager {

    @Autowired
    private UserDao userDao;

    @Autowired
    private AggregatorManager aggregatorManager;

    @Autowired
    private ProviderManager providerManager;

    @Autowired
    @Qualifier(value = "oauthProperties")
    private AppProperties oauthProperties;

    /**
     * Returns the current user
     * @return RSUser object containig the info of the current user
     * @throws RSSException, if there is not a user object attached to the session
     */
    public RSUser getCurrentUser() throws RSSException {
        RSUser user = userDao.getCurrentUser();

        if (user == null) {
            throw new RSSException(
                    UNICAExceptionType.NON_ALLOWED_OPERATION,
                    "Your user is not authorized to access the RSS");
        }
        return user;
    }

    /**
     * Checks whether the current user contains a given role
     * @param role
     * @return true if the user contains the given role
     * @throws RSSException, if there is not a user object attached to the session
     */
    private boolean checkRole(String role) throws RSSException{
        boolean found = false;
        RSUser user = this.getCurrentUser();
        Iterator<Role> roles = user.getRoles().iterator();

        while (roles.hasNext() && !found) {
            if (roles.next().getName().equalsIgnoreCase(role)) {
                found = true;
            }
        }
        return found;
    }

    /**
     * Check whether a user contains the role specified in the properties file 
     * as admin of the system
     * @return true, if the role is found
     * @throws RSSException, if there is not a user object attached to the session
     */
    public boolean isAdmin() throws RSSException{
        return this.checkRole(
            oauthProperties.getProperty("config.grantedRole"));
    }

    /**
     * Check whether the current user has the aggregator role, that is, the 
     * user is a store admin
     * @return
     * @throws RSSException 
     */
    public boolean isAggregator() throws RSSException {
        return this.checkRole(
                oauthProperties.getProperty("config.aggregatorRole"));
    }

    /**
     * Check whether the current user has the seller role, that is, the user is
     * able to create RS models in a given aggregator
     * @return
     * @throws RSSException 
     */
    public boolean isSeller() throws RSSException {
        return this.checkRole(
                oauthProperties.getProperty("config.sellerRole"));
    }

    private String getEffectiveAggregator(String aggregatorId, String relatedModel) throws RSSException{
        String effectiveAggregator = aggregatorId;
        if (aggregatorId == null) {
            // Extract the default aggregator if required
            Aggregator defaultAggregator = this.aggregatorManager.getDefaultAggregator();

            if  (defaultAggregator == null) {
                String[] args = {"There isn't any aggregator registered"};
                throw new RSSException(UNICAExceptionType.NON_ALLOWED_OPERATION, args);
            }

            effectiveAggregator = defaultAggregator.getAggregatorId();
        }

        // Validate if the user has permissions to retrieve transactions from 
        // the effective aggregator
        if (!this.isAdmin() && (!this.isAggregator()
                || !this.getCurrentUser().getEmail().equalsIgnoreCase(effectiveAggregator))
                && !this.isSeller()) {

            String[] args = {"You are not allowed to manage " + relatedModel +" of the specified aggregator"};
            throw new RSSException(UNICAExceptionType.NON_ALLOWED_OPERATION, args);
        }

        return effectiveAggregator;
    }

    private String getEffectiveProvider(
            String effectiveAggregator, String providerId, String relatedModel) throws RSSException {

        String effectiveProvider = providerId;
        RSSProvider provider = this.providerManager.getProvider(
                    effectiveAggregator, this.getCurrentUser().getId());

        if (provider == null) {
            String[] args = {"You do not have a provider profile, please contact with the administrator"};
            throw new RSSException(UNICAExceptionType.NON_ALLOWED_OPERATION, args);
        }

        // Get the effective provider
        if (providerId == null) {
            effectiveProvider = provider.getProviderId();
        } else if (!this.isAggregator() && !provider.getProviderId().equals(providerId)) {
            String[] args = {"You are not allowed to manage " + relatedModel +" of the specified provider"};
            throw new RSSException(UNICAExceptionType.NON_ALLOWED_OPERATION, args);
        }

        return effectiveProvider;
    }

    /**
     * Checks the basic permissions of the current user to manage objects
     * identified by an aggregatorId and a providerId. it returns the effective
     * aggregatorId and providerId to be used to access the database, or null
     * if the user has permission to retrieve all the related objects.
     * 
     * If the user has the admin role he wiil be able to retieve all the existing objects
     *
     * If the user has the aggregator role, he will be able to retieve all the
     * objects that are included in the aggregator he owns
     *
     * If the user has the seller role, he will be able to retrieve all its objects
     *
     * If the aggregator id is not provided and the user is not admin, the effective
     * aggregator will be the default one.
     * 
     * If the providerId is not provided and the user is not an admin nor an 
     * aggregator, the effective providerId will be the user id
     * 
     * @param aggregatorId Id of the expect aggregator or null
     * @param providerId Id of the expected provider or null
     * @param relatedModel Identifies the type of object that will be managed
     * @return a Map containing the effective aggregatorId and providerId to be used to access the database
     * identified by the 'provider' and 'aggregator' keys
     * @throws RSSException If the user has not permission to access to the requested aggregator and provider
     */
    public Map<String, String> getAllowedIds (
            String aggregatorId, String providerId, String relatedModel) throws RSSException {

        Map<String, String> result = new HashMap<>();

        if (!this.isAdmin() && !this.isAggregator() && !this.isSeller()) {
            String[] args = {"You are not allowed to manage " + relatedModel};
            throw new RSSException(UNICAExceptionType.NON_ALLOWED_OPERATION, args);
        }

        // Get the effective aggregator
        String effectiveAggregator = aggregatorId;
        String effectiveProvider = providerId;
        if (!this.isAdmin() || providerId != null) {
            effectiveAggregator = this.getEffectiveAggregator(aggregatorId, relatedModel);
        }

        if (!this.isAdmin() && !this.isAggregator()) {
            effectiveProvider = this.getEffectiveProvider(effectiveAggregator, providerId, relatedModel);
        }

        result.put("provider", effectiveProvider);
        result.put("aggregator", effectiveAggregator);

        return result;
    }

    /**
     * Checks the basic permissions of the current user to manage objects
     * identified by an aggregatorId and a providerId. It returns the effective
     * aggregatorId and providerId identifing a concerete provider.
     * 
     * If the provided aggregatorId is null the default one is returned
     * If the provided providerId is null the user one is returned
     * 
     * @param aggregatorId Id of the expected aggregator or null
     * @param providerId Id of the expected provider or null
     * @param relatedModel Identifies the type of object that will be managed
     * @return a Map containing the effective aggregatorId and providerId to be used to access the database
     * identified by the 'provider' and 'aggregator' keys
     * @throws RSSException If the user has not permission to access to the requested aggregator and provider
     */
    public Map<String, String> getAllowedIdsSingleProvider(
            String aggregatorId, String providerId, String relatedModel) throws RSSException {

        Map<String, String> result = new HashMap<>();

        if (!this.isAdmin() && !this.isAggregator() && !this.isSeller()) {
            String[] args = {"You are not allowed to manage " + relatedModel};
            throw new RSSException(UNICAExceptionType.NON_ALLOWED_OPERATION, args);
        }

        String effectiveAggregator = this.getEffectiveAggregator(aggregatorId, relatedModel);
        result.put("aggregator", effectiveAggregator);
        result.put("provider", this.getEffectiveProvider(effectiveAggregator, providerId, relatedModel));
        return result;
    }
}
