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
package es.upm.fiware.rss.ws;

import es.upm.fiware.rss.exception.RSSException;
import es.upm.fiware.rss.exception.UNICAExceptionType;
import es.upm.fiware.rss.model.Aggregator;
import es.upm.fiware.rss.model.RSSProvider;
import es.upm.fiware.rss.model.RSUser;
import es.upm.fiware.rss.service.AggregatorManager;
import es.upm.fiware.rss.service.ProviderManager;
import es.upm.fiware.rss.service.UserManager;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.core.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author jortiz
 */
public class ProviderServiceTest {

    @Mock private ProviderManager providerManager;
    @Mock private UserManager userManager;
    @Mock private AggregatorManager aggregatorManager;
    @InjectMocks private ProviderService toTest;

    private RSSProvider provider;
    private RSUser user;
    private final String defaultAggregatorId  = "default@email.com";

    @Before
    public void setUp() throws RSSException {
        MockitoAnnotations.initMocks(this);

        this.provider = new RSSProvider();
        this.provider.setAggregatorId("aggregator@mail.com");
        this.provider.setProviderId("providerId");
        this.provider.setProviderName("providerName");

        this.user = new RSUser();
        this.user.setDisplayName("username");
        this.user.setEmail("user@mail.com");

        when(this.userManager.getCurrentUser()).thenReturn(this.user);
    }

    private void mockDefaultAggregator() throws Exception {
        Aggregator defaultAggregator = new Aggregator();
        defaultAggregator.setAggregatorId(this.defaultAggregatorId);

        this.user.setEmail(this.defaultAggregatorId);
        when(this.aggregatorManager.getDefaultAggregator()).thenReturn(defaultAggregator);
    }

    @Test
    public void createProviderAdminUser() throws Exception {
        when(this.userManager.isAdmin()).thenReturn(true);

        Response response = toTest.createProvider(this.provider);

        Assert.assertEquals(201 ,response.getStatus());
        verify(this.providerManager).createProvider(this.provider);
    }

    @Test
    public void createProviderDefaultAggregator() throws Exception {
        when(this.userManager.isAggregator()).thenReturn(true);
        this.provider.setAggregatorId(null);

        this.mockDefaultAggregator();

        Response response = toTest.createProvider(this.provider);

        Assert.assertEquals(201 ,response.getStatus());
        Assert.assertEquals(this.user.getEmail(), this.provider.getAggregatorId());
        verify(this.providerManager).createProvider(this.provider);
    }

    @Test
    public void throwRSSExceptionCreateProviderNotAllowed() throws Exception {
        try {
            toTest.createProvider(provider);
            Assert.fail();
        } catch (RSSException e) {
            Assert.assertEquals(UNICAExceptionType.NON_ALLOWED_OPERATION, e.getExceptionType());
            Assert.assertEquals(
                    "Operation is not allowed: You are not allowed to create a provider for the given aggregatorId",
                    e.getMessage());
        }
    }

    private void testGetProvidersCorrect(
            String queryAggregator, String aggregator) throws Exception {

        List <RSSProvider> providers = new LinkedList<>();
        when(providerManager.getAPIProviders(aggregator)).thenReturn(providers);

        Response response = toTest.getProviders(queryAggregator);

        Assert.assertEquals(200 ,response.getStatus());
        Assert.assertEquals(providers, response.getEntity());
    }

    @Test
    public void getAllProvidersAdmin () throws Exception {
        when(userManager.isAdmin()).thenReturn(true);
        this.testGetProvidersCorrect(null, null);
    }

    @Test
    public void getProvidersDefaultAggregator () throws Exception {
        when(userManager.isAggregator()).thenReturn(true);
        this.mockDefaultAggregator();

        this.testGetProvidersCorrect(null, this.defaultAggregatorId);
    }

    @Test
    public void getProvidersUserAggregator () throws Exception {
        when(userManager.isAggregator()).thenReturn(true);
        this.user.setEmail(this.provider.getAggregatorId());

        this.testGetProvidersCorrect(
                this.provider.getAggregatorId(), this.provider.getAggregatorId());
    }

    private void testGetProvidersException (
            String queryAggregator, String errMsg) throws Exception {

        try {
            this.toTest.getProviders(queryAggregator);
            Assert.fail();
        } catch (RSSException e) {
            Assert.assertEquals(UNICAExceptionType.NON_ALLOWED_OPERATION, e.getExceptionType());
            Assert.assertEquals("Operation is not allowed: " + errMsg, e.getMessage());
        }
    }

    @Test
    public void throwRSSExceptionMissingAggregators () throws Exception {
        this.testGetProvidersException(
                null, "There isn't any aggregator registered");
    }
}
