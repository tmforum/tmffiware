/**
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
package es.upm.fiware.rss.settlement;

import es.upm.fiware.rss.algorithm.AlgorithmFactory;
import es.upm.fiware.rss.algorithm.AlgorithmProcessor;
import es.upm.fiware.rss.exception.RSSException;
import es.upm.fiware.rss.model.BmCurrency;
import es.upm.fiware.rss.model.DbeTransaction;
import es.upm.fiware.rss.model.RSSModel;
import es.upm.fiware.rss.service.SettlementManager;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.isA;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author fdelavega
 */
public class ProductSettlementTaskTest {

    @Mock private SettlementManager settlementManager;
    @Mock private ThreadPoolManager poolManager;
    @Mock private AlgorithmFactory algorithmFactory;

    @InjectMocks private ProductSettlementTask toTest;

    private List<DbeTransaction> txs = new LinkedList<>();
    private RSSModel model;
    private String callback = "http://callback.com";
    
    private AlgorithmProcessor processor;
    private RSSModel report = new RSSModel();

    @Before
    public void setUp() throws RSSException {
        // Build transactions
        BmCurrency currency = new BmCurrency();
        currency.setTxIso4217Code("EUR");

        this.txs.add(this.buildTransaction(currency, new BigDecimal(10),
                new BigDecimal(12), "C", 1, 1));
        
        this.txs.add(this.buildTransaction(currency, new BigDecimal(10),
                new BigDecimal(12), "C", 2, 2));
        
        this.txs.add(this.buildTransaction(currency, new BigDecimal(10),
                new BigDecimal(12), "R", 3, 3));
        
        // Build revenue sharing model
        this.model = new RSSModel();
        this.model.setAggregatorId("agregator@mail.com");
        this.model.setAlgorithmType("FIXED_PERCENTAGE");
        this.model.setOwnerProviderId("owner@mail.com");
        this.model.setProductClass("productClass");
        
        this.toTest = new ProductSettlementTask(model, this.txs, this.callback);
        MockitoAnnotations.initMocks(this);
        
        // Mock AlgorithmProcessor
        this.processor = mock(AlgorithmProcessor.class);
        
        when(this.algorithmFactory.getAlgorithmProcessor(eq("FIXED_PERCENTAGE")))
                .thenReturn(processor);
    }

    private DbeTransaction buildTransaction(BmCurrency currency, BigDecimal amount,
            BigDecimal taxAmount, String type, Integer corrId, Integer txId) {
        
        DbeTransaction transaction = new DbeTransaction();
        transaction.setBmCurrency(currency);
        transaction.setFtChargedAmount(amount);
        transaction.setFtChargedTaxAmount(taxAmount);
        transaction.setTcTransactionType(type);
        transaction.setTxPbCorrelationId(corrId);
        transaction.setTxTransactionId(txId);
        
        return transaction;
    }
    /*
     * Validates the run method of the product settlement task with correct 
     * transactions
     */
    @Test
    public void testRunSettlementTask() throws IOException, RSSException {
        // Mock processor behaviour
        when(this.processor.calculateRevenue(isA(RSSModel.class), isA(BigDecimal.class)))
                .thenReturn(this.report);

        // Execute method
        this.toTest.run();
        
        // Validate calls
        verify(this.processor).calculateRevenue(eq(model), eq(new BigDecimal(10)));
        verify(this.settlementManager).generateReport(eq(this.report), eq("EUR"));
        verify(this.settlementManager).setTxState(eq(this.txs), eq("processed"), eq(Boolean.TRUE));
        
        verify(this.poolManager).completeTask(toTest, callback, true);
    }

    /*
     * Validates the run method of the product settlement task with an error
     * in algorithm proccessing
     */
    @Test
    public void testRunSettlementTaskAlgException() throws IOException, RSSException {
        // Mock processor behaviour
        when(this.processor.calculateRevenue(isA(RSSModel.class), isA(BigDecimal.class)))
                .thenThrow(new RSSException("Algorithm error"));

        // Execute method
        this.toTest.run();
        
        // Validate calls
        verify(this.processor).calculateRevenue(eq(model), eq(new BigDecimal(10)));
        verify(this.settlementManager).setTxState(eq(this.txs), eq("pending"), eq(Boolean.TRUE));
        
        verify(this.poolManager).completeTask(toTest, callback, false);
    }
}
