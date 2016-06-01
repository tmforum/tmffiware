
/*
 * #%L
 * FiwareMarketplace
 * %%
 * Copyright (C) 2014-2015 CoNWeT Lab, Universidad Politécnica de Madrid
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of copyright holders nor the names of its contributors
 *    may be used to endorse or promote products derived from this software 
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package es.upm.fiware.rss.oauth.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pac4j.oauth.credentials.OAuthCredentials;
import org.pac4j.springframework.security.authentication.ClientAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Filter that will identify a user when the Authorization header is specified
 * in a request to the API
 *
 * @author aitor
 *
 */
public class FIWAREHeaderAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private String userHeader;
    private String rolesHeader;
    private String emailHeader;
    private final String clientName = "businessEcosystem";
    
    @Autowired
    private AuthUserManager userManager;

    protected FIWAREHeaderAuthenticationFilter() {
        this("/rss/", "X-Nick-Name", "X-Roles", "X-Email");
    }

    protected FIWAREHeaderAuthenticationFilter(String baseUrl, String userHeader,
            String rolesHeader, String emailHeader) {
        // Super class constructor must be called. 
        super(new FIWAREHeaderAuthenticationRequestMatcher(baseUrl, userHeader, rolesHeader, emailHeader));

        // Store header name
        this.userHeader = userHeader;
        this.rolesHeader = rolesHeader;
        this.emailHeader = emailHeader;

        // Needed to continue with the process of the request
        setContinueChainBeforeSuccessfulAuthentication(true);

        // Set the authentication in the context
        setSessionAuthenticationStrategy(new FIWAREHeaderAuthenticationStrategy());

        // This handler doesn't do anything but it's required to replace the default one
        setAuthenticationSuccessHandler(new FIWAREHeaderAuthenticationSuccessHandler());
    }

    private FIWAREProfile extractUserProfile(String id, String roles, String email) {
        // Build new FIWARE User profile
        FIWAREProfile profile = new FIWAREProfile();
        profile.setId(id);
        profile.addAttribute("email", email);
        profile.addAttribute("roles", userManager.buildUserRoles(roles, email));

        profile.addRole("ROLE_USER");

        // User information should be stored in the local users table
        userManager.updateUser(profile);

        return profile;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException,
            IOException, ServletException {

        Authentication auth = null;
        String actorId = request.getHeader(this.userHeader);
        String roles = request.getHeader(this.rolesHeader);
        String email = request.getHeader(this.emailHeader);

        try {
            // We only have one possible match
            if (actorId != null && !actorId.isEmpty() && roles != null && !roles.isEmpty()) {

                // This method can return an exception when the Token is invalid
                // In this case, the exception is caught and the correct exceptions is thrown...
                FIWAREProfile profile = this.extractUserProfile(actorId, roles, email);

                // Define authorities
                Collection<GrantedAuthority> authorities = new ArrayList<>();

                profile.getRoles().stream().forEach((role) -> {
                    authorities.add(new SimpleGrantedAuthority(role));
                });

                // new token with credentials (like previously) and user profile and authorities
                OAuthCredentials credentials = new OAuthCredentials(null, actorId, "", this.clientName);
                auth = new ClientAuthenticationToken(credentials, this.clientName, profile, authorities);
            } else {
                // This is not supposed to happen
                throw new IllegalStateException("Pattern is suppossed to match.");
            }

        } catch (Exception ex) {
            // This exception should be risen in order to return a 401
            throw new BadCredentialsException("The provided token is invalid or the system was not able to check it");
        }

        return auth;
    }

	// AUXILIAR CLASSES //
    /**
     * Request Matcher that specifies when the filter should be executed. In
     * this case we want the filter to be executed when the following two
     * conditions are true: 1) The request is to the API (/api/...) 2) The
     * X-Auth-Token header is present (Authorization: ...)
     *
     * @author aitor
     *
     */
    static class FIWAREHeaderAuthenticationRequestMatcher implements RequestMatcher {

        private final String baseUrl;
        private final String userHeader;
        private final String rolesHeader;
        private final String emailHeader;

        public FIWAREHeaderAuthenticationRequestMatcher(String baseUrl, String userHeader,
                String rolesHeader, String emailHeader) {
            
            this.baseUrl = baseUrl;
            this.userHeader = userHeader;
            this.rolesHeader = rolesHeader;
            this.emailHeader = emailHeader;
        }

        @Override
        public boolean matches(HttpServletRequest request) {

            String actorId = request.getHeader(this.userHeader);
            String roles = request.getHeader(this.rolesHeader);
            String email = request.getHeader(this.emailHeader);

            // Get path
            String url = request.getServletPath();
            String pathInfo = request.getPathInfo();
            String query = request.getQueryString();

            if (pathInfo != null || query != null) {
                StringBuilder sb = new StringBuilder(url);

                if (pathInfo != null) {
                    sb.append(pathInfo);
                }

                if (query != null) {
                    sb.append('?').append(query);
                }
                url = sb.toString();
            }

            return url.startsWith(baseUrl) && actorId != null && !actorId.isEmpty()
                    && roles != null && !roles.isEmpty() && email != null && !email.isEmpty();
        }
    }

    /**
     * Actions to be carried out when the authentication is successful. In this
     * case no actions are required.
     *
     * @author aitor
     *
     */
    static class FIWAREHeaderAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request,
                HttpServletResponse response, Authentication authentication)
                throws IOException, ServletException {
            // Nothing to do... The chain will continue
        }
    }

    /**
     * Set the Session in the Security Context when the Authorization token is
     * valid
     *
     * @author aitor
     *
     */
    static class FIWAREHeaderAuthenticationStrategy implements SessionAuthenticationStrategy {

        @Override
        public void onAuthentication(Authentication authentication,
                HttpServletRequest request, HttpServletResponse response)
                throws SessionAuthenticationException {
            // Set the authentication in the current context
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

    }
}
