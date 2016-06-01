/**
 * Revenue Settlement and Sharing System GE
 * Copyright (C) 2011-2014, Javier Lucio - lucio@tid.es
 * Telefonica Investigacion y Desarrollo, S.A.
 * 
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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import es.upm.fiware.rss.model.RSSModel;
import es.upm.fiware.rss.service.RSSModelsManager;
import es.upm.fiware.rss.service.AggregatorManager;
import es.upm.fiware.rss.service.ProviderManager;
import es.upm.fiware.rss.service.UserManager;
import java.util.Map;

/**
 * 
 * 
 */
@WebService(serviceName = "RSSModelService", name = "RSSModelService")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/")
public class RSSModelService {

    /**
     * Variable to print the trace.
     */
    private static Logger logger = LoggerFactory.getLogger(RSSModelService.class);

    @Autowired
    private RSSModelsManager rssModelsManager;

    @Autowired
    private ProviderManager providerManager;

    @Autowired
    private AggregatorManager aggregatorManager;
 
    @Autowired
    private UserManager userManager;

    /**
     * Get Rss Models.
     * 
     * @param appProvider
     * @param productClass
     * @param aggregatorId
     * @return
     * @throws Exception
     */
    @WebMethod
    @GET
    @Path("/")
    public Response getRssModels(@QueryParam("appProviderId") String appProvider,
        @QueryParam("productClass") String productClass,
        @QueryParam("aggregatorId") String aggregatorId)
        throws Exception {

        RSSModelService.logger.debug("Into getRssModels()");

        Map<String, String> ids = this.userManager.getAllowedIds(aggregatorId, appProvider, "RS models");

        // Call service
        List<RSSModel> rssModels = rssModelsManager.getRssModels(
                ids.get("aggregator"), ids.get("provider"), productClass);

        // Response
        ResponseBuilder rb = Response.status(Response.Status.OK.getStatusCode());
        rb.entity(rssModels);
        return rb.build();
    }

    /**
     * Create Rss Model
     * 
     * @param rssModel
     * @return
     * @throws Exception
     */
    @WebMethod
    @POST
    @Path("/")
    @Consumes("application/json")
    public Response createRSSModel(RSSModel rssModel) throws Exception {
        RSSModelService.logger.debug("Into createRSSModel method");
        // check security
        Map<String, String> ids = this.userManager.getAllowedIdsSingleProvider(
                rssModel.getAggregatorId(), rssModel.getOwnerProviderId(), "RS models");

        //Override RS models fields with the effective aggregator and provider
        rssModel.setAggregatorId(ids.get("aggregator"));
        rssModel.setOwnerProviderId(ids.get("provider"));

        // Call service
        RSSModel model = rssModelsManager.createRssModel(rssModel);
        // Building response
        ResponseBuilder rb = Response.status(Response.Status.CREATED.getStatusCode());
        rb.entity(model);
        return rb.build();
    }

    /**
     * Update Rss model
     * 
     * @param rssModel
     * @return
     * @throws Exception
     */
    @WebMethod
    @PUT
    @Path("/")
    @Consumes("application/json")
    public Response modifyRSSModel(RSSModel rssModel) throws Exception {
        RSSModelService.logger.debug("Into modifyRSSModel method");

        Map<String, String> ids = this.userManager.getAllowedIdsSingleProvider(
                rssModel.getAggregatorId(), rssModel.getOwnerProviderId(), "RS models");

        //Override RS models fields with the effective aggregator and provider
        rssModel.setAggregatorId(ids.get("aggregator"));
        rssModel.setOwnerProviderId(ids.get("provider"));

        // Call service
        RSSModel model = rssModelsManager.updateRssModel(rssModel);
        // Building response
        ResponseBuilder rb = Response.status(Response.Status.CREATED.getStatusCode());
        rb.entity(model);
        return rb.build();
    }

    /**
     * Delete Rss Models.
     * 
     * @param aggregatorId
     * @param appProvider
     * @param productClass
     * @return
     * @throws Exception
     */
    @WebMethod
    @DELETE
    @Path("/")
    @Consumes("application/json")
    public Response deleteRSSModel(
        @QueryParam("aggregatorId") String aggregatorId,
        @QueryParam("appProviderId") String appProvider,
        @QueryParam("productClass") String productClass) throws Exception {
        RSSModelService.logger.debug("Into deleteRSSModel method");
        // check security
        Map<String, String> ids = this.userManager.getAllowedIdsSingleProvider(
                aggregatorId, appProvider, "RS models");

        // Call service
        rssModelsManager.deleteRssModel(ids.get("aggregator"), ids.get("provider"), productClass);
        // Building response
        ResponseBuilder rb = Response.status(Response.Status.NO_CONTENT.getStatusCode());
        return rb.build();
    }
}
