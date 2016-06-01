/**
 * Revenue Settlement and Sharing System GE
 * Copyright (C) 2011-2014, Javier Lucio - lucio@tid.es
 * Telefonica Investigacion y Desarrollo, S.A.
 *
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
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import es.upm.fiware.rss.model.RSSModel;
import es.upm.fiware.rss.service.RSSModelsManager;
import es.upm.fiware.rss.service.UserManager;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;

/**
 *
 *
 */
public class RSSModelServiceTest {

    @Mock private RSSModelsManager rssModelsManager;
    @Mock private UserManager userManager;
    @InjectMocks private RSSModelService toTest;

    private RSSModel model;
    private final String aggregator = "aggregator@email.com";
    private final String provider = "providerId";
    private final String productClass = "productClass";

    @Before
    public void setUp() throws RSSException {
        MockitoAnnotations.initMocks(this);

        this.model = new RSSModel();
        Map<String, String> ids = new HashMap<>();
        ids.put("aggregator", this.aggregator);
        ids.put("provider", this.provider);

        when(this.userManager.getAllowedIds(null, null, "RS models")).thenReturn(ids);
        when(this.userManager.getAllowedIdsSingleProvider(null, null, "RS models")).thenReturn(ids);
    }

    @Test
    public void getRSSModels () throws Exception {
        List<RSSModel> rssModels = new LinkedList<>();
        when(rssModelsManager.getRssModels(
                this.aggregator, this.provider, this.productClass)).thenReturn(rssModels);

        Response response = toTest.getRssModels(null, productClass, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals(rssModels, response.getEntity());
    }

    @Test
    public void createRSModel() throws Exception {
        when(rssModelsManager.createRssModel(this.model)).thenReturn(this.model);

        Response response = toTest.createRSSModel(this.model);
        Assert.assertEquals(this.model, response.getEntity());

        Assert.assertEquals(this.aggregator, this.model.getAggregatorId());
        Assert.assertEquals(this.provider, this.model.getOwnerProviderId());
    }

    @Test
    public void updateRSmodel() throws Exception {
        when(rssModelsManager.updateRssModel(this.model)).thenReturn(this.model);

        Response response = toTest.modifyRSSModel(this.model);
        Assert.assertEquals(this.model, response.getEntity());

        Assert.assertEquals(this.aggregator, this.model.getAggregatorId());
        Assert.assertEquals(this.provider, this.model.getOwnerProviderId());
    }

    @Test
    public void deleteRSModel() throws Exception {
        Response response = toTest.deleteRSSModel(null, null, this.productClass);
        Assert.assertEquals(204, response.getStatus());

        verify(this.rssModelsManager).deleteRssModel(this.aggregator, this.provider, this.productClass);
    }
}
