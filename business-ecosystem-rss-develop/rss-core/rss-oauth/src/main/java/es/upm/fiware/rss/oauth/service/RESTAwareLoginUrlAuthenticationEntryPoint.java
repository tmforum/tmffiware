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

package es.upm.fiware.rss.oauth.service;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * By default, clients attempts to log in users when they access a protected resource without credentials. However,
 * this behavior is not appropriate when the API is being used, since developers expect a 401 HTTP error when the user
 * is not authenticated. This class changes this behavior to work as expected by developers:
 * <ul>
 * <li>401 is returned when an unauthenticated users attempts to access a protected resource in the API</li>
 * <li>Users are redirected to the login page when they try to access a protected resource outside the API</li>
 * </ul>
 * @author aitor
 *
 */
public class RESTAwareLoginUrlAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}
