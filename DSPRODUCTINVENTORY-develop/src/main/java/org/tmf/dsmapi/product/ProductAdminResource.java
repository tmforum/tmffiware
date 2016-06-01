package org.tmf.dsmapi.product;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.tmf.dsmapi.commons.exceptions.BadUsageException;
import org.tmf.dsmapi.commons.exceptions.UnknownResourceException;
import org.tmf.dsmapi.commons.jaxrs.Report;
import org.tmf.dsmapi.product.model.Product;
import org.tmf.dsmapi.product.event.ProductEvent;
import org.tmf.dsmapi.product.event.ProductEventFacade;
import org.tmf.dsmapi.product.event.ProductEventPublisherLocal;
import org.tmf.dsmapi.product.model.BillingAccount;
import org.tmf.dsmapi.product.model.Price;
import org.tmf.dsmapi.product.model.ProductCharacteristic;
import org.tmf.dsmapi.product.model.ProductOffering;
import org.tmf.dsmapi.product.model.ProductPrice;
import org.tmf.dsmapi.product.model.ProductRef;
import org.tmf.dsmapi.product.model.ProductRelationship;
import org.tmf.dsmapi.product.model.ProductSpecification;
import org.tmf.dsmapi.product.model.RealizingResource;
import org.tmf.dsmapi.product.model.RealizingService;
import org.tmf.dsmapi.product.model.RelatedParty;
import org.tmf.dsmapi.product.model.State;
import org.tmf.dsmapi.product.model.ValidFor;

@Stateless
@Path("admin/product")
public class ProductAdminResource {

    @EJB
    ProductFacade productInventoryFacade;
    @EJB
    ProductEventFacade eventFacade;
//    @EJB
//    ProductEventPublisherLocal publisher;

    @GET
    @Produces({"application/json"})
    public List<Product> findAll() {
        return productInventoryFacade.findAll();
    }

    /**
     *
     * For test purpose only
     *
     * @param entities
     * @return
     */
    @POST
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response post(List<Product> entities, @Context UriInfo info) throws UnknownResourceException {

        if (entities == null) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
        }

        int previousRows = productInventoryFacade.count();
        int affectedRows=0;

        // Try to persist entities
        try {
            for (Product entitie : entities) {
                productInventoryFacade.checkCreation(entitie);
                productInventoryFacade.create(entitie);
                entitie.setHref(info.getAbsolutePath() + "/" + Long.toString(entitie.getId()));
                productInventoryFacade.edit(entitie);
                affectedRows = affectedRows + 1;
//                publisher.createNotification(entitie, new Date());
            }
        } catch (BadUsageException e) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
        }

        Report stat = new Report(productInventoryFacade.count());
        stat.setAffectedRows(affectedRows);
        stat.setPreviousRows(previousRows);

        // 201 OK
        return Response.created(null).
                entity(stat).
                build();
    }

    @PUT
    @Path("{id}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response update(@PathParam("id") long id, Product entity) throws UnknownResourceException {
        Response response = null;
        Product productInventory = productInventoryFacade.find(id);
        if (productInventory != null) {
            entity.setId(id);
            productInventoryFacade.edit(entity);
//            publisher.valueChangedNotification(entity, new Date());
            // 200 OK + location
            response = Response.status(Response.Status.OK).entity(entity).build();

        } else {
            // 404 not found
            response = Response.status(Response.Status.NOT_FOUND).build();
        }
        return response;
    }

    /**
     *
     * For test purpose only
     *
     * @return
     * @throws org.tmf.dsmapi.commons.exceptions.UnknownResourceException
     */
    @DELETE
    public Report deleteAll() throws UnknownResourceException {

        eventFacade.removeAll();
        int previousRows = productInventoryFacade.count();
        productInventoryFacade.removeAll();
        List<Product> pis = productInventoryFacade.findAll();
        for (Product pi : pis) {
            delete(pi.getId());
        }

        int currentRows = productInventoryFacade.count();
        int affectedRows = previousRows - currentRows;

        Report stat = new Report(currentRows);
        stat.setAffectedRows(affectedRows);
        stat.setPreviousRows(previousRows);

        return stat;
    }

    /**
     *
     * For test purpose only
     *
     * @param id
     * @return
     * @throws UnknownResourceException
     */
    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") Long id) throws UnknownResourceException {
        int previousRows = productInventoryFacade.count();
        Product entity = productInventoryFacade.find(id);

        // Event deletion
//        publisher.deletionNotification(entity, new Date());
        try {
            //Pause for 4 seconds to finish notification
            Thread.sleep(4000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ProductAdminResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        // remove event(s) binding to the resource
        List<ProductEvent> events = eventFacade.findAll();
        for (ProductEvent event : events) {
            if (event.getResource().getId().equals(id)) {
                eventFacade.remove(event.getId());
            }
        }
        //remove resource
        productInventoryFacade.remove(id);

        int affectedRows = 1;
        Report stat = new Report(productInventoryFacade.count());
        stat.setAffectedRows(affectedRows);
        stat.setPreviousRows(previousRows);

        // 200 
        Response response = Response.ok(stat).build();
        return response;
    }

    @GET
    @Produces({"application/json"})
    @Path("event")
    public List<ProductEvent> findAllEvents() {
        return eventFacade.findAll();
    }

    @DELETE
    @Path("event")
    public Report deleteAllEvent() {

        int previousRows = eventFacade.count();
        eventFacade.removeAll();
        int currentRows = eventFacade.count();
        int affectedRows = previousRows - currentRows;

        Report stat = new Report(currentRows);
        stat.setAffectedRows(affectedRows);
        stat.setPreviousRows(previousRows);

        return stat;
    }

    @DELETE
    @Path("event/{id}")
    public Response deleteEvent(@PathParam("id") String id) throws UnknownResourceException {

        int previousRows = eventFacade.count();
        List<ProductEvent> events = eventFacade.findAll();
        for (ProductEvent event : events) {
            if (event.getResource().getId().equals(id)) {
                eventFacade.remove(event.getId());

            }
        }
        int currentRows = eventFacade.count();
        int affectedRows = previousRows - currentRows;

        Report stat = new Report(currentRows);
        stat.setAffectedRows(affectedRows);
        stat.setPreviousRows(previousRows);

        // 200 
        Response response = Response.ok(stat).build();
        return response;
    }

    /**
     *
     * @return
     */
    @GET
    @Path("count")
    @Produces({"application/json"})
    public Report count() {
        return new Report(productInventoryFacade.count());
    }

    @GET
    @Produces({"application/json"})
    @Path("proto")
    public Product proto() {
        Product product = new Product();
        product.setId(new Long(123));
        product.setHref("http://serverLocalisation:port/DSProductInventory/api/productInventory/v2/product/123");
        product.setName("Broadband");
        product.setDescription("Description of the instantiated broadband product");
        product.setStatus(State.Active);
        product.setIsCustomerVisible("true");
        product.setIsBundle("true");
        product.setProductSerialNumber("Not useful in this case, we are describing a service…");
        
        GregorianCalendar gc = new GregorianCalendar();
        gc.set(2015, 05, 15);
        product.setStartDate(gc.getTime());
        gc.add(GregorianCalendar.MINUTE, 5);
        product.setOrderDate(gc.getTime());
        product.setTerminationDate(null);
        product.setPlace("productPlace");
        
        ProductOffering po = new ProductOffering();
        po.setId("4");
        po.setHref("http://serverlocation:port/catalogApi/productOffering/4");
        po.setName("My Quick BB Offer");
        product.setProductOffering(po);
        
        ProductSpecification ps = new ProductSpecification();
        ps.setId("42");
        ps.setHref("http://serverlocation:port/catalogApi/productSpecification/42");
        product.setProductSpecification(ps);
        
        List<ProductCharacteristic> l_pc = new ArrayList<ProductCharacteristic>();
        ProductCharacteristic pc = new ProductCharacteristic();
        pc.setName("speed");
        pc.setValue("16M");
        l_pc.add(pc);
        product.setProductCharacteristic(l_pc);
        
        List<ProductRelationship> l_prs = new ArrayList<ProductRelationship>();
        ProductRelationship prs = new ProductRelationship();
        prs.setType("contains");
        ProductRef ref = new ProductRef();
        ref.setHref("http://serverlocation:port/inventoryApi/product/59");
        prs.setProduct(ref);
        l_prs.add(prs);
        product.setProductRelationship(l_prs);
        
        List<BillingAccount> l_ba = new ArrayList<BillingAccount>();
        BillingAccount ba = new BillingAccount();
        ba.setId("678");
        ba.setHref("http://serverlocation:port/billingApi/billingAccount/678");
        ba.setName("account name");
        l_ba.add(ba);
        product.setBillingAccount(l_ba);
        
        List<RelatedParty> l_rp = new ArrayList<RelatedParty>();
        RelatedParty rp = new RelatedParty();
        rp.setId("42");
        rp.setHref("http://serverlocation:port/partnerManagement/partner/42");
        rp.setRole("partner");
        l_rp.add(rp);
        product.setRelatedParty(l_rp);
        
        List<RealizingResource> l_realresource = new ArrayList<RealizingResource>();
        RealizingResource realResource = new RealizingResource();
        realResource.setId("not useful in this case, we are describing a service");
        realResource.setHref("not useful in this case, we are describing a service");
        l_realresource.add(realResource);
        product.setRealizingResource(l_realresource);
        
        List<RealizingService> l_realService = new ArrayList<RealizingService>();
        RealizingService realService = new RealizingService();
        realService.setId("46779");
        realService.setHref("http://serverlocation:port/inventoryApi/service/46779");
        l_realService.add(realService);
        product.setRealizingService(l_realService);
        
        List<ProductPrice> l_pp = new ArrayList<ProductPrice>();
        ProductPrice pp = new ProductPrice();
        pp.setName("productPrice Name");
        pp.setDescription("productPrice Description");
        pp.setPriceType("recurring");
        pp.setRecurringChargePeriod("monthly");
        pp.setUnitOfMeasure("");
        ValidFor validFor = new ValidFor();
        gc.set(2014, 05, 15);
        validFor.setStartDateTime(gc.getTime());
        gc.set(2017, 05, 15);
        validFor.setEndDateTime(gc.getTime());
        pp.setValidFor(validFor);
        Price price = new Price();
        price.setAmount(new Float("12"));
        price.setCurrency("$");
        pp.setPrice(price);
        l_pp.add(pp);
        product.setProductPrice(l_pp);
                
        return product;
    }
}
