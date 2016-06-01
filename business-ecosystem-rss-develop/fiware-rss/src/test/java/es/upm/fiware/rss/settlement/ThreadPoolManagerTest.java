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

package es.upm.fiware.rss.settlement;

import es.upm.fiware.rss.settlement.ThreadPoolManager;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.eq;

/**
 *
 * @author jortiz
 */
public class ThreadPoolManagerTest {

    @Mock private ExecutorService executorService;
    @Mock private Map<String, TaskPool> tasks;

    @InjectMocks private ThreadPoolManager toTest;
    
    private TaskPool pool;
    private final String callbackUrl = "http://callbackurl.com";

    public ThreadPoolManagerTest() {
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        this.pool = mock(TaskPool.class);

        when(this.tasks.get(eq(this.callbackUrl))).thenReturn(this.pool);
    }

    private void verifySubmited(ProductSettlementTask task) {
        verify(this.pool).addTask(task);
        verify(this.executorService).submit(task);
    }

    @Test
    public void submitFirstTask() {
        // No task for the specified callback url has been submited yet
        when(this.tasks.containsKey(eq(this.callbackUrl))).thenReturn(false);
        
        // Call method
        ProductSettlementTask task = new ProductSettlementTask();
        this.toTest.submitTask(task, callbackUrl);
        
        // Verify calls
        ArgumentCaptor<TaskPool> captor = ArgumentCaptor.forClass(TaskPool.class);
        verify(this.tasks).put(eq(this.callbackUrl), captor.capture());
        
        Assert.assertEquals(this.callbackUrl, captor.getValue().getCallbackUrl());
     
        this.verifySubmited(task);
    }
    
    @Test
    public void submitTaskExistingPool() {
        // There is already an existing pool
        when(this.tasks.containsKey(eq(this.callbackUrl))).thenReturn(true);
        
        // Call method
        ProductSettlementTask task = new ProductSettlementTask();
        this.toTest.submitTask(task, callbackUrl);
        
        this.verifySubmited(task);
    }

    @Test
    public void completeTaskPoolFinished() {
        
    }
    
    @Test
    public void completeTaskPoolNotFinished() {
        when(this.pool.isFinished()).thenReturn(false);
        
        ProductSettlementTask task = new ProductSettlementTask();
        this.toTest.completeTask(task, this.callbackUrl, true);
        
        verify(this.pool).completeTask(task, true);
    }
    
    @Test
    public void closePool() {
        this.toTest.closeTaskPool(this.callbackUrl);
        
        verify(this.pool).close();
    }
}
