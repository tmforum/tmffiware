package org.tmf.dsmapi.usage;

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
import org.tmf.dsmapi.usage.model.Usage;
import org.tmf.dsmapi.usage.UsageFacade;
import org.tmf.dsmapi.usage.event.UsageEvent;
import org.tmf.dsmapi.usage.event.UsageEventFacade;
import org.tmf.dsmapi.usage.event.UsageEventPublisherLocal;
import org.tmf.dsmapi.usage.model.RatedProductUsage;
import org.tmf.dsmapi.usage.model.Reference;
import org.tmf.dsmapi.usage.model.RelatedParty;
import org.tmf.dsmapi.usage.model.Status;
import org.tmf.dsmapi.usage.model.UsageCharacteristic;
import org.tmf.dsmapi.usage.model.UsageSpecification;

@Stateless
@Path("admin/usage")
public class UsageAdminResource {

    @EJB
    UsageFacade usageFacade;
    @EJB
    UsageEventFacade eventFacade;
//    @EJB
//    UsageEventPublisherLocal publisher;

    @GET
    @Produces({"application/json"})
    public List<Usage> findAll() {
        return usageFacade.findAll();
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
    public Response post(List<Usage> entities, @Context UriInfo info) throws UnknownResourceException {

        if (entities == null) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
        }

        int previousRows = usageFacade.count();
        int affectedRows=0;

        // Try to persist entities
        try {
            for (Usage entitie : entities) {
                usageFacade.checkCreation(entitie);
                usageFacade.create(entitie);
                entitie.setHref(info.getAbsolutePath() + "/" + Long.toString(entitie.getId()));
                usageFacade.edit(entitie);
                affectedRows = affectedRows + 1;
//                publisher.createNotification(entitie, new Date());
            }
        } catch (BadUsageException e) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
        }

        Report stat = new Report(usageFacade.count());
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
    public Response update(@PathParam("id") long id, Usage entity) throws UnknownResourceException {
        Response response = null;
        Usage usage = usageFacade.find(id);
        if (usage != null) {
            entity.setId(id);
            usageFacade.edit(entity);
//            publisher.updateNotification(entity, new Date());
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
        int previousRows = usageFacade.count();
        usageFacade.removeAll();
        List<Usage> pis = usageFacade.findAll();
        for (Usage pi : pis) {
            delete(pi.getId());
        }

        int currentRows = usageFacade.count();
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
        int previousRows = usageFacade.count();
        Usage entity = usageFacade.find(id);

        // Event deletion
//        publisher.deletionNotification(entity, new Date());
        try {
            //Pause for 4 seconds to finish notification
            Thread.sleep(4000);
        } catch (InterruptedException ex) {
            Logger.getLogger(UsageAdminResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        // remove event(s) binding to the resource
        List<UsageEvent> events = eventFacade.findAll();
        for (UsageEvent event : events) {
            if (event.getResource().getId().equals(id)) {
                eventFacade.remove(event.getId());
            }
        }
        //remove resource
        usageFacade.remove(id);

        int affectedRows = 1;
        Report stat = new Report(usageFacade.count());
        stat.setAffectedRows(affectedRows);
        stat.setPreviousRows(previousRows);

        // 200 
        Response response = Response.ok(stat).build();
        return response;
    }

    @GET
    @Produces({"application/json"})
    @Path("event")
    public List<UsageEvent> findAllEvents() {
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
        List<UsageEvent> events = eventFacade.findAll();
        for (UsageEvent event : events) {
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
        return new Report(usageFacade.count());
    }

    @GET
    @Produces({"application/json"})
    @Path("proto")
    public Usage proto() {
        Usage usage = new Usage();
        usage.setId(new Long(1));
        usage.setHref("http://serverLocalisation:port/DSUsageManagement/api/usageManagement/v2/usage/1");
        usage.setDate(new Date());
        usage.setType("VOICE");
        usage.setDescription("Description for individual usage content");
        usage.setStatus(Status.Rated);

        List<Reference> l_usageSpecification = new ArrayList<Reference>();
        Reference usageSpecification = new Reference();
        usageSpecification.setId("22");
        usageSpecification.setHref("http://serverlocation:port/usageManagement/usageSpecification/22");
        usageSpecification.setName("Voice usage specification");
        l_usageSpecification.add(usageSpecification);
        usage.setUsageSpecification(usageSpecification);

        List<UsageCharacteristic> l_usageCharacteristic = new ArrayList<UsageCharacteristic>();
        UsageCharacteristic usageCharacteristic = new UsageCharacteristic();
        usageCharacteristic.setName("originatingCountryCode");
        usageCharacteristic.setValue("43");
        l_usageCharacteristic.add(usageCharacteristic);
        usageCharacteristic = new UsageCharacteristic();
        usageCharacteristic.setName("originatingNumber");
        usageCharacteristic.setValue("676123456789");
        l_usageCharacteristic.add(usageCharacteristic);
        usageCharacteristic = new UsageCharacteristic();
        usageCharacteristic.setName("destinationCountryCode");
        usageCharacteristic.setValue("49");
        l_usageCharacteristic.add(usageCharacteristic);
        usageCharacteristic = new UsageCharacteristic();
        usageCharacteristic.setName("destinationNumber");
        usageCharacteristic.setValue("170123456789");
        l_usageCharacteristic.add(usageCharacteristic);
        usageCharacteristic = new UsageCharacteristic();
        usageCharacteristic.setName("duration");
        usageCharacteristic.setValue("20");
        l_usageCharacteristic.add(usageCharacteristic);
        usageCharacteristic = new UsageCharacteristic();
        usageCharacteristic.setName("unit");
        usageCharacteristic.setValue("SEC");
        l_usageCharacteristic.add(usageCharacteristic);
        usageCharacteristic = new UsageCharacteristic();
        usageCharacteristic.setName("startDateTime");
        usageCharacteristic.setValue("2013-04-19T16:42:23-04:00");
        l_usageCharacteristic.add(usageCharacteristic);
        usageCharacteristic = new UsageCharacteristic();
        usageCharacteristic.setName("endDateTime");
        usageCharacteristic.setValue("2099-01-01T01:00:00-04:00");
        l_usageCharacteristic.add(usageCharacteristic);

        usage.setUsageCharacteristic(l_usageCharacteristic);

        List<RelatedParty> l_relatedParty = new ArrayList<RelatedParty>();
        RelatedParty relatedParty = new RelatedParty();
        relatedParty.setId("1");
        relatedParty.setHref("http://serverlocation:port/partyManagement/organization/1");
        relatedParty.setRole("serviceProvider");
        l_relatedParty.add(relatedParty);
        relatedParty.setId("45");
        relatedParty.setHref("http://serverlocation:port/partyManagement/individual/45");
        relatedParty.setRole("customer");
        l_relatedParty.add(relatedParty);

        usage.setRelatedParty(l_relatedParty);

        List<RatedProductUsage> l_ratedProductUsage = new ArrayList<RatedProductUsage>();
        RatedProductUsage ratedProductUsage = new RatedProductUsage();
        GregorianCalendar gc = new GregorianCalendar(2014, 04, 19, 16, 00, 00);
        ratedProductUsage.setRatingDate(gc.getTime());
        ratedProductUsage.setUsageRatingTag("Usage");
        ratedProductUsage.setRatingAmountType("Total");
        ratedProductUsage.setTaxExcludedRatingAmount(new Float(12.00));
        ratedProductUsage.setTaxIncludedRatingAmount(new Float(10.00));
        ratedProductUsage.setTaxRate(new Float(20.00));
        ratedProductUsage.setIsTaxExempt(Boolean.FALSE);
        ratedProductUsage.setOfferTariffType("Normal");
        ratedProductUsage.setBucketValueConvertedInAmount(new Float(0.0));
        ratedProductUsage.setCurrencyCode("EUR");
        l_ratedProductUsage.add(ratedProductUsage);

        usage.setRatedProductUsage(l_ratedProductUsage);

        return usage;
    }
}
