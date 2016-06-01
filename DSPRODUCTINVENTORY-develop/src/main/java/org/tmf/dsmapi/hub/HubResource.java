package org.tmf.dsmapi.hub;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.tmf.dsmapi.commons.exceptions.BadUsageException;
import org.tmf.dsmapi.commons.exceptions.UnknownResourceException;
import org.tmf.dsmapi.commons.jaxrs.Report;
import org.tmf.dsmapi.product.event.ProductEvent;
import org.tmf.dsmapi.product.event.ProductEventTypeEnum;
import org.tmf.dsmapi.product.model.BillingAccount;
import org.tmf.dsmapi.product.model.Price;
import org.tmf.dsmapi.product.model.Product;
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
@Path("/productInventory/v2/hub")
public class HubResource {

    @EJB
    HubFacade hubFacade;

    public HubResource() {
    }

    @POST
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response create(Hub entity) throws BadUsageException {
        entity.setId(null);
        hubFacade.create(entity);
        //201
        return Response.status(Response.Status.CREATED).entity(entity).build();
    }

    @DELETE
    public Report deleteAllHub() {

        int previousRows = hubFacade.count();
        hubFacade.removeAll();
        int currentRows = hubFacade.count();
        int affectedRows = previousRows - currentRows;

        Report stat = new Report(currentRows);
        stat.setAffectedRows(affectedRows);
        stat.setPreviousRows(previousRows);

        return stat;
    }

    @DELETE
    @Path("{id}")
    public Response remove(@PathParam("id") String id) throws UnknownResourceException {
        Hub hub = hubFacade.find(id);
        if (null == hub) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            hubFacade.remove(id);
            // 204
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }

    @GET
    @Produces({"application/json"})
    public List<Hub> findAll() {
        return hubFacade.findAll();
    }

    @GET
    @Produces({"application/json"})
    @Path("proto/product/event")
    public ProductEvent protoproductevent() {
        ProductEvent event = new ProductEvent();
        ProductEventTypeEnum x = ProductEventTypeEnum.ProductCreationNotification;
        event.setEventType(x);
        event.setEventTime(new Date());
        event.setId("42");
        
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
        gc.set(2014, 05, 15);
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
        
        event.setResource(product);
        
        return event;
        
    }
}
