package es.upm.fiware.rss.service;

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

import es.upm.fiware.rss.dao.DbeAggregatorDao;
import es.upm.fiware.rss.dao.DbeAppProviderDao;
import es.upm.fiware.rss.exception.InterfaceExceptionType;
import es.upm.fiware.rss.exception.RSSException;
import es.upm.fiware.rss.exception.UNICAExceptionType;
import es.upm.fiware.rss.model.DbeAggregator;
import es.upm.fiware.rss.model.DbeAppProvider;
import es.upm.fiware.rss.model.DbeAppProviderId;
import es.upm.fiware.rss.model.RSSProvider;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author francisco
 */

public class ProviderManagerTest {

    private final Logger logger = LoggerFactory.getLogger(ProviderManagerTest.class);

    @Mock private DbeAppProviderDao appProviderDao;
    @Mock private DbeAggregatorDao aggregatorDao;
    @Mock private RSSModelsManager modelsManager;
    @InjectMocks private ProviderManager toTest;

    private RSSProvider providerInfo;
    private DbeAggregator aggregator;

    public ProviderManagerTest() {}

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        this.providerInfo = new RSSProvider();
        this.providerInfo.setProviderId("providerId");
        this.providerInfo.setProviderName("providerName");
        this.providerInfo.setAggregatorId("aggregator@mail.com");

        this.aggregator = new DbeAggregator("aggregatorName", this.providerInfo.getAggregatorId());
        when(aggregatorDao.getById(this.providerInfo.getAggregatorId())).thenReturn(aggregator);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void createProviderCorrect () throws RSSException {

        toTest.createProvider(this.providerInfo);

        // Validate calls
        ArgumentCaptor<DbeAppProvider> captor = ArgumentCaptor.forClass(DbeAppProvider.class);
        verify(this.appProviderDao).create(captor.capture());
        
        DbeAppProvider createdProvider = captor.getValue();
        
        Assert.assertEquals(this.providerInfo.getProviderId(), createdProvider.getId().getTxAppProviderId());
        Assert.assertEquals(this.providerInfo.getAggregatorId(), createdProvider.getId().getAggregator().getTxEmail());
        Assert.assertEquals(this.providerInfo.getProviderName(), createdProvider.getTxName());
        Assert.assertEquals(0, createdProvider.getTxCorrelationNumber().intValue());
    }

    private void testExceptionProvider(InterfaceExceptionType exceptionType,
            String errMsg) {
        try {
            this.toTest.createProvider(this.providerInfo);
        } catch (RSSException e) {
            Assert.assertEquals(exceptionType, e.getExceptionType());
            Assert.assertEquals(errMsg, e.getMessage());
        }
    }

    @Test
    public void throwRSSExceptionNullProviderId () throws RSSException {
        this.providerInfo.setProviderId(null);
        this.testExceptionProvider(UNICAExceptionType.MISSING_MANDATORY_PARAMETER,
                "Missing mandatory parameter: ProviderID field is required for creating a provider");
    }

    @Test
    public void  throwRSSExceptionEmptyProviderId () throws RSSException {
        this.providerInfo.setProviderId("");
        this.testExceptionProvider(UNICAExceptionType.MISSING_MANDATORY_PARAMETER,
                "Missing mandatory parameter: ProviderID field is required for creating a provider");
    }

    @Test
    public void throwRSSExceptionNullProviderName() throws RSSException {
        this.providerInfo.setProviderName(null);
        this.testExceptionProvider(UNICAExceptionType.MISSING_MANDATORY_PARAMETER,
                "Missing mandatory parameter: ProviderName field is required for creating a provider");
    }

    @Test
    public void throwRSSExceptionEmptyProviderName() throws RSSException {
        this.providerInfo.setProviderName("");
        this.testExceptionProvider(UNICAExceptionType.MISSING_MANDATORY_PARAMETER,
                "Missing mandatory parameter: ProviderName field is required for creating a provider");
    }

    @Test
    public void throwRSSExceptionNullAggregatorId() throws RSSException {
        this.providerInfo.setAggregatorId(null);
        this.testExceptionProvider(UNICAExceptionType.MISSING_MANDATORY_PARAMETER,
                "Missing mandatory parameter: AggregatorID field is required for creating a provider");
    }

    @Test
    public void throwRSSExceptionEmptyAggregatorId() throws RSSException {
        this.providerInfo.setAggregatorId("");
        this.testExceptionProvider(UNICAExceptionType.MISSING_MANDATORY_PARAMETER,
                "Missing mandatory parameter: AggregatorID field is required for creating a provider");
    }

    @Test
    public void throwRSSExceptionNonExistingAggregator() throws RSSException {
        when(aggregatorDao.getById(this.providerInfo.getAggregatorId())).thenReturn(null);
        this.testExceptionProvider(UNICAExceptionType.NON_EXISTENT_RESOURCE_ID,
                "Resource " + this.aggregator.getTxEmail() + " does not exist");
    }

    @Test
    public void throwRSSExceptionAlreadyExistingProvider () throws RSSException {
        DbeAppProviderId id = new DbeAppProviderId();
        id.setAggregator(this.aggregator);
        id.setTxAppProviderId(this.providerInfo.getProviderId());

        DbeAppProvider prevProvider = new DbeAppProvider();
        prevProvider.setId(id);

        when(appProviderDao.getProvider(
                this.aggregator.getTxEmail(), this.providerInfo.getProviderId()))
                .thenReturn(prevProvider);

        this.testExceptionProvider(UNICAExceptionType.RESOURCE_ALREADY_EXISTS,
                "Resource already exists: The provider " + this.providerInfo.getProviderId() + " of the aggregator " + this.providerInfo.getAggregatorId() + " already exists");
    }

    private DbeAppProvider buildProvider () {
        DbeAggregator dbeAggregator = new DbeAggregator("aggegatorName",
                this.providerInfo.getAggregatorId());

        DbeAppProviderId dbeAppProviderId = new DbeAppProviderId();
        dbeAppProviderId.setAggregator(dbeAggregator);
        dbeAppProviderId.setTxAppProviderId(this.providerInfo.getProviderId());

        DbeAppProvider provModel = new DbeAppProvider();
        provModel.setId(dbeAppProviderId);
        provModel.setTxCorrelationNumber(0);
        provModel.setTxName(this.providerInfo.getProviderId());
        provModel.setTxTimeStamp(new Date());

        return provModel;
    }

    private List<DbeAppProvider> getProvidersList() {
        List <DbeAppProvider> providers = new LinkedList<>();
        providers.add(this.buildProvider());

        return providers;
    }

    private void validateProvider (RSSProvider provider) {
        Assert.assertEquals(this.providerInfo.getAggregatorId(), provider.getAggregatorId());
        Assert.assertEquals(this.providerInfo.getProviderId(), provider.getProviderId());
        Assert.assertEquals(this.providerInfo.getAggregatorId(), provider.getAggregatorId());
    }

    private void validateProvidersList (List<RSSProvider> result) {
        Assert.assertEquals(1, result.size());
        this.validateProvider(result.get(0));
    }

    @Test
    public void getAPIProvidersFromAggregator () throws RSSException {
        List<DbeAppProvider> providers = this.getProvidersList();

        when(appProviderDao.getProvidersByAggregator(
                this.providerInfo.getAggregatorId())).thenReturn(providers);

        List<RSSProvider> result = toTest.getAPIProviders(this.providerInfo.getAggregatorId());
        this.validateProvidersList(result);
    }

    @Test
    public void getAllAPIProviders () throws RSSException {
        List<DbeAppProvider> providers = this.getProvidersList();

        when(appProviderDao.getAll()).thenReturn(providers);

        List<RSSProvider> result = toTest.getAPIProviders(null);
        this.validateProvidersList(result);
    }

    @Test
    public void getAPIProvidersNoneExisting () throws RSSException {
        List<RSSProvider> result = toTest.getAPIProviders(null);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getProvider() throws RSSException {
        when(appProviderDao.getProvider(this.providerInfo.getAggregatorId(),
                this.providerInfo.getProviderId())).thenReturn(this.buildProvider());

        RSSProvider result = toTest.getProvider(
                this.providerInfo.getAggregatorId(), this.providerInfo.getProviderId());

        this.validateProvider(result);
    }

    @Test
    public void getProviderRSSExceptionNonExistentResourceTest() throws RSSException {
        when(appProviderDao.getProvider(this.providerInfo.getAggregatorId(),
                this.providerInfo.getProviderId())).thenReturn(null);

        try {
            toTest.getProvider(
                this.providerInfo.getAggregatorId(), this.providerInfo.getProviderId());
        } catch (RSSException e) {
            Assert.assertEquals(UNICAExceptionType.NON_EXISTENT_RESOURCE_ID, e.getExceptionType());
            Assert.assertEquals(
                    "Resource " + this.providerInfo.getAggregatorId() + " " + this.providerInfo.getProviderId() + " does not exist",
                    e.getMessage());
        }
    }
}
