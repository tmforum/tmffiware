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
import java.util.ArrayList;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;

import es.upm.fiware.rss.exception.RSSException;
import es.upm.fiware.rss.exception.UNICAExceptionType;
import es.upm.fiware.rss.model.Aggregator;
import es.upm.fiware.rss.service.AggregatorManager;
import es.upm.fiware.rss.service.UserManager;

/**
 *
 * @author fdelavega
 */
@Path("/")
@WebService(serviceName = "aggregators", name="aggregators")
public class AggregatorService {

    @Autowired
    UserManager userManager;

    @Autowired
    AggregatorManager aggregatorManager;

    @WebMethod
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Response getAggregators() throws Exception {

        List<Aggregator> aggregators;

        if (userManager.isAdmin()) {
            aggregators = aggregatorManager.getAPIAggregators();
        } else if (userManager.isAggregator()) {
            aggregators = new ArrayList<>();
            aggregators.add(aggregatorManager.getAggregator(userManager.getCurrentUser().getEmail()));
        } else {
            String[] args = {"You are not allowed to retrieve aggregators"};
            throw new RSSException(UNICAExceptionType.NON_ALLOWED_OPERATION, args);
        }

        Response.ResponseBuilder rb = Response.status(Response.Status.OK.getStatusCode());
        rb.entity(aggregators);
        return rb.build();
    }

    @WebMethod
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public Response createAggregator(Aggregator aggregator) throws Exception {
        if (!this.userManager.isAdmin()) {
            String[] args = {"You are not allowed to create aggregators"};
            throw new RSSException(UNICAExceptionType.NON_ALLOWED_OPERATION, args);
        }

        this.aggregatorManager.createAggretator(aggregator);

        Response.ResponseBuilder rb = Response.status(Response.Status.CREATED.getStatusCode());
        return rb.build();
    }
}
