package org.tmf.dsmapi.catalog.service.productOffering;

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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.tmf.dsmapi.catalog.entity.catalog.CatalogEntity;
import org.tmf.dsmapi.catalog.entity.product.ProductOfferingEntity;
import org.tmf.dsmapi.catalog.exception.IllegalLifecycleStatusException;
import org.tmf.dsmapi.catalog.hub.service.productOffering.ProductOfferingEventPublisherLocal;
import org.tmf.dsmapi.catalog.resource.LifecycleStatus;
import org.tmf.dsmapi.catalog.resource.product.ProductOffering;
import org.tmf.dsmapi.catalog.service.AbstractFacadeREST;
import org.tmf.dsmapi.catalog.service.ServiceConstants;
import org.tmf.dsmapi.catalog.service.catalog.CatalogFacade;
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
@Path("catalog/{catalogId}/productOffering")
public class ProductOfferingInCatalogIdFacadeREST extends AbstractFacadeREST<ProductOfferingEntity> {
    private static final Logger logger = Logger.getLogger(ProductOffering.class.getName());

    @EJB
    private ProductOfferingFacade manager;
    
     @EJB
    ProductOfferingEventPublisherLocal publisher;
     
      @EJB
    CatalogFacade cmanager;

    /*
     *
     */
    public ProductOfferingInCatalogIdFacadeREST() {
        super(ProductOfferingEntity.class);
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
    public Response create(@PathParam("catalogId") String catalogId, ProductOfferingEntity input, @Context UriInfo uriInfo) 
    throws IllegalLifecycleStatusException
    {
        logger.log(Level.FINE, "ProductOfferingInCatalogIdFacadeREST:create(catalogId: {0})", catalogId);
        System.out.println("URIINFO=" + uriInfo.getPath());
        
        List<CatalogEntity> centities = cmanager.findCatalogById(catalogId, null);
        if (centities == null || centities.size() <= 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

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
        input.setCatalogId(catalogId);
        manager.create(input);
        String href = buildHref(uriInfo, input.getId(), input.getParsedVersion());
        String rhref = href.replaceFirst("productOffering", "catalog/"+catalogId+ "/productOffering" );

        input.setHref(rhref);
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
    public Response update(@PathParam("catalogId") String catalogId, @PathParam("entityId") String entityId, ProductOfferingEntity input, @Context UriInfo uriInfo) {
        logger.log(Level.FINE, "ProductOfferingInCatalogIdFacadeREST:update(catalogId: {0}, entityId: {1})", new Object[]{catalogId, entityId});

        return Response.status(Response.Status.FORBIDDEN).build();
    }

    /*
     *
     */
    @PUT
    @Path("{entityId}:({entityVersion})")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response update(@PathParam("catalogId") String catalogId, @PathParam("entityId") String entityId, @PathParam("entityVersion") ParsedVersion entityVersion, ProductOfferingEntity input, @Context UriInfo uriInfo) {
        logger.log(Level.FINE, "ProductOfferingInCatalogIdFacadeREST:update(catalogId: {0}, entityId: {1}, entityVersion: {2})", new Object[]{catalogId, entityId, entityVersion});

        return Response.status(Response.Status.FORBIDDEN).build();
    }

    /*
     *
     */
    @PATCH
    @Path("{entityId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response edit(@PathParam("catalogId") String catalogId, @PathParam("entityId") String entityId, ProductOfferingEntity input, @Context UriInfo uriInfo) throws IllegalLifecycleStatusException {
        logger.log(Level.FINE, "ProductOfferingInCatalogIdFacadeREST:edit(catalogId: {0}, entityId: {1})", new Object[]{catalogId, entityId});
         List<CatalogEntity> centities = cmanager.findCatalogById(catalogId, null);
        if (centities == null || centities.size() <= 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return edit_(catalogId, entityId, null, input, uriInfo);
    }

    /*
     *
     */
    @PATCH
    @Path("{entityId}:({entityVersion})")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response edit(@PathParam("catalogId") String catalogId, @PathParam("entityId") String entityId, @PathParam("entityVersion") ParsedVersion entityVersion, ProductOfferingEntity input, @Context UriInfo uriInfo) throws IllegalLifecycleStatusException {
        logger.log(Level.FINE, "ProductOfferingInCatalogIdFacadeREST:edit(catalogId: {0}, entityId: {1}, entityVersion: {2})", new Object[]{catalogId, entityId, entityVersion});
        List<CatalogEntity> centities = cmanager.findCatalogById(catalogId, null);
        if (centities == null || centities.size() <= 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return edit_(catalogId, entityId, null, input, uriInfo);
    }

    /*
     *
     */
    @DELETE
    @Path("{entityId}")
    public Response remove(@PathParam("catalogId") String catalogId, @PathParam("entityId") String entityId) {
        logger.log(Level.FINE, "ProductOfferingInCatalogIdFacadeREST:remove(catalogId: {0}, entityId: {1})", new Object[]{catalogId, entityId});
        List<CatalogEntity> centities = cmanager.findCatalogById(catalogId, null);
        if (centities == null || centities.size() <= 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return remove_(catalogId,entityId, null);
    }

    /*
     *
     */
    @DELETE
    @Path("{entityId}:({entityVersion})")
    public Response remove(@PathParam("catalogId") String catalogId, @PathParam("entityId") String entityId, @PathParam("entityVersion") ParsedVersion entityVersion) {
        logger.log(Level.FINE, "ProductOfferingInCatalogIdFacadeREST:remove(catalogId: {0}, entityId: {1}, entityVersion: {2})", new Object[]{catalogId, entityId, entityVersion});
        List<CatalogEntity> centities = cmanager.findCatalogById(catalogId, null);
        if (centities == null || centities.size() <= 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return remove_(catalogId,entityId, null);
    }

    /*
     *
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response find(@PathParam("catalogId") String catalogId, @QueryParam("depth") int depth, @Context UriInfo uriInfo) throws BadUsageException {
        logger.log(Level.FINE, "ProductOfferingInCatalogIdFacadeREST:find(catalogId: {0}, depth: {1})", new Object[]{catalogId, depth});

        List<CatalogEntity> centities = cmanager.findCatalogById(catalogId, null);
        if (centities == null || centities.size() <= 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        QueryParameterParser queryParameterParser = new QueryParameterParser(uriInfo.getRequestUri().getQuery());
        MultivaluedMap<String, String> tagsWithValue = queryParameterParser.getTagsWithValue();
        tagsWithValue.add("catalogId", catalogId);
        // Remove known parameters before running the query.
        Set<String> outputFields = getFieldSet(queryParameterParser);
        queryParameterParser.removeTagWithValues("depth");

        Set<ProductOfferingEntity> entities = manager.find(queryParameterParser.getTagsWithValue());
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
    public Response findById(@PathParam("catalogId") String catalogId, @PathParam("entityId") String entityId, @QueryParam("depth") int depth, @Context UriInfo uriInfo) {
        logger.log(Level.FINE, "ProductOfferingInCatalogIdFacadeREST:find(catalogId: {0}, entityId: {1}, depth: {2})", new Object[]{catalogId, entityId, depth});
        List<CatalogEntity> centities = cmanager.findCatalogById(catalogId, null);
        if (centities == null || centities.size() <= 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return find_(catalogId,entityId, null, depth, uriInfo);
    }

    /*
     *
     */
    @GET
    @Path("{entityId}:({entityVersion})")
    @Produces({MediaType.APPLICATION_JSON})
    public Response find(@PathParam("catalogId") String catalogId, @PathParam("entityId") String entityId, @PathParam("entityVersion") ParsedVersion entityVersion, @QueryParam("depth") int depth, @Context UriInfo uriInfo) {
        logger.log(Level.FINE, "ProductOfferingInCatalogIdFacadeREST:find(catalogId: {0}, entityId: {1}, entityVersion: {2}, depth: {3})", new Object[]{catalogId, entityId, entityVersion, depth});
        List<CatalogEntity> centities = cmanager.findCatalogById(catalogId, null);
        if (centities == null || centities.size() <= 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
         return find_(catalogId,entityId, null, depth, uriInfo);
    }

    /*
     *
     */
    @GET
    @Path("admin/proto")
    @Produces({MediaType.APPLICATION_JSON})
    public Response proto(@PathParam("catalogId") String catalogId) {
        logger.log(Level.FINE, "ProductOfferingInCatalogIdFacadeREST:proto(catalogId: {0})", catalogId);

        return Response.ok(ProductOfferingEntity.createProto()).build();
    }

    /*
     *
     */
    @GET
    @Path("admin/count")
    @Produces({MediaType.TEXT_PLAIN})
    public Response count(@PathParam("catalogId") String catalogId) {
        logger.log(Level.FINE, "ProductOfferingInCatalogIdFacadeREST:count(catalogId: {0})", catalogId);

        return Response.status(Response.Status.FORBIDDEN).build();
    }
    
      /*
     *
     */
    private Response update_(String catalogId, String entityId, ParsedVersion entityVersion, ProductOfferingEntity input, UriInfo uriInfo) throws IllegalLifecycleStatusException {
        logger.log(Level.FINE, "ProductOfferingFacadeREST:update_(entityId: {0}, entityVersion: {1})", new Object[]{entityId, entityVersion});

        if (input == null) {
            logger.log(Level.FINE, "input is required.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (input.isValid() == false) {
            logger.log(Level.FINE, "invalid input.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        List<ProductOfferingEntity> entities = manager.findById(catalogId, ParsedVersion.ROOT_CATALOG_VERSION, entityId, entityVersion);
        ProductOfferingEntity entity = (entities != null && entities.size() > 0) ? entities.get(0) : null;
        if (entity == null) {
            logger.log(Level.FINE, "requested ProductOffering [{0}, {1}] not found", new Object[]{entityId, entityVersion});
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        validateLifecycleStatus(input, entity);

        input.configureCatalogIdentifier();
        input.setCatalogId(catalogId);

        if (input.keysMatch(entity)) {
            
            
        String href = buildHref(uriInfo, input.getId(), input.getParsedVersion());
        String rhref = href.replaceFirst("productOffering", "catalog/"+catalogId+ "/productOffering" );
        input.setHref(rhref);
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

       
        String href = buildHref(uriInfo, input.getId(), input.getParsedVersion());
        String rhref = href.replaceFirst("productOffering", "catalog/"+catalogId+ "/productOffering" );
        input.setHref(rhref);
        
        manager.edit(input);

        publisher.updateNotification(input, null, null);
        return Response.status(Response.Status.CREATED).entity(input).build();
    }

    /*
     *
     */
    private Response edit_(String catalogId, String entityId, ParsedVersion entityVersion, ProductOfferingEntity input, UriInfo uriInfo) throws IllegalLifecycleStatusException {
          logger.log(Level.FINE, "ProductOfferingFacadeREST:edit_(entityId: {0}, entityVersion: {1})", new Object[]{entityId, entityVersion});

        if (input == null) {
            logger.log(Level.FINE, "input is required");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        List<ProductOfferingEntity> entities = manager.findById(catalogId, ParsedVersion.ROOT_CATALOG_VERSION, entityId, entityVersion);
        ProductOfferingEntity entity = (entities != null && entities.size() > 0) ? entities.get(0) : null;
        if (entity == null) {
            logger.log(Level.FINE, "requested ProductOffering [{0}, {1}] not found", new Object[]{entityId, entityVersion});
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        input.edit(entity);
        input.setCatalogId(entity.getCatalogId());
        input.setCatalogVersion(entity.getCatalogVersion());
        input.setId(entity.getId());

        if(input.isValid() == false) {
            logger.log(Level.FINE, "patched ProductOffering would be invalid");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        validateLifecycleStatus(input, entity);

        if (input.getVersion() == null) {
            input.setVersion(entity.getVersion());
            String href = buildHref(uriInfo, input.getId(), input.getParsedVersion());
        String rhref = href.replaceFirst("productOffering", "catalog/"+catalogId+ "/productOffering" );
        input.setHref(rhref);
            manager.edit(input);
            
            publisher.valueChangedNotification(input, null, null);
            return Response.status(Response.Status.CREATED).entity(entity).build();
        }

        if (input.hasHigherVersionThan(entity) == false) {
            logger.log(Level.FINE, "specified version ({0}) must be higher than entity version ({1})", new Object[]{input.getVersion(), entity.getVersion()});
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        manager.remove(entity);

       String href = buildHref(uriInfo, input.getId(), input.getParsedVersion());
        String rhref = href.replaceFirst("productOffering", "catalog/"+catalogId+ "/productOffering" );
        input.setHref(rhref);
        manager.create(input);

        publisher.valueChangedNotification(input, null, null);
        return Response.status(Response.Status.CREATED).entity(input).build();
    }

    /*
     *
     */
    private Response remove_(String catalogId, String entityId, ParsedVersion entityVersion) {
        logger.log(Level.FINE, "ProductOfferingFacadeREST:remove_(entityId: {0}, entityVersion: {1})", new Object[]{entityId, entityVersion});

        List<ProductOfferingEntity> entities = manager.findById(catalogId, ParsedVersion.ROOT_CATALOG_VERSION, entityId, entityVersion);
        if (entities == null || entities.size() <= 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        ProductOfferingEntity entity = entities.get(0);
        manager.remove(entity);
 
//        publisher.deletionNotification(entity, null, null);
        return Response.ok().build();
    }

    /*
     *
     */
    private Response find_(String CatalogId, String entityId, ParsedVersion entityVersion, int depth, UriInfo uriInfo) {
        logger.log(Level.FINE, "ProductOfferingFacadeREST:find_(entityId: {0}, entityVersion: {1}, depth: {2})", new Object[]{entityId, entityVersion, depth});

        List<ProductOfferingEntity> entities = manager.findById(CatalogId, ParsedVersion.ROOT_CATALOG_VERSION, entityId, entityVersion);
        if (entities == null || entities.size() <= 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        ProductOfferingEntity entity = entities.get(0);
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
