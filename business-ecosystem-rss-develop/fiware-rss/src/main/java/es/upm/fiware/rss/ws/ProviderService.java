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
package es.upm.fiware.rss.ws;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;

import es.upm.fiware.rss.exception.RSSException;
import es.upm.fiware.rss.exception.UNICAExceptionType;
import es.upm.fiware.rss.model.Aggregator;
import es.upm.fiware.rss.model.RSSProvider;
import es.upm.fiware.rss.model.RSUser;
import es.upm.fiware.rss.service.AggregatorManager;
import es.upm.fiware.rss.service.ProviderManager;
import es.upm.fiware.rss.service.UserManager;

/**
 *
 * @author fdelavega
 */
@Path("/")
@WebService(serviceName = "providers", name="providers")
public class ProviderService {

    @Autowired
    ProviderManager providerManager;

    @Autowired
    UserManager userManager;

    @Autowired
    AggregatorManager aggregatorManager;

    /**
     * 
     * @param providerInfo
     * @throws Exception
     * @return 
     */
    @WebMethod
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createProvider(RSSProvider providerInfo) throws Exception{

        RSUser user = userManager.getCurrentUser();

        // Get the effective aggregator
        if (providerInfo.getAggregatorId() == null
                || providerInfo.getAggregatorId().isEmpty()) {
            // If the aggregator id has not been provided the provider is created
            // in the default aggregator
            providerInfo.setAggregatorId(aggregatorManager
                    .getDefaultAggregator()
                    .getAggregatorId());
        }

        // Check if the user can create a provider for the given aggregator
        if (!userManager.isAggregator() && !providerInfo.getAggregatorId().equals(user.getEmail())
                && !userManager.isAdmin()) {
            String[] args = {"You are not allowed to create a provider for the given aggregatorId"};
            throw new RSSException(UNICAExceptionType.NON_ALLOWED_OPERATION, args);
        }

        // Create a new provider for the store represented by the User (AggregatorID)
        providerManager.createProvider(providerInfo);

        ResponseBuilder rb = Response.status(Response.Status.CREATED.getStatusCode());
        return rb.build();
    }

    @WebMethod
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProviders(@QueryParam("aggregatorId") String aggregatorId)
            throws Exception{

        RSUser user = userManager.getCurrentUser();
        String effectiveAggregator = aggregatorId;

        if (!userManager.isAdmin() && aggregatorId == null) {
            Aggregator defaultAggregator = this.aggregatorManager.getDefaultAggregator();

            if (defaultAggregator == null) {
                String[] args = {"There isn't any aggregator registered"};
                throw new RSSException(UNICAExceptionType.NON_ALLOWED_OPERATION, args);
            }
            effectiveAggregator = defaultAggregator.getAggregatorId();
        }

        List<RSSProvider> providers = providerManager.getAPIProviders(effectiveAggregator);
        ResponseBuilder rb = Response.status(Response.Status.OK.getStatusCode());
        rb.entity(providers);
        return rb.build();
    }
}
