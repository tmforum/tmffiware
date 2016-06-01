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

import org.junit.Before;
import org.junit.Test;
import es.upm.fiware.rss.dao.DbeTransactionDao;
import es.upm.fiware.rss.exception.RSSException;
import es.upm.fiware.rss.model.Aggregator;
import es.upm.fiware.rss.model.DbeTransaction;
import es.upm.fiware.rss.model.RSSModel;
import es.upm.fiware.rss.model.RSSProvider;
import es.upm.fiware.rss.model.SettlementJob;
import es.upm.fiware.rss.settlement.ProductSettlementTask;
import es.upm.fiware.rss.settlement.SettlementTaskFactory;
import es.upm.fiware.rss.settlement.ThreadPoolManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.isA;
import org.mockito.MockitoAnnotations;

public class SettlementManagerTest {

    @Mock private DbeTransactionDao transactionDao;
    @Mock private SettlementTaskFactory taskFactory;
    @Mock private AggregatorManager aggregatorManager;
    @Mock private ProviderManager providerManager;
    @Mock private RSSModelsManager modelsManager;
    @Mock private ThreadPoolManager poolManager;
    @InjectMocks private SettlementManager toTest;

    private String aggregatorId;
    private String providerId;
    private String productClass;
    private String callbackUrl;
    private SettlementJob job;
    private Aggregator aggregator;
    private RSSProvider rSSProvider;
    private RSSModel model;
    private List <RSSModel> models;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Build models
        this.aggregatorId = "aggregator@mail.com";
        this.providerId = "provider@mail.com";
        this.productClass = "productClass";
        this.callbackUrl = "http://callback.com";
        
        this.job = new SettlementJob();
        
        this.job.setAggregatorId(aggregatorId);
        this.job.setProviderId(providerId);
        this.job.setProductClass(productClass);
        this.job.setCallbackUrl(callbackUrl);
        
        this.aggregator = this.buildAggregator(aggregatorId);
        this.rSSProvider = this.buildProvider(aggregatorId, providerId);
        this.model = this.buildModel(aggregatorId, providerId, productClass);
        
        this.models = new LinkedList<>();
        this.models.add(model);
    }

    private Aggregator buildAggregator (String aggregatorId) {
        Aggregator agg = new Aggregator();
        agg.setAggregatorId(aggregatorId);
        agg.setAggregatorName(aggregatorId);
        
        return agg;
    }

    private List<Aggregator> buildAggregatorList(String... aggs) {
        List<Aggregator> aggregators = new ArrayList<>();
        for (String agg: aggs) {
            aggregators.add(this.buildAggregator(agg));
        }
        return aggregators;
    }

    private RSSProvider buildProvider(String aggregatorId, String providerId) {
        RSSProvider provider = new RSSProvider();
        provider.setAggregatorId(aggregatorId);
        provider.setProviderId(providerId);
        provider.setProviderName(providerId);
        
        return provider;
    }

    private List<RSSProvider> buildProviders(String aggregatorId, String... providerIds) {
        List<RSSProvider> providers = new ArrayList<>();
        for (String prov: providerIds) {
            providers.add(this.buildProvider(aggregatorId, prov));
        }
        return providers;
    }

    private RSSModel buildModel(String aggregatorId, String providerId, String productClass) {
        RSSModel mod = new RSSModel();
        mod.setAggregatorId(aggregatorId);
        mod.setAggregatorShare(BigDecimal.ZERO);
        mod.setAlgorithmType(aggregatorId);
        mod.setOwnerProviderId(providerId);
        mod.setOwnerValue(BigDecimal.ZERO);
        mod.setProductClass(productClass);
        mod.setStakeholders(null);
        return mod;
    }

    private List<RSSModel> buildModels(String aggregatorId,
            String providerId, String... productClasses) {

        List<RSSModel> mods = new ArrayList<>();
        for (String prodClass: productClasses) {
            mods.add(this.buildModel(aggregatorId, providerId, prodClass));
        }
        return mods;
    }

    private List<DbeTransaction> buildTransactions() {
        List <DbeTransaction> transactions = new LinkedList<>();
        DbeTransaction transaction = new DbeTransaction();
        transactions.add(transaction);
        return transactions;
    }

    private ProductSettlementTask buildTask(RSSModel model) {
        List<DbeTransaction> tx1 = this.buildTransactions();
        when(transactionDao.getTransactions(model.getAggregatorId(), model.getOwnerProviderId(), model.getProductClass()))
                .thenReturn(tx1);
        
        ProductSettlementTask t1 = new ProductSettlementTask();
        when(taskFactory.getSettlementTask(model, tx1, callbackUrl)).thenReturn(t1);
        
        return t1;
    }

    private void mockSingleModel() throws RSSException {
        when(modelsManager.existModel(aggregatorId, providerId, productClass)).thenReturn(Boolean.TRUE);
        when(aggregatorManager.getAggregator(aggregatorId)).thenReturn(aggregator);
        when(providerManager.getProvider(aggregatorId, providerId)).thenReturn(rSSProvider);
        when(modelsManager.getRssModels(aggregatorId, providerId, productClass)).thenReturn(models);
    }

    @Test
    /**
     * Validates the run settlement process for the specific transactions of a 
     * provider product class
     */
    public void testRunSettlementProviderTx() throws RSSException {
        List <DbeTransaction> transactions = this.buildTransactions();

        // Create Mocks
        this.mockSingleModel();
        when(transactionDao.getTransactions(aggregatorId, providerId, productClass)).thenReturn(transactions);

        ProductSettlementTask settlementTask = new ProductSettlementTask();
        when(taskFactory.getSettlementTask(model, transactions, callbackUrl)).thenReturn(settlementTask);

        // Execute method
        toTest.runSettlement(job);
        
        // Validate calls
        verify(modelsManager).checkValidAppProvider(aggregatorId, providerId);
        verify(poolManager).submitTask(settlementTask, callbackUrl);
        verify(poolManager).closeTaskPool(callbackUrl);
    }
    
    @Test
    /**
     * Verifies the run settlement process when no transaction available for a 
     * specific provider and product class
     */
    public void testRunSettlementProviderNoTransactions() throws RSSException {
        // Create Mocks
        this.mockSingleModel();
        
        // Execute Mwethod
        toTest.runSettlement(job);
        
        // Validate calls
        verify(modelsManager).checkValidAppProvider(aggregatorId, providerId);
        verify(poolManager, never()).submitTask(isA(ProductSettlementTask.class), isA(String.class));
        verify(poolManager).closeTaskPool(callbackUrl);
    }
    

    /*
     * Verifies the run settlement process for all pending transactions
     */
    @Test
    public void testRunSettlementAll() throws RSSException {
        // Create Mocks
        this.job.setAggregatorId(null);
        this.job.setProviderId(null);
        this.job.setProductClass(null);
        
        // Mock aggregators
        List<Aggregator> aggregators = this.buildAggregatorList("aggregator1@email.com", "aggregator2@email.com");
        when(aggregatorManager.getAPIAggregators()).thenReturn(aggregators);
        
        // Mock providers
        List<RSSProvider> providers1 = this.buildProviders("aggregator1@email.com", "provider1", "provider2");
        List<RSSProvider> providers2 = this.buildProviders("aggregator2@email.com", "provider3", "provider4");
        
        when(providerManager.getAPIProviders("aggregator1@email.com")).thenReturn(providers1);
        when(providerManager.getAPIProviders("aggregator2@email.com")).thenReturn(providers2);

        // Mock models
        List<RSSModel> models1 = this.buildModels("aggregator1@email.com", "provider1", "class1", "class2");
        List<RSSModel> models2 = this.buildModels("aggregator1@email.com", "provider2", "class3", "class4");
        List<RSSModel> models3 = this.buildModels("aggregator2@email.com", "provider3", "class5", "class6");
        List<RSSModel> models4 = this.buildModels("aggregator2@email.com", "provider4", "class7", "class8");
        
        when(modelsManager.getRssModels("aggregator1@email.com", "provider1", null)).thenReturn(models1);
        when(modelsManager.getRssModels("aggregator1@email.com", "provider2", null)).thenReturn(models2);
        when(modelsManager.getRssModels("aggregator2@email.com", "provider3", null)).thenReturn(models3);
        when(modelsManager.getRssModels("aggregator2@email.com", "provider4", null)).thenReturn(models4);
        
        // Mock Transactions
        List<ProductSettlementTask> tasks = new ArrayList<>();
        tasks.add(this.buildTask(models1.get(0)));
        tasks.add(this.buildTask(models1.get(1)));
        tasks.add(this.buildTask(models2.get(0)));
        tasks.add(this.buildTask(models2.get(1)));
        tasks.add(this.buildTask(models3.get(0)));
        tasks.add(this.buildTask(models3.get(1)));
        tasks.add(this.buildTask(models4.get(0)));
        tasks.add(this.buildTask(models4.get(1)));
        
        // Execute Method
        toTest.runSettlement(job);
        
        // Validate calls
        for (ProductSettlementTask task: tasks) {
            verify(poolManager).submitTask(task, callbackUrl);
        }

        verify(poolManager).closeTaskPool(callbackUrl);
    }

    @Test
    (expected = RSSException.class)
    public void runSettlementRSSExceptionTest() throws RSSException {
        when(modelsManager.existModel(aggregatorId, providerId, productClass)).thenReturn(false);

        toTest.runSettlement(job);
    }

    @Test
    public void setTxStateTest() {
        String state = "state";
        List <DbeTransaction> transactions = new LinkedList<>();

        DbeTransaction transaction = new DbeTransaction();
        transactions.add(transaction);

        toTest.setTxState(transactions, state, true);
    }

    @Test
    public void setTxStateNotFlushTest() {
        String state = "state";
        List <DbeTransaction> transactions = new LinkedList<>();

        DbeTransaction transaction = new DbeTransaction();
        transactions.add(transaction);

        toTest.setTxState(transactions, state, false);
    }

}
