/**
 * Copyright (C) 2015 - 2016 CoNWeT Lab., Universidad Politécnica de Madrid
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author fdelavega
 */
@Component
@Scope("singleton")
public class ThreadPoolManager {
    private Map<String, TaskPool> pendingTasks;
    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        this.executorService = Executors.newCachedThreadPool();
        this.pendingTasks = new HashMap<>();
    }

    @PreDestroy
    public void cleanUp() {
        this.executorService.shutdownNow();
    }

    /**
     * 
     * @param task
     * @param callbackUrl 
     */
    public synchronized void submitTask(ProductSettlementTask task, String callbackUrl) {
        // Save the task with its callback url
        if (!this.pendingTasks.containsKey(callbackUrl)) {
            TaskPool tp = new TaskPool();
            tp.setCallbackUrl(callbackUrl);

            this.pendingTasks.put(callbackUrl, tp);
        }
        this.pendingTasks.get(callbackUrl).addTask(task);

        // Submit the tasks to the executor service
        this.executorService.submit(task);
    }

    /**
     * 
     * @param task
     * @param callbackUrl 
     */
    public synchronized void completeTask(ProductSettlementTask task, String callbackUrl, boolean status) {
        TaskPool pool = this.pendingTasks.get(callbackUrl);
        pool.completeTask(task, status);

        // Check taskpool status
        if (pool.isFinished()) {
            SettlementNotifier notifier = new SettlementNotifier(pool);
            notifier.notifyProvider();
        }
    }

    /**
     * 
     * @param callbackUrl 
     */
    public synchronized void closeTaskPool(String callbackUrl) {
        this.pendingTasks.get(callbackUrl).close();
    }
}
