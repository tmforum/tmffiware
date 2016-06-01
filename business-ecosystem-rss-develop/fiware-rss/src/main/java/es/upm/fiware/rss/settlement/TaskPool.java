/**
 * Copyright (C) 2016, CoNWeT Lab., Universidad Politécnica de Madrid
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

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author fdelavega
 */
public class TaskPool {
    private TaskPoolState state = TaskPoolState.LOADING;
    private List<ProductSettlementTask> tasks = new ArrayList<>();
    private int failed = 0;
    private int completed = 0;

    private String callbackUrl;

    /**
     * 
     * @param task 
     */
    public void addTask(ProductSettlementTask task) {
        if (this.state == TaskPoolState.LOADING) {
            this.tasks.add(task);
        }
    }

    /**
     * 
     * @param task
     * @param status 
     */
    public void completeTask(ProductSettlementTask task, boolean status) {
        this.completed++;

        if (!status) {
            this.failed++;
        }

        if (this.state == TaskPoolState.PENDING && this.completed == this.tasks.size()) {
            // All the tasks has been processed
            this.state = TaskPoolState.COMPLETED;

            if (this.failed > 0) {
                this.state = this.failed == this.tasks.size() ? TaskPoolState.FAILED : TaskPoolState.PARTIAL;
            }
        }
    }
    
    /**
     * 
     */
    public void close() {
        if (this.state == TaskPoolState.LOADING) {
            this.state = this.tasks.isEmpty() ? TaskPoolState.COMPLETED : TaskPoolState.PENDING;
        }
    }

    /**
     * 
     * @return 
     */
    public boolean isFinished() {
        return this.state == TaskPoolState.COMPLETED ||
                this.state == TaskPoolState.FAILED ||
                this.state == TaskPoolState.PARTIAL;
    }

    // ============================================

    public TaskPoolState getState() {
        return state;
    }

    public List<ProductSettlementTask> getTasks() {
        return tasks;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
}
