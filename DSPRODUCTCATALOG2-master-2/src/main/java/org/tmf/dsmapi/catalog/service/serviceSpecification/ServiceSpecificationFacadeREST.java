package org.tmf.dsmapi.catalog.service.serviceSpecification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.tmf.dsmapi.catalog.entity.service.ServiceSpecificationEntity;
import org.tmf.dsmapi.catalog.exception.IllegalLifecycleStatusException;
import org.tmf.dsmapi.catalog.hub.service.serviceSpecification.ServiceSpecificationEventPublisherLocal;
import org.tmf.dsmapi.catalog.resource.LifecycleStatus;
import org.tmf.dsmapi.catalog.resource.service.ServiceSpecification;
import org.tmf.dsmapi.catalog.service.AbstractFacadeREST;
import org.tmf.dsmapi.catalog.service.ServiceConstants;
import org.tmf.dsmapi.commons.ParsedVersion;
import org.tmf.dsmapi.commons.QueryParameterParser;
import org.tmf.dsmapi.commons.exceptions.BadUsageException;
import org.tmf.dsmapi.commons.jaxrs.PATCH;

/**
 *
 * @author bahman.barzideh
 *
 */
@Stateless
@Path("serviceSpecification")
public class ServiceSpecificationFacadeREST extends AbstractFacadeREST<ServiceSpecificationEntity> {
    private static final Logger logger = Logger.getLogger(ServiceSpecification.class.getName());

    @EJB
    private ServiceSpecificationFacade manager;

    @EJB
    ServiceSpecificationEventPublisherLocal publisher;
    
    /*
     *
     */
    public ServiceSpecificationFacadeREST() {
        super(ServiceSpecificationEntity.class);
    }

    /*
     *
     */
    @Override
    public Logger getLogger() {
        return logger;
    }

    /*
     *
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response create(ServiceSpecificationEntity input, @Context UriInfo uriInfo) throws IllegalLifecycleStatusException {
        logger.log(Level.FINE, "ServiceSpecificationFacadeREST:create()");

        if (input == null) {
            logger.log(Level.FINE, "input is required");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        input.setCreateDefaults();

        if (input.isValid() == false) {
            logger.log(Level.FINE, "input is not valid");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (input.canLifecycleTransitionFrom (null) == false) {
            logger.log(Level.FINE, "invalid lifecycleStatus: {0}", input.getLifecycleStatus());
            throw new IllegalLifecycleStatusException(LifecycleStatus.transitionableStatues(null));
        }

        input.configureCatalogIdentifier();
        manager.create(input);

        input.setHref(buildHref(uriInfo, input.getId(), input.getParsedVersion()));
        manager.edit(input);

        publisher.createNotification(input, null, null);
        return Response.status(Response.Status.CREATED).entity(input).build();
    }

    /*
     *
     */
    @PUT
    @Path("{entityId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response update(@PathParam("entityId") String entityId, ServiceSpecificationEntity input, @Context UriInfo uriInfo) throws IllegalLifecycleStatusException {
        logger.log(Level.FINE, "ServiceSpecificationFacadeREST:update(entityId: {0})", entityId);

        return update_(entityId, null, input, uriInfo);
    }

    /*
     *
     */
    @PUT
    @Path("{entityId}:({entityVersion})")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response update(@PathParam("entityId") String entityId, @PathParam("entityVersion") ParsedVersion entityVersion, ServiceSpecificationEntity input, @Context UriInfo uriInfo) throws IllegalLifecycleStatusException {
        logger.log(Level.FINE, "ServiceSpecificationFacadeREST:update(entityId: {0}, entityVersion: {1})", new Object[]{entityId, entityVersion});

        return update_(entityId, entityVersion, input, uriInfo);
    }

    /*
     *
     */
    @PATCH
    @Path("{entityId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response edit(@PathParam("entityId") String entityId, ServiceSpecificationEntity input, @Context UriInfo uriInfo) throws IllegalLifecycleStatusException {
        logger.log(Level.FINE, "ServiceSpecificationFacadeREST:edit(entityId: {0})", entityId);

        return edit_(entityId, null, input, uriInfo);
    }

    /*
     *
     */
    @PATCH
    @Path("{entityId}:({entityVersion})")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response edit(@PathParam("entityId") String entityId, @PathParam("entityVersion") ParsedVersion entityVersion, ServiceSpecificationEntity input, @Context UriInfo uriInfo) throws IllegalLifecycleStatusException {
        logger.log(Level.FINE, "ServiceSpecificationFacadeREST:edit(entityId: {0}, entityVersion: {1})", new Object[]{entityId, entityVersion});

        return edit_(entityId, entityVersion, input, uriInfo);
    }

    /*
     *
     */
    @DELETE
    @Path("{entityId}")
    public Response remove(@PathParam("entityId") String entityId) {
        logger.log(Level.FINE, "ServiceSpecificationFacadeREST:remove(entityId: {0})", entityId);

        return remove_(entityId, null);
    }

    /*
     *
     */
    @DELETE
    @Path("{entityId}:({entityVersion})")
    public Response remove(@PathParam("entityId") String entityId, @PathParam("entityVersion") ParsedVersion entityVersion) {
        logger.log(Level.FINE, "ServiceSpecificationFacadeREST:remove(entityId: {0}, entityVersion: {1})", new Object[]{entityId, entityVersion});

        return remove_(entityId, entityVersion);
    }

    /*
     *
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response find(@QueryParam("depth") int depth, @Context UriInfo uriInfo) throws BadUsageException {
        logger.log(Level.FINE, "ServiceSpecificationFacadeREST:find(depth: {0})", depth);

        QueryParameterParser queryParameterParser = new QueryParameterParser(uriInfo.getRequestUri().getQuery());

        // Remove known parameters before running the query.
        Set<String> outputFields = getFieldSet(queryParameterParser);
        queryParameterParser.removeTagWithValues("depth");

        Set<ServiceSpecificationEntity> entities = manager.find(queryParameterParser.getTagsWithValue());
        if (entities == null || entities.size() <= 0) {
            return Response.ok(entities).build();
        }

        getReferencedEntities(entities, depth);

        if (outputFields.isEmpty() || outputFields.contains(ServiceConstants.ALL_FIELDS)) {
            return Response.ok(entities).build();
        }

       ArrayList<Object> outputEntities = selectFields(entities, outputFields);
       return Response.ok(outputEntities).build();
    }

    /*
     *
     */
    @GET
    @Path("{entityId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findById(@PathParam("entityId") String entityId, @QueryParam("depth") int depth, @Context UriInfo uriInfo) {
        logger.log(Level.FINE, "ServiceSpecificationFacadeREST:find(entityId: {0}, depth: {1})", new Object[]{entityId, depth});

        return find_(entityId, null, depth, uriInfo);
    }

    /*
     *
     */
    @GET
    @Path("{entityId}:({entityVersion})")
    @Produces({MediaType.APPLICATION_JSON})
    public Response find(@PathParam("entityId") String entityId, @PathParam("entityVersion") ParsedVersion entityVersion, @QueryParam("depth") int depth, @Context UriInfo uriInfo) {
        logger.log(Level.FINE, "ServiceSpecificationFacadeREST:find(entityId: {0}, entityVersion: {1}, depth: {2})", new Object[]{entityId, entityVersion, depth});

        return find_(entityId, entityVersion, depth, uriInfo);
    }

    /*
     *
     */
    @GET
    @Path("admin/proto")
    @Produces({MediaType.APPLICATION_JSON})
    public Response proto() {
        logger.log(Level.FINE, "ServiceSpecificationFacadeREST:proto()");

        return Response.ok(ServiceSpecificationEntity.createProto()).build();
    }

    /*
     *
     */
    @GET
    @Path("admin/count")
    @Produces({MediaType.TEXT_PLAIN})
    public Response count() {
        logger.log(Level.FINE, "ServiceSpecificationFacadeREST:count()");

        int entityCount = manager.count();
        return Response.ok(String.valueOf(entityCount)).build();
    }

    /*
     *
     */
    private Response update_(String entityId, ParsedVersion entityVersion, ServiceSpecificationEntity input, UriInfo uriInfo) throws IllegalLifecycleStatusException {
        logger.log(Level.FINE, "ServiceSpecificationFacadeREST:update_(entityId: {0}, entityVersion: {1})", new Object[]{entityId, entityVersion});

        if (input == null) {
            logger.log(Level.FINE, "input is required.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (input.isValid() == false) {
            logger.log(Level.FINE, "invalid input.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        List<ServiceSpecificationEntity> entities = manager.findById(ServiceSpecificationEntity.ROOT_CATALOG_ID, ParsedVersion.ROOT_CATALOG_VERSION, entityId, entityVersion);
        ServiceSpecificationEntity entity = (entities != null && entities.size() > 0) ? entities.get(0) : null;
        if (entity == null) {
            logger.log(Level.FINE, "requested ServiceSpecification [{0}, {1}] not found", new Object[]{entityId, entityVersion});
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        validateLifecycleStatus(input, entity);

        input.configureCatalogIdentifier();

        if (input.keysMatch(entity)) {
            input.setHref(buildHref(uriInfo, input.getId(), input.getParsedVersion()));
            manager.edit(input);
            
            publisher.updateNotification(input, null, null);
            return Response.status(Response.Status.CREATED).entity(entity).build();
        }

        if (input.hasHigherVersionThan(entity) == false) {
            logger.log(Level.FINE, "specified version ({0}) must be higher than entity version ({1})", new Object[]{input.getVersion(), entity.getVersion()});
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        manager.remove(entity);
        manager.create(input);

        input.setHref(buildHref(uriInfo, input.getId(), input.getParsedVersion()));
        manager.edit(input);

        publisher.updateNotification(input, null, null);
        return Response.status(Response.Status.CREATED).entity(input).build();
    }

    /*
     *
     */
    private Response edit_(String entityId, ParsedVersion entityVersion, ServiceSpecificationEntity input, UriInfo uriInfo) throws IllegalLifecycleStatusException {
        logger.log(Level.FINE, "ServiceSpecificationFacadeREST:edit_(entityId: {0}, entityVersion: {1})", new Object[]{entityId, entityVersion});

        if (input == null) {
            logger.log(Level.FINE, "input is required");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        List<ServiceSpecificationEntity> entities = manager.findById(ServiceSpecificationEntity.ROOT_CATALOG_ID, ParsedVersion.ROOT_CATALOG_VERSION, entityId, entityVersion);
        ServiceSpecificationEntity entity = (entities != null && entities.size() > 0) ? entities.get(0) : null;
        if (entity == null) {
            logger.log(Level.FINE, "requested ServiceSpecification [{0}, {1}] not found", new Object[]{entityId, entityVersion});
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        input.edit(entity);
        input.setCatalogId(entity.getCatalogId());
        input.setCatalogVersion(entity.getCatalogVersion());
        input.setId(entity.getId());

        if(input.isValid() == false) {
            logger.log(Level.FINE, "patched ServiceSpecification would be invalid");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        validateLifecycleStatus(input, entity);

        if (input.getVersion() == null) {
            input.setVersion(entity.getVersion());
            input.setHref(buildHref(uriInfo, input.getId(), input.getParsedVersion()));
            manager.edit(input);
            
            publisher.valueChangedNotification(input, null, null);
            return Response.status(Response.Status.CREATED).entity(entity).build();
        }

        if (input.hasHigherVersionThan(entity) == false) {
            logger.log(Level.FINE, "specified version ({0}) must be higher than entity version ({1})", new Object[]{input.getVersion(), entity.getVersion()});
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        manager.remove(entity);

        input.setHref(buildHref(uriInfo, input.getId(), input.getParsedVersion()));
        manager.create(input);

        publisher.valueChangedNotification(input, null, null);
        return Response.status(Response.Status.CREATED).entity(input).build();
    }

    /*
     *
     */
    private Response remove_(String entityId, ParsedVersion entityVersion) {
        logger.log(Level.FINE, "ServiceSpecificationFacadeREST:remove_(entityId: {0}, entityVersion: {1})", new Object[]{entityId, entityVersion});

        List<ServiceSpecificationEntity> entities = manager.findById(ServiceSpecificationEntity.ROOT_CATALOG_ID, ParsedVersion.ROOT_CATALOG_VERSION, entityId, entityVersion);
        if (entities == null || entities.size() <= 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        ServiceSpecificationEntity entity = entities.get(0);
        manager.remove(entity);
 
//        publisher.deletionNotification(entity, null, null);
        return Response.ok().build();
    }

    /*
     *
     */
    private Response find_(String entityId, ParsedVersion entityVersion, int depth, UriInfo uriInfo) {
        logger.log(Level.FINE, "ServiceSpecificationFacadeREST:find_(entityId: {0}, entityVersion: {1}, depth: {2})", new Object[]{entityId, entityVersion, depth});

        List<ServiceSpecificationEntity> entities = manager.findById(ServiceSpecificationEntity.ROOT_CATALOG_ID, ParsedVersion.ROOT_CATALOG_VERSION, entityId, entityVersion);
        if (entities == null || entities.size() <= 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        ServiceSpecificationEntity entity = entities.get(0);
        getReferencedEntities(entity, depth);

        QueryParameterParser queryParameterParser = new QueryParameterParser(uriInfo.getRequestUri().getQuery());
        Set<String> outputFields = getFieldSet(queryParameterParser);

        if (outputFields.isEmpty() || outputFields.contains(ServiceConstants.ALL_FIELDS)) {
            return Response.ok(entity).build();
        }

        Object outputEntity = selectFields(entity, outputFields);
        return Response.ok(outputEntity).build();
    }

}
