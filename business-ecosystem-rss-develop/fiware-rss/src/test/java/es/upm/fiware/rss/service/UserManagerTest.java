/**
 * Revenue Settlement and Sharing System GE
 * Copyright (C) 2015 - 2016 , CoNWeT Lab., Universidad Politécnica de Madrid
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
import es.upm.fiware.rss.dao.UserDao;
import es.upm.fiware.rss.exception.RSSException;
import es.upm.fiware.rss.exception.UNICAExceptionType;
import es.upm.fiware.rss.model.Aggregator;
import es.upm.fiware.rss.model.RSSProvider;
import es.upm.fiware.rss.model.RSUser;
import es.upm.fiware.rss.model.Role;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author jortiz
 */

public class UserManagerTest {

    @Mock private UserDao userDaoMock;
    @Mock private AppProperties appProperties;
    @Mock private AggregatorManager aggregatorManager;
    @Mock private ProviderManager providerManager;
    @InjectMocks private UserManager userManager;

    private RSUser user;
    private final String aggregatorId = "aggregator@mail.com";
    private final String providerId = "providerId";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        
        when(appProperties.getProperty("config.grantedRole")).thenReturn("admin");
        when(appProperties.getProperty("config.sellerRole")).thenReturn("seller");
        when(appProperties.getProperty("config.aggregatorRole")).thenReturn("aggregator");

        this.user = new RSUser();
        this.user.setEmail("user@email.com");
        this.user.setId("username");
    }

    @Test
    public void getCurrentUser() throws RSSException {
        RSUser rSUser = new RSUser();
        when(userDaoMock.getCurrentUser()).thenReturn(rSUser);

        RSUser returned = userManager.getCurrentUser();

        assertEquals(rSUser, returned);
    }

    @Test
    public void getCurrentUserNoneExisting() throws RSSException{
        when(userDaoMock.getCurrentUser()).thenReturn(null);

        try {
            userManager.getCurrentUser();
        } catch (RSSException e) {
            assertEquals(UNICAExceptionType.NON_ALLOWED_OPERATION, e.getExceptionType());
            assertEquals("Your user is not authorized to access the RSS", e.getMessage());
        }
    }

    private void mockUserRoles(String ... roles) {
        Set <Role> rolesSet = new HashSet<>();
        
        for (String role: roles) {
            Role r = new Role();
            r.setId(role);
            r.setName(role);
            rolesSet.add(r);
        }
        user.setRoles(rolesSet);
        
        when(userDaoMock.getCurrentUser()).thenReturn(user);
    }

    @Test
    public void isAdmin() throws RSSException {
        this.mockUserRoles("other", "admin");
        assertTrue(userManager.isAdmin());
    }

    @Test
    public void isNotAdmin() throws RSSException {
        this.mockUserRoles();
        assertFalse(userManager.isAdmin());
    }

    @Test
    public void isAggregator () throws RSSException {
        this.mockUserRoles("aggregator", "other");
        assertTrue(userManager.isAggregator());
    }

    @Test
    public void isNotAggregator () throws RSSException {
        this.mockUserRoles("provider", "other");
        assertFalse(userManager.isAggregator());
    }

    @Test
    public void isSeller () throws RSSException {
        this.mockUserRoles("aggregator", "seller");
        assertTrue(userManager.isSeller());
    }

    @Test
    public void isNotSeller () throws RSSException {
        this.mockUserRoles("provider", "other");
        assertFalse(userManager.isSeller());
    }

    private void testGetAllowedIdsCorrect (
            String effectiveAggregator, String effectiveProvider, String aggregator,
            String provider) throws RSSException {

        Map<String, String> result = this.userManager.getAllowedIds(aggregator, provider, "transactions");
        
        assertEquals(effectiveAggregator, result.get("aggregator"));
        assertEquals(effectiveProvider, result.get("provider"));
    }

    @Test
    public void getAllowedIdsAdminUser() throws Exception {
        this.mockUserRoles("admin");

        this.testGetAllowedIdsCorrect(null, null, null, null);
    }

    @Test
    public void getAllowedIdsDefaultAggregatorAggregatorUser() throws Exception {
        this.mockUserRoles("aggregator");

        Aggregator defaultAggregator = new Aggregator();
        defaultAggregator.setAggregatorId(this.user.getEmail());

        when(aggregatorManager.getDefaultAggregator()).thenReturn(defaultAggregator);

        this.testGetAllowedIdsCorrect(this.user.getEmail(), null, null, null);
    }

    @Test
    public void getAllowedIdsProviderSellerUser() throws Exception {
        this.mockUserRoles("seller");

        RSSProvider provider = new RSSProvider();
        provider.setAggregatorId(this.aggregatorId);
        provider.setProviderId(providerId);
        this.user.setId(providerId);

        when(providerManager.getProvider(this.aggregatorId, this.providerId)).thenReturn(provider);

        this.testGetAllowedIdsCorrect(
                this.aggregatorId, this.providerId, this.aggregatorId, this.providerId);
    }

    @Test
    public void getAllowedIdsDefaultProviderSellerUser() throws Exception {
        this.mockUserRoles("seller");

        RSSProvider provider = new RSSProvider();
        provider.setAggregatorId(this.aggregatorId);
        provider.setProviderId(this.user.getId());

        when(providerManager.getProvider(this.aggregatorId, this.user.getId())).thenReturn(provider);

        this.testGetAllowedIdsCorrect(this.aggregatorId, this.user.getId(), this.aggregatorId, null);
    }

    private void testExceptionGetIds (
            String aggregator, String provider, String msg) throws Exception {
        try {
            this.userManager.getAllowedIds(aggregator, provider, "transactions");
            Assert.fail();
        } catch (RSSException e) {
            Assert.assertEquals(UNICAExceptionType.NON_ALLOWED_OPERATION, e.getExceptionType());
            Assert.assertEquals("Operation is not allowed: " + msg, e.getMessage());
        }
    }

    @Test
    public void throwExceptionNoRoles() throws Exception {
        this.mockUserRoles();
        this.testExceptionGetIds(
                this.aggregatorId, this.providerId,
                "You are not allowed to manage transactions");
    }

    @Test
    public void throwExceptionNoDefaultAggregator() throws Exception {
        this.mockUserRoles("aggregator");
        this.testExceptionGetIds(
                null, null, "There isn't any aggregator registered");
    }

    @Test
    public void throwExceptionAggregatorNotAllowed() throws Exception {
        this.mockUserRoles("aggregator");
        this.testExceptionGetIds(aggregatorId, null,
                "You are not allowed to manage transactions of the specified aggregator");
    }

    @Test
    public void throwExceptionProviderPofileNotExisting() throws Exception {
        this.mockUserRoles("seller");
        this.testExceptionGetIds(
                aggregatorId, null, "You do not have a provider profile, please contact with the administrator");
    }

    @Test
    public void throwExceptionProviderNotAllowed() throws Exception {
        this.mockUserRoles("seller");

        RSSProvider provider = new RSSProvider();
        provider.setAggregatorId(this.aggregatorId);
        provider.setProviderId(this.user.getId());

        when(providerManager.getProvider(this.aggregatorId, this.user.getId())).thenReturn(provider);

        this.testExceptionGetIds(
                this.aggregatorId, this.providerId,
                "You are not allowed to manage transactions of the specified provider");
    }

    private void testGetAllowedIdsSingleProvider(
            String effectiveAggregator, String effectiveProvider, String aggregator,
            String provider) throws RSSException{

        Map<String, String> result = this.userManager.getAllowedIdsSingleProvider(aggregator, provider, "transactions");

        assertEquals(effectiveAggregator, result.get("aggregator"));
        assertEquals(effectiveProvider, result.get("provider"));
    }

    @Test
    public void getAllowedIdsSingleProviderAdminUser () throws RSSException {
        this.mockUserRoles("admin");

        Aggregator defaultAggregator = new Aggregator();
        defaultAggregator.setAggregatorId(this.user.getEmail());

        when(aggregatorManager.getDefaultAggregator()).thenReturn(defaultAggregator);

        RSSProvider provider = new RSSProvider();
        provider.setAggregatorId(this.user.getEmail());
        provider.setProviderId(this.providerId);
        this.user.setId(this.providerId);

        when(providerManager.getProvider(this.user.getEmail(), this.providerId)).thenReturn(provider);

        this.testGetAllowedIdsSingleProvider(this.user.getEmail(), this.providerId, null, null);
    }

    @Test
    public void getAllowedIdsSingleProviderAggregatorUser () throws RSSException {
        this.mockUserRoles("aggregator");

        RSSProvider provider = new RSSProvider();
        provider.setAggregatorId(this.aggregatorId);
        provider.setProviderId(this.user.getId());
        this.user.setEmail(this.aggregatorId);

        when(providerManager.getProvider(this.aggregatorId, this.user.getId())).thenReturn(provider);

        this.testGetAllowedIdsSingleProvider(this.aggregatorId, this.providerId, this.aggregatorId, this.providerId);
    }

    @Test
    public void throwExceptionGetAllowedIdsSingleProviderNoRoles() throws Exception {
        this.mockUserRoles();
        try {
            this.userManager.getAllowedIdsSingleProvider(this.aggregatorId, this.providerId, "transactions");
            Assert.fail();
        } catch (RSSException e) {
            Assert.assertEquals(UNICAExceptionType.NON_ALLOWED_OPERATION, e.getExceptionType());
            Assert.assertEquals("Operation is not allowed: You are not allowed to manage transactions", e.getMessage());
        }
    }
}
