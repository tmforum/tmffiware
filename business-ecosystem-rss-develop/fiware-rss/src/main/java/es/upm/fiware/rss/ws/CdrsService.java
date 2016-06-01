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
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import es.upm.fiware.rss.service.CdrsManager;
import es.upm.fiware.rss.service.UserManager;
import es.upm.fiware.rss.exception.RSSException;
import es.upm.fiware.rss.exception.UNICAExceptionType;
import es.upm.fiware.rss.model.CDR;


@WebService(serviceName = "cdrs", name = "cdrs")
@Path("/")
public class CdrsService {

    /***
     * Logging system.
     */
    private final Logger logger = LoggerFactory.getLogger(CdrsService.class);

    @Autowired
    private CdrsManager cdrsManager;

    @Autowired
    private UserManager userManager;

    /**
     * Web service used to receive new CDRs defining a set of transactions.
     * 
     * @param cdrs, List of CDR document defining different transactions
     * @return, A CREATED response
     * @throws Exception, When a problem occur saving the transactions
     */
    @WebMethod
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createCdr(List<CDR> cdrs) throws Exception {
        logger.info("createCdr POST Start.");

        // Validate user permissions (Sellers cannot create CDRs)
        if (!userManager.isAdmin() && !userManager.isAggregator()) {
            String[] args = {"You are not allowed to create transactions"};
            throw new RSSException(UNICAExceptionType.NON_ALLOWED_OPERATION, args);
        }

        this.cdrsManager.createCDRs(cdrs);
        Response.ResponseBuilder rb = Response.status(Response.Status.CREATED.getStatusCode());
        return rb.build();
    }

    @WebMethod
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCDRs(@QueryParam("aggregatorId") String aggregatorId,
            @QueryParam("appProviderId") String providerId) throws Exception {

        logger.debug("getCDRs GET start");

        Map<String, String> ids = this.userManager.getAllowedIds(aggregatorId, providerId, "transactions");

        List<CDR> resp = this.cdrsManager.getCDRs(ids.get("aggregator"), ids.get("provider"));

        Response.ResponseBuilder rb = Response.status(Response.Status.OK.getStatusCode());
        rb.entity(resp);
        return rb.build();
    }
}
