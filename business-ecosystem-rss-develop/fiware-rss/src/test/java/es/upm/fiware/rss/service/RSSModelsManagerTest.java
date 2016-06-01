/**
 * Revenue Settlement and Sharing System GE
 * Copyright (C) 2011-2014, Javier Lucio - lucio@tid.es
 * Telefonica Investigacion y Desarrollo, S.A.
 *
 * Copyright (C) 2015 - 2016, CoNWeT Lab., Universidad Politénica de Madrid
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

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import es.upm.fiware.rss.dao.DbeAggregatorDao;
import es.upm.fiware.rss.dao.DbeAppProviderDao;
import es.upm.fiware.rss.dao.ModelProviderDao;
import es.upm.fiware.rss.dao.SetRevenueShareConfDao;
import es.upm.fiware.rss.exception.InterfaceExceptionType;
import es.upm.fiware.rss.exception.RSSException;
import es.upm.fiware.rss.exception.UNICAExceptionType;
import es.upm.fiware.rss.model.DbeAggregator;
import es.upm.fiware.rss.model.DbeAppProvider;
import es.upm.fiware.rss.model.DbeAppProviderId;
import es.upm.fiware.rss.model.ModelProvider;
import es.upm.fiware.rss.model.ModelProviderId;
import es.upm.fiware.rss.model.RSSModel;
import es.upm.fiware.rss.model.SetRevenueShareConf;
import es.upm.fiware.rss.model.SetRevenueShareConfId;
import es.upm.fiware.rss.model.StakeholderModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

public class RSSModelsManagerTest {
    /***
     * Logging system.
     */
    private final Logger logger = LoggerFactory.getLogger(RSSModelsManagerTest.class);

    @Mock private DbeAppProviderDao appProviderDao;
    @Mock private SetRevenueShareConfDao revenueShareConfDao;
    @Mock private DbeAggregatorDao aggregatorDao;
    @Mock private ModelProviderDao modelProviderDao;
    @InjectMocks private RSSModelsManager toTest;

    private RSSModel rssModel;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        String aggregatorId = "aggregator@mail.com";
        String algorithmType = "FIXED_PERCENTAGE";
        String ownerProviderId = "provider@mail.com";
        String productClass = "productClass@mail.com";
        String stakeholderId = "stakeholder@mail.com";

        List <StakeholderModel> holdersModel = new LinkedList<>();
        StakeholderModel stakeholderModel = new StakeholderModel();
        stakeholderModel.setModelValue(BigDecimal.valueOf(20));
        stakeholderModel.setStakeholderId(stakeholderId);
        holdersModel.add(stakeholderModel);

        rssModel = new RSSModel();
        rssModel.setAggregatorId(aggregatorId);
        rssModel.setAggregatorShare(BigDecimal.valueOf(50));
        rssModel.setAlgorithmType(algorithmType);
        rssModel.setOwnerProviderId(ownerProviderId);
        rssModel.setOwnerValue(BigDecimal.valueOf(30));
        rssModel.setProductClass(productClass);
        rssModel.setStakeholders(holdersModel);
    }

    private DbeAppProvider mockGetProvider(String aggregatorId, String providerId) {
        DbeAggregator dbeAggregator = new DbeAggregator("aggegatorName", aggregatorId);

        DbeAppProviderId dbeAppProviderId = new DbeAppProviderId();
        dbeAppProviderId.setAggregator(dbeAggregator);
        dbeAppProviderId.setTxAppProviderId(providerId);

        DbeAppProvider provModel = new DbeAppProvider();
        provModel.setId(dbeAppProviderId);
        provModel.setModels(null);
        provModel.setTxCorrelationNumber(Integer.MIN_VALUE);
        provModel.setTxName(providerId);
        provModel.setTxTimeStamp(new Date());

        when(appProviderDao.getProvider(aggregatorId, providerId)).thenReturn(provModel);
        return provModel;
    }
    
    @Test
    public void appProviderValidated () throws RSSException {
        String aggregatorId = "aggregator@mail.com";
        String providerId = "provider@mail.com";

        this.mockGetProvider(aggregatorId, providerId);

        toTest.checkValidAppProvider(aggregatorId, providerId);
    }
    
    @Test
    public void throwsRSSExceptionProviderNotExists() {
        String aggregatorId = "aggregator@mail.com";
        this.mockGetProvider(aggregatorId, "provider@mail.com");

        try {
            toTest.checkValidAppProvider(aggregatorId, "provider1@mail.com");
            Assert.fail();
        } catch (RSSException e) {
            Assert.assertEquals(UNICAExceptionType.NON_EXISTENT_RESOURCE_ID, e.getExceptionType());
            Assert.assertEquals("Resource provider does not exist", e.getMessage());
        }
    }

    @Test
    public void rssModelValidated() throws RSSException {
        this.mockGetProvider(rssModel.getAggregatorId(), rssModel.getOwnerProviderId());

        // Mock getProvider method for stakeholders
        when(appProviderDao.getProvider(
                rssModel.getAggregatorId(),
                rssModel.getStakeholders().get(0).getStakeholderId()))
                .thenReturn(new DbeAppProvider());

        toTest.checkValidRSSModel(rssModel);
    }

    private void testCheckRSSModelException(InterfaceExceptionType exceptionType, String message) {
        try {
            toTest.checkValidRSSModel(rssModel);
            Assert.fail();
        } catch (RSSException e) {
            Assert.assertEquals(exceptionType, e.getExceptionType());
            Assert.assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void throwsRSSExceptionMissingAggregator() {
        rssModel.setAggregatorId(null);
        testCheckRSSModelException(UNICAExceptionType.MISSING_MANDATORY_PARAMETER, "Missing mandatory parameter: aggregatorId");
    }

    @Test
    public void throwsRSSExceptionMissingOwner() {
        rssModel.setOwnerProviderId(null);
        testCheckRSSModelException(UNICAExceptionType.MISSING_MANDATORY_PARAMETER, "Missing mandatory parameter: ownerProviderId");
    }

    @Test
    public void throwsRSSExceptionMissingAlgorithm() {
        rssModel.setAlgorithmType(null);
        testCheckRSSModelException(UNICAExceptionType.MISSING_MANDATORY_PARAMETER, "Missing mandatory parameter: algorithmType");
    }

    @Test
    public void throwsRSSExceptionMissingProductClass() {
        rssModel.setAlgorithmType(null);
        testCheckRSSModelException(UNICAExceptionType.MISSING_MANDATORY_PARAMETER, "Missing mandatory parameter: algorithmType");
    }

    @Test
    public void throwsRSSExceptionMissingAggregatorValue() {
        rssModel.setAggregatorShare(null);
        testCheckRSSModelException(UNICAExceptionType.MISSING_MANDATORY_PARAMETER, "Missing mandatory parameter: aggregatorValue");
    }

    @Test
    public void throwsRSSExceptionMissingOwnerValue() {
        rssModel.setOwnerValue(null);
        testCheckRSSModelException(UNICAExceptionType.MISSING_MANDATORY_PARAMETER, "Missing mandatory parameter: ownerValue");
    }

    @Test
    public void throwsRSSExceptionMissingStakeholderId() {
        this.mockGetProvider(rssModel.getAggregatorId(), rssModel.getOwnerProviderId());
        rssModel.getStakeholders().get(0).setStakeholderId(null);
        testCheckRSSModelException(UNICAExceptionType.MISSING_MANDATORY_PARAMETER, "Missing mandatory parameter: stakeholderId");
    }

    @Test
    public void throwsRSSExceptionMissingStakeholderValue() {
        this.mockGetProvider(rssModel.getAggregatorId(), rssModel.getOwnerProviderId());
        rssModel.getStakeholders().get(0).setModelValue(null);
        testCheckRSSModelException(UNICAExceptionType.MISSING_MANDATORY_PARAMETER, "Missing mandatory parameter: modelValue");
    }

    @Test
    public void throwsRSSExceptionOwnerIncludedAsStakeholder() {
        this.mockGetProvider(rssModel.getAggregatorId(), rssModel.getOwnerProviderId());
        when(appProviderDao.getProvider(
                rssModel.getAggregatorId(),
                rssModel.getStakeholders().get(0).getStakeholderId()))
                .thenReturn(new DbeAppProvider());

        StakeholderModel stakeholderModel = new StakeholderModel();
        stakeholderModel.setModelValue(BigDecimal.valueOf(20));
        stakeholderModel.setStakeholderId(rssModel.getOwnerProviderId());
        rssModel.getStakeholders().add(stakeholderModel);

        testCheckRSSModelException(UNICAExceptionType.INVALID_PARAMETER, "Invalid parameter: The RS model owner cannot be included as stakeholder");
    }

    private SetRevenueShareConf buildDatabaseRSModel() {

        DbeAggregator dbeAggregator = new DbeAggregator("Aggregator", rssModel.getAggregatorId());
        DbeAppProviderId dbeAppProviderId = new DbeAppProviderId();
        DbeAppProvider dbeAppProvider = new DbeAppProvider();
        SetRevenueShareConfId setRevenueShareConfId = new SetRevenueShareConfId();

        Set <ModelProvider> stakeholders = new HashSet<>();
        SetRevenueShareConf setRevenueShareConf = new SetRevenueShareConf();

        ModelProviderId modelProviderId = new ModelProviderId();
        DbeAppProvider stProvider = new DbeAppProvider();
        stProvider.setTxName("Provider");
        modelProviderId.setStakeholder(stProvider);

        ModelProvider modelProvider = new ModelProvider();

        dbeAppProviderId.setTxAppProviderId(rssModel.getOwnerProviderId());

        dbeAppProvider.setId(dbeAppProviderId);

        setRevenueShareConfId.setModelOwner(dbeAppProvider);
        setRevenueShareConfId.setProductClass(rssModel.getProductClass());

        stakeholders.add(modelProvider);

        setRevenueShareConf.setAggregator(dbeAggregator);
        setRevenueShareConf.setAggregatorValue(rssModel.getAggregatorValue());
        setRevenueShareConf.setAlgorithmType(rssModel.getAlgorithmType());
        setRevenueShareConf.setId(setRevenueShareConfId);
        setRevenueShareConf.setOwnerValue(rssModel.getOwnerValue());
        setRevenueShareConf.setStakeholders(stakeholders);

        modelProvider.setId(modelProviderId);
        modelProvider.setModel(setRevenueShareConf);
        modelProvider.setModelValue(rssModel.getStakeholders().get(0).getModelValue());

        DbeAppProviderId stakeholderId = new DbeAppProviderId();
        stakeholderId.setAggregator(dbeAggregator);
        stakeholderId.setTxAppProviderId(rssModel.getStakeholders().get(0).getStakeholderId());

        DbeAppProvider stakeholder = new DbeAppProvider();
        stakeholder.setId(stakeholderId);
        modelProvider.setStakeholder(stakeholder);

        return setRevenueShareConf;
    }
    
    @Test
    public void convertIntoApiModelTest() {
        RSSModel model = toTest.convertIntoApiModel(this.buildDatabaseRSModel());

        // Validate Model
        Assert.assertEquals(rssModel.getAggregatorId(), model.getAggregatorId());
        Assert.assertEquals(rssModel.getAggregatorValue(), model.getAggregatorValue());
        Assert.assertEquals(rssModel.getAlgorithmType(), model.getAlgorithmType());
        Assert.assertEquals(rssModel.getOwnerProviderId(), model.getOwnerProviderId());
        Assert.assertEquals(rssModel.getOwnerValue(), model.getOwnerValue());
        Assert.assertEquals(rssModel.getProductClass(), model.getProductClass());

        // Validate stakeholders
        Assert.assertEquals(rssModel.getStakeholders().size(), model.getStakeholders().size());
        for (int i = 0; i < rssModel.getStakeholders().size(); i++) {
            Assert.assertEquals(rssModel.getStakeholders().get(i).getStakeholderId(),
                    model.getStakeholders().get(i).getStakeholderId());

            Assert.assertEquals(rssModel.getStakeholders().get(i).getModelValue(),
                    model.getStakeholders().get(i).getModelValue());
        }
    }

    @Test
    public void revenueSharingModelCorrectlyCreated() throws RSSException {
        this.mockGetProvider(rssModel.getAggregatorId(), rssModel.getOwnerProviderId());

        DbeAppProvider dbStakeholder = new DbeAppProvider();
        when(appProviderDao.getProvider(
                rssModel.getAggregatorId(),
                rssModel.getStakeholders().get(0).getStakeholderId()))
                .thenReturn(dbStakeholder);

        DbeAggregator dBAggregator = new DbeAggregator();
        when(this.aggregatorDao.getById(rssModel.getAggregatorId())).thenReturn(dBAggregator);

        RSSModel model = toTest.createRssModel(rssModel);

        Assert.assertEquals(rssModel, model);

        // Verify calls
        ArgumentCaptor<SetRevenueShareConf> captor = ArgumentCaptor.forClass(SetRevenueShareConf.class);
        verify(this.revenueShareConfDao).create(captor.capture());

        SetRevenueShareConf dBModel = captor.getValue();
        Assert.assertEquals(dBAggregator, dBModel.getAggregator());
        Assert.assertEquals(rssModel.getAggregatorValue(), dBModel.getAggregatorValue());
        Assert.assertEquals(rssModel.getAlgorithmType(), dBModel.getAlgorithmType());
        Assert.assertEquals(rssModel.getOwnerProviderId(), dBModel.getId().getModelOwner().getId().getTxAppProviderId());
        Assert.assertEquals(rssModel.getOwnerValue(), dBModel.getOwnerValue());
        Assert.assertEquals(rssModel.getProductClass(), dBModel.getId().getProductClass());

        ArgumentCaptor<ModelProvider> stCaptor = ArgumentCaptor.forClass(ModelProvider.class);

        verify(this.modelProviderDao).create(stCaptor.capture());
        List<ModelProvider> stakeholders = stCaptor.getAllValues();

        for (int i = 0; i < stakeholders.size(); i++) {
            Assert.assertEquals(dbStakeholder, stakeholders.get(i).getStakeholder());

            Assert.assertEquals(rssModel.getStakeholders().get(i).getModelValue(),
                    stakeholders.get(i).getModelValue());
        }
    }

    @Test
    public void throwsRSSExceptionAlreadyExistingModel() {
        this.mockGetProvider(rssModel.getAggregatorId(), rssModel.getOwnerProviderId());
        when(appProviderDao.getProvider(
                rssModel.getAggregatorId(),
                rssModel.getStakeholders().get(0).getStakeholderId()))
                .thenReturn(new DbeAppProvider());

        List<SetRevenueShareConf> models = new ArrayList<>();
        models.add(this.buildDatabaseRSModel());

        when(this.revenueShareConfDao.getRevenueModelsByParameters(
                rssModel.getAggregatorId(), rssModel.getOwnerProviderId(),
                rssModel.getProductClass())).thenReturn(models);

        try {
            this.toTest.createRssModel(rssModel);
            Assert.fail();
        } catch (RSSException e) {
            Assert.assertEquals(UNICAExceptionType.RESOURCE_ALREADY_EXISTS, e.getExceptionType());
            Assert.assertEquals("Resource already exists: A model with the same Product Class already exists", e.getMessage());
        }
    }

    @Test
    public void revenueSharingModelDeleted() throws Exception {
        this.mockGetProvider(rssModel.getAggregatorId(), rssModel.getOwnerProviderId());
        DbeAppProvider dbStakeholder = new DbeAppProvider();
        when(appProviderDao.getProvider(
                rssModel.getAggregatorId(),
                rssModel.getStakeholders().get(0).getStakeholderId()))
                .thenReturn(dbStakeholder);

        List<SetRevenueShareConf> models = new ArrayList<>();
        SetRevenueShareConf dbModel = this.buildDatabaseRSModel();
        models.add(dbModel);

        when(this.revenueShareConfDao.getRevenueModelsByParameters(
                rssModel.getAggregatorId(), rssModel.getOwnerProviderId(),
                rssModel.getProductClass())).thenReturn(models);

        toTest.deleteRssModel(rssModel.getAggregatorId(), rssModel.getOwnerProviderId(), rssModel.getProductClass());

        ArgumentCaptor<SetRevenueShareConf> captor = ArgumentCaptor.forClass(SetRevenueShareConf.class);
        verify(this.revenueShareConfDao).delete(captor.capture());

        Assert.assertEquals(dbModel, captor.getValue());

        ArgumentCaptor<ModelProvider> stCaptor = ArgumentCaptor.forClass(ModelProvider.class);

        verify(this.modelProviderDao).delete(stCaptor.capture());
        List<ModelProvider> stakeholders = stCaptor.getAllValues();
        Assert.assertEquals(dbModel.getStakeholders().size(), stakeholders.size());

        stakeholders.stream().forEach((stakeholder) -> {
            Assert.assertTrue(dbModel.getStakeholders().contains(stakeholder));
        });
    }

    @Test
    public void emptyListRSModelsToBeRemoved() throws Exception {
        this.mockGetProvider(rssModel.getAggregatorId(), rssModel.getOwnerProviderId());
        when(appProviderDao.getProvider(
                rssModel.getAggregatorId(),
                rssModel.getStakeholders().get(0).getStakeholderId()))
                .thenReturn(new DbeAppProvider());

        toTest.deleteRssModel(rssModel.getAggregatorId(), rssModel.getOwnerProviderId(), rssModel.getProductClass());

        verify(this.revenueShareConfDao, never()).delete(Matchers.isA(SetRevenueShareConf.class));
    }

    @Test
    public void throwsRSSExceptionNullAggregator() throws Exception {
        try {
            this.toTest.deleteRssModel(null, null, null);
            Assert.fail();
        } catch (RSSException e) {
            Assert.assertEquals(UNICAExceptionType.MISSING_MANDATORY_PARAMETER, e.getExceptionType());
            Assert.assertEquals("Missing mandatory parameter: aggregatorId", e.getMessage());
        }
    }

    @Test
    public void throwsRSSExceptionNotExistingProvider() throws Exception {
        try {
            this.toTest.deleteRssModel(
                    rssModel.getAggregatorId(), rssModel.getOwnerProviderId(), rssModel.getProductClass());
            Assert.fail();
        } catch (RSSException e) {
            Assert.assertEquals(UNICAExceptionType.NON_EXISTENT_RESOURCE_ID, e.getExceptionType());
            Assert.assertEquals("Resource provider does not exist", e.getMessage());
        }
    }

    @Test
    public void rsModelExist() {
        List <SetRevenueShareConf> revenueShareConfs = mock(LinkedList.class);
        when(revenueShareConfs.isEmpty()).thenReturn(false);

        when(revenueShareConfDao.getRevenueModelsByParameters(rssModel.getAggregatorId(),
                rssModel.getOwnerProviderId(), rssModel.getProductClass())).thenReturn(revenueShareConfs);

        boolean returned = toTest.existModel(rssModel.getAggregatorId(),
                rssModel.getOwnerProviderId(), rssModel.getProductClass());

        Assert.assertTrue(returned);
    }

    @Test
    public void rsModelNotExistEmpty() {

        List <SetRevenueShareConf> revenueShareConfs = new ArrayList<>();

        when(revenueShareConfDao.getRevenueModelsByParameters(rssModel.getAggregatorId(),
                rssModel.getOwnerProviderId(), rssModel.getProductClass())).thenReturn(revenueShareConfs);

        boolean returned = toTest.existModel(rssModel.getAggregatorId(),
                rssModel.getOwnerProviderId(), rssModel.getProductClass());

        Assert.assertFalse(returned);
    }

    @Test
    public void rsModelNotExistNull() {
        boolean returned = toTest.existModel(rssModel.getAggregatorId(),
                rssModel.getOwnerProviderId(), rssModel.getProductClass());

        Assert.assertFalse(returned);
    }

    @Test
    public void productClassModelRetrieved() throws RSSException {
        this.mockGetProvider(rssModel.getAggregatorId(), rssModel.getOwnerProviderId());

        List<SetRevenueShareConf> models = new ArrayList<>();
        models.add(this.buildDatabaseRSModel());

        when(revenueShareConfDao.getRevenueModelsByParameters(rssModel.getAggregatorId(),
                rssModel.getOwnerProviderId(), rssModel.getProductClass())).thenReturn(models);

        List<RSSModel> result = toTest.getRssModels(rssModel.getAggregatorId(),
                rssModel.getOwnerProviderId(), rssModel.getProductClass());

        Assert.assertEquals(1, result.size());
        RSSModel model = result.get(0);

        Assert.assertEquals(rssModel.getAggregatorId(), model.getAggregatorId());
        Assert.assertEquals(rssModel.getAggregatorValue(), model.getAggregatorValue());
        Assert.assertEquals(rssModel.getAlgorithmType(), model.getAlgorithmType());
        Assert.assertEquals(rssModel.getOwnerProviderId(), model.getOwnerProviderId());
        Assert.assertEquals(rssModel.getOwnerValue(), model.getOwnerValue());
        Assert.assertEquals(rssModel.getProductClass(), model.getProductClass());

        // Validate stakeholders
        Assert.assertEquals(rssModel.getStakeholders().size(), model.getStakeholders().size());
        for (int i = 0; i < rssModel.getStakeholders().size(); i++) {
            Assert.assertEquals(rssModel.getStakeholders().get(i).getStakeholderId(),
                    model.getStakeholders().get(i).getStakeholderId());

            Assert.assertEquals(rssModel.getStakeholders().get(i).getModelValue(),
                    model.getStakeholders().get(i).getModelValue());
        } 
    }

    @Test
    public void getRssModelsNullArgumentsTest() throws RSSException {
        this.mockGetProvider(rssModel.getAggregatorId(), rssModel.getOwnerProviderId());
        List<RSSModel> result = toTest.getRssModels(rssModel.getAggregatorId(),
                rssModel.getOwnerProviderId(), null);

        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void shouldUpdateRssModel() throws Exception {
        this.mockGetProvider(rssModel.getAggregatorId(), rssModel.getOwnerProviderId());
        DbeAppProvider providerModel = new DbeAppProvider();

        when(appProviderDao.getProvider(
                rssModel.getAggregatorId(),
                rssModel.getStakeholders().get(0).getStakeholderId()))
                .thenReturn(providerModel);

        SetRevenueShareConf dbModel = this.buildDatabaseRSModel();

        when(this.revenueShareConfDao.getById(Matchers.isA(SetRevenueShareConfId.class))).thenReturn(dbModel);

        // Update model values
        rssModel.setOwnerValue(new BigDecimal(40));
        rssModel.setAggregatorShare(new BigDecimal(40));

        RSSModel newModel = toTest.updateRssModel(rssModel);

        Assert.assertEquals(rssModel, newModel);

        ArgumentCaptor<SetRevenueShareConfId> idCaptor = ArgumentCaptor.forClass(SetRevenueShareConfId.class);
        verify(this.revenueShareConfDao).getById(idCaptor.capture());

        SetRevenueShareConfId revId = idCaptor.getValue();
        Assert.assertEquals(rssModel.getProductClass(), revId.getProductClass());
        Assert.assertEquals(rssModel.getOwnerProviderId(), revId.getModelOwner().getId().getTxAppProviderId());

        // Verify calls        
        ArgumentCaptor<ModelProvider> stCaptor = ArgumentCaptor.forClass(ModelProvider.class);
        verify(this.modelProviderDao).delete(stCaptor.capture());

        ModelProvider expSt = dbModel.getStakeholders().iterator().next();
        Assert.assertEquals(expSt.getStakeholder().getTxName(), stCaptor.getValue().getStakeholder().getTxName());

        ArgumentCaptor<SetRevenueShareConf> modelCaptor = ArgumentCaptor.forClass(SetRevenueShareConf.class);
        verify(this.revenueShareConfDao).update(modelCaptor.capture());

        SetRevenueShareConf newDBModel = modelCaptor.getValue();

        Assert.assertEquals(rssModel.getAggregatorValue(), newDBModel.getAggregatorValue());
        Assert.assertEquals(rssModel.getOwnerValue(), newDBModel.getOwnerValue());

        ArgumentCaptor<ModelProvider> stCreateCaptor = ArgumentCaptor.forClass(ModelProvider.class);
        verify(this.modelProviderDao).create(stCreateCaptor.capture());

        Assert.assertEquals(providerModel, stCreateCaptor.getValue().getStakeholder());
    }

    @Test
    public void throwsExceptionRSModelNotExists () throws Exception {
        this.mockGetProvider(rssModel.getAggregatorId(), rssModel.getOwnerProviderId());

        when(appProviderDao.getProvider(
                rssModel.getAggregatorId(),
                rssModel.getStakeholders().get(0).getStakeholderId()))
                .thenReturn(new DbeAppProvider());

        try {
            toTest.updateRssModel(rssModel);
            Assert.fail();
        } catch (RSSException e) {
            Assert.assertEquals(UNICAExceptionType.NON_EXISTENT_RESOURCE_ID, e.getExceptionType());
            Assert.assertEquals("Resource RSS Model does not exist", e.getMessage());
        }
    }
}