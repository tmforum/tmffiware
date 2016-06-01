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
package es.upm.fiware.rss.service;

import es.upm.fiware.rss.dao.DbeAggregatorDao;
import es.upm.fiware.rss.exception.InterfaceExceptionType;
import es.upm.fiware.rss.exception.RSSException;
import es.upm.fiware.rss.exception.UNICAExceptionType;
import es.upm.fiware.rss.model.Aggregator;
import es.upm.fiware.rss.model.DbeAggregator;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;

import org.slf4j.Logger;

/**
 *
 * @author jortiz
 */

public class AggregatorManagerTest {

    @Mock private Logger loggerMock;
    @Mock private DbeAggregatorDao dbeAggregatorDaoMock;
    @InjectMocks private AggregatorManager toTest;

    private Aggregator aggregator;

    public AggregatorManagerTest() {
    }

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
        
        this.aggregator = new Aggregator();
        this.aggregator.setAggregatorId("aggregator@id.com");
        this.aggregator.setAggregatorName("aggregatorName");
        this.aggregator.setDefaultAggregator(false);

        DbeAggregator prevAggregator = new DbeAggregator();
        prevAggregator.setTxEmail("prev@mail.com");
        prevAggregator.setTxName("prevAggregator");
        prevAggregator.setDefaultAggregator(false);

        List<DbeAggregator> aggregators = new ArrayList<>();

        aggregators.add(prevAggregator);
        when(this.dbeAggregatorDaoMock.getAll()).thenReturn(aggregators);
    }

    private void testCreateAggregator (boolean isDefault) throws RSSException{
        this.toTest.createAggretator(this.aggregator);
        
        // Validate calls
        ArgumentCaptor<DbeAggregator> captor = ArgumentCaptor.forClass(DbeAggregator.class);
        verify(this.dbeAggregatorDaoMock).create(captor.capture());
        
        DbeAggregator dbeAggregator = captor.getValue();
        
        Assert.assertEquals("aggregator@id.com", dbeAggregator.getTxEmail());
        Assert.assertEquals("aggregatorName", dbeAggregator.getTxName());
        Assert.assertEquals(isDefault, dbeAggregator.isDefaultAggregator());
    }

    @Test
    public void createAggretator() throws RSSException{
        this.testCreateAggregator(false);
    }

    @Test
    public void createFirstAggregator() throws RSSException {
        when(this.dbeAggregatorDaoMock.getAll()).thenReturn(new ArrayList<>());
        this.testCreateAggregator(true);
    }

    @Test
    public void createFirstDefaultAggregator() throws RSSException {
        when(this.dbeAggregatorDaoMock.getAll()).thenReturn(new ArrayList<>());
        this.aggregator.setDefaultAggregator(true);
        
        this.testCreateAggregator(true);
    }

    @Test
    public void createDefaultAggregator() throws RSSException {
        DbeAggregator defaultAggregator = new DbeAggregator();
        defaultAggregator.setDefaultAggregator(true);

        this.aggregator.setDefaultAggregator(true);
        when(this.dbeAggregatorDaoMock.getDefaultAggregator()).thenReturn(defaultAggregator);

        this.testCreateAggregator(true);

        Assert.assertFalse(defaultAggregator.isDefaultAggregator());
    }

    private void testExceptionAggregator(InterfaceExceptionType exceptionType,
            String errMsg) {
        try {
            this.toTest.createAggretator(this.aggregator);
        } catch (RSSException e) {
            Assert.assertEquals(exceptionType, e.getExceptionType());
            Assert.assertEquals(errMsg, e.getMessage());
        }
    }

    @Test
    public void throwExceptionCreateAggretatorNullID() {
        this.aggregator.setAggregatorId(null);
        this.testExceptionAggregator(
                UNICAExceptionType.MISSING_MANDATORY_PARAMETER,
                "Missing mandatory parameter: AggregatorID field is required for creating an aggregator");
    }

    @Test
    public void throwExceptionCreateAggretatorEmptyID() {
        this.aggregator.setAggregatorId("");
        this.testExceptionAggregator(
                UNICAExceptionType.MISSING_MANDATORY_PARAMETER,
                "Missing mandatory parameter: AggregatorID field is required for creating an aggregator");
    }
    
    @Test
    public void throwExceptionCreateAggretatorInvalidEmail() throws RSSException {
        this.aggregator.setAggregatorId("aggregatorID");
        this.testExceptionAggregator(UNICAExceptionType.INVALID_PARAMETER,
                "Invalid parameter: AggregatorID field must be an email identifiying a valid Store owner");
    }

    @Test
    public void throwExceptionCreateAggretatorNullName() {
        this.aggregator.setAggregatorName(null);
        this.testExceptionAggregator(
                UNICAExceptionType.MISSING_MANDATORY_PARAMETER,
                "Missing mandatory parameter: AggregatorName field is required for creating an aggregator");
    }
    
    @Test
    public void throwExceptionCreateAggretatorEmptyName() {
        this.aggregator.setAggregatorName("");
        this.testExceptionAggregator(
                UNICAExceptionType.MISSING_MANDATORY_PARAMETER,
                "Missing mandatory parameter: AggregatorName field is required for creating an aggregator");
    }

    @Test
    public void createAggretatorRSSExceptionAlreadyExistsTest() throws RSSException {
        when(this.dbeAggregatorDaoMock.getById("aggregator@id.com")).thenReturn(new DbeAggregator());
        this.testExceptionAggregator(UNICAExceptionType.RESOURCE_ALREADY_EXISTS,
                "Resource already exists: The Aggregator aggregator@id.com already exists");
    }

    @Test
    public void getAPIAggregators() throws RSSException {
        List <Aggregator> returned = toTest.getAPIAggregators();

        Assert.assertEquals(1, returned.size());
        
        Aggregator r = returned.get(0);
        Assert.assertEquals("prev@mail.com", r.getAggregatorId());
        Assert.assertEquals("prevAggregator", r.getAggregatorName());
        Assert.assertFalse(r.isDefaultAggregator());
    }


    @Test
    public void getAggregatorTest() throws RSSException {
        String aggregatorID = "aggregator@id.com";

        Aggregator aggregator = new Aggregator();
        aggregator.setAggregatorId(aggregatorID);
        aggregator.setAggregatorName("aggregatorName");

        DbeAggregator dbeAggregator = new DbeAggregator("aggregatorName", aggregatorID);

        when(dbeAggregatorDaoMock.getById(aggregatorID)).thenReturn(dbeAggregator);

        Aggregator returned = toTest.getAggregator(aggregatorID);

        Assert.assertEquals(aggregator.getAggregatorId(), returned.getAggregatorId());
        Assert.assertEquals(aggregator.getAggregatorName(), returned.getAggregatorName());
    }

    @Test
    (expected = RSSException.class)
    public void getAggregatorRSSExceptionTest() throws RSSException {
        String aggregatorID = "aggregator@id.com";

        toTest.getAggregator(aggregatorID);
    }

}
