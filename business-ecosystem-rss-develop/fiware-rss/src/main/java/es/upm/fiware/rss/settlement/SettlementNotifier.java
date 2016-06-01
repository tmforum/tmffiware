/**
 * Copyright (C) 2016 CoNWeT Lab., Universidad Politécnica de Madrid
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

/**
 *
 * @author fdelavega
 */
public class SettlementNotifier {
    
    private TaskPool pool;

    public SettlementNotifier(TaskPool pool) {
        this.pool = pool;
    }
    
    public void notifyProvider() {
        // Makes a POST request to the specified callback URL
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(this.pool.getCallbackUrl());
        post.setHeader("Content-type", MediaType.APPLICATION_JSON);
        
        Map<String, String> data = new HashMap<>();
        data.put("status", pool.getState().toString());

        try {
            HttpEntity entity = new StringEntity(new JSONObject(data).toString());
            post.setEntity(entity);
            
            // A failure in the notification must not block the system
            client.execute(post);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(SettlementNotifier.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SettlementNotifier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
