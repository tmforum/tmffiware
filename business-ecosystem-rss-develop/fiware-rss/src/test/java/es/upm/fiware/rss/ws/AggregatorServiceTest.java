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
import es.upm.fiware.rss.model.RSUser;
import es.upm.fiware.rss.service.AggregatorManager;
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
import org.mockito.MockitoAnnotations;

/**
 *
 * @author jortiz
 */
public class AggregatorServiceTest {

    private @Mock UserManager userManager;
    private @Mock AggregatorManager aggregatorManager;
    private @InjectMocks AggregatorService toTest;
    
    private Aggregator aggregator;

    public AggregatorServiceTest() {}

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.aggregator = new Aggregator();
        this.aggregator.setAggregatorId("aggregator@mail.com");
        this.aggregator.setAggregatorName("aggregatorName");
    }

    @Test
    public void createAggregator() throws Exception {
        when(userManager.isAdmin()).thenReturn(true);

        Response response = toTest.createAggregator(this.aggregator);

        Assert.assertEquals(201 ,response.getStatus());
    }

    @Test
    public void createAggregatorNotAdminTest() throws Exception {
        when(userManager.isAdmin()).thenReturn(false);
        
        try {
            toTest.createAggregator(aggregator);
        } catch (RSSException e) {
            Assert.assertEquals(UNICAExceptionType.NON_ALLOWED_OPERATION, e.getExceptionType());
            Assert.assertEquals("Operation is not allowed: You are not allowed to create aggregators", e.getMessage());
        }
    }

    private void testGetAggregators(List<Aggregator> aggregators) throws Exception {
        Response response = toTest.getAggregators();

        Assert.assertEquals(200 ,response.getStatus());
        Assert.assertEquals(aggregators, response.getEntity());
    }

    @Test
    public void getAggregatorsAdmin() throws Exception {
        List <Aggregator> aggregators = new LinkedList<>();
        aggregators.add(aggregator);
        when(aggregatorManager.getAPIAggregators()).thenReturn(aggregators);

        when(userManager.isAdmin()).thenReturn(true);
        this.testGetAggregators(aggregators);
    }

    public void getAggregatorsAggregator() throws Exception {
        List <Aggregator> aggregators = new LinkedList<>();
        aggregators.add(aggregator);

        RSUser user = new RSUser();
        user.setEmail("user@mail.com");
        when(userManager.getCurrentUser()).thenReturn(user);
        when(aggregatorManager.getAggregator("user@mail.com")).thenReturn(aggregator);

        when(userManager.isAggregator()).thenReturn(true);
        this.testGetAggregators(aggregators);
    }

    @Test
    public void getAggregatorsNotAllowed() throws Exception {
        try {
            Response response = toTest.getAggregators();
        } catch (RSSException e) {
            Assert.assertEquals(UNICAExceptionType.NON_ALLOWED_OPERATION, e.getExceptionType());
            Assert.assertEquals("Operation is not allowed: You are not allowed to retrieve aggregators", e.getMessage());
        }
    }

}
