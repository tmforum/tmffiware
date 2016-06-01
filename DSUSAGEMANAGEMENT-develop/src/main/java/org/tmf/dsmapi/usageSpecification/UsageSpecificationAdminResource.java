package org.tmf.dsmapi.usageSpecification;

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
import org.tmf.dsmapi.usage.model.UsageCharacteristic;
import org.tmf.dsmapi.usage.model.UsageSpecCharacteristic;
import org.tmf.dsmapi.usage.model.UsageSpecCharacteristicValue;
import org.tmf.dsmapi.usage.model.UsageSpecification;
import org.tmf.dsmapi.usage.model.ValidFor;
import org.tmf.dsmapi.usageSpecification.UsageSpecificationFacade;
import org.tmf.dsmapi.usageSpecification.event.UsageSpecificationEvent;
import org.tmf.dsmapi.usageSpecification.event.UsageSpecificationEventFacade;
import org.tmf.dsmapi.usageSpecification.event.UsageSpecificationEventPublisherLocal;
import sun.util.calendar.Gregorian;

@Stateless
@Path("admin/usageSpecification")
public class UsageSpecificationAdminResource {

    @EJB
    UsageSpecificationFacade usageSpecificationFacade;
    @EJB
    UsageSpecificationEventFacade eventFacade;
//    @EJB
//    UsageSpecificationEventPublisherLocal publisher;

    @GET
    @Produces({"application/json"})
    public List<UsageSpecification> findAll() {
        return usageSpecificationFacade.findAll();
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
    public Response post(List<UsageSpecification> entities, @Context UriInfo info) throws UnknownResourceException {

        if (entities == null) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
        }

        int previousRows = usageSpecificationFacade.count();
        int affectedRows=0;

        // Try to persist entities
        try {
            for (UsageSpecification entitie : entities) {
                usageSpecificationFacade.checkCreation(entitie);
                usageSpecificationFacade.create(entitie);
                entitie.setHref(info.getAbsolutePath() + "/" + Long.toString(entitie.getId()));
                usageSpecificationFacade.edit(entitie);
                affectedRows = affectedRows + 1;
//                publisher.createNotification(entitie, new Date());
            }
        } catch (BadUsageException e) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
        }

        Report stat = new Report(usageSpecificationFacade.count());
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
    public Response update(@PathParam("id") long id, UsageSpecification entity) throws UnknownResourceException {
        Response response = null;
        UsageSpecification usageSpecification = usageSpecificationFacade.find(id);
        if (usageSpecification != null) {
            entity.setId(id);
            usageSpecificationFacade.edit(entity);
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
        int previousRows = usageSpecificationFacade.count();
        usageSpecificationFacade.removeAll();
        List<UsageSpecification> pis = usageSpecificationFacade.findAll();
        for (UsageSpecification pi : pis) {
            delete(pi.getId());
        }

        int currentRows = usageSpecificationFacade.count();
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
        int previousRows = usageSpecificationFacade.count();
        UsageSpecification entity = usageSpecificationFacade.find(id);

        // Event deletion
//        publisher.deletionNotification(entity, new Date());
        try {
            //Pause for 4 seconds to finish notification
            Thread.sleep(4000);
        } catch (InterruptedException ex) {
            Logger.getLogger(UsageSpecificationAdminResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        // remove event(s) binding to the resource
        List<UsageSpecificationEvent> events = eventFacade.findAll();
        for (UsageSpecificationEvent event : events) {
            if (event.getResource().getId().equals(id)) {
                eventFacade.remove(event.getId());
            }
        }
        //remove resource
        usageSpecificationFacade.remove(id);

        int affectedRows = 1;
        Report stat = new Report(usageSpecificationFacade.count());
        stat.setAffectedRows(affectedRows);
        stat.setPreviousRows(previousRows);

        // 200 
        Response response = Response.ok(stat).build();
        return response;
    }

    @GET
    @Produces({"application/json"})
    @Path("event")
    public List<UsageSpecificationEvent> findAllEvents() {
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
        List<UsageSpecificationEvent> events = eventFacade.findAll();
        for (UsageSpecificationEvent event : events) {
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
        return new Report(usageSpecificationFacade.count());
    }

    @GET
    @Produces({"application/json"})
    @Path("proto")
    public UsageSpecification proto() {
        UsageSpecification usageSpecification = new UsageSpecification();
        usageSpecification.setId(new Long(22));
        usageSpecification.setHref("http://serverlocation:port/usageManagement/usageSpecification/22");
        usageSpecification.setName("VoiceSpec");
        usageSpecification.setDescription("Spec for voice calls usage");

        ValidFor validFor = new ValidFor();
        GregorianCalendar gc = new GregorianCalendar(2015, 04, 30, 12, 00, 00);
        validFor.setStartDateTime(gc.getTime());
        gc = new GregorianCalendar(2099, 01, 12, 00, 00, 00);
        validFor.setEndDateTime(gc.getTime());
        usageSpecification.setValidFor(validFor);

        List<UsageSpecCharacteristic> l_usageSpecCharacteristic = new ArrayList<UsageSpecCharacteristic>();
        UsageSpecCharacteristic usageSpecCharacteristic = new UsageSpecCharacteristic();
        usageSpecCharacteristic.setName("originatingCountryCode");
        usageSpecCharacteristic.setConfigurable(Boolean.TRUE);
        usageSpecCharacteristic.setDescription("country code of the caller");

        List<UsageSpecCharacteristicValue> l_uscValue = new ArrayList<UsageSpecCharacteristicValue>();
        UsageSpecCharacteristicValue uscValue = new UsageSpecCharacteristicValue();
        uscValue.setValueType("number");
        uscValue.setValue("0123456789");
        uscValue.setDefault(Boolean.FALSE);
        l_uscValue.add(uscValue);
        usageSpecCharacteristic.setUsageSpecCharacteristicValue(l_uscValue);
        l_usageSpecCharacteristic.add(usageSpecCharacteristic);

        usageSpecCharacteristic = new UsageSpecCharacteristic();
        usageSpecCharacteristic.setName("originatingNumber");
        usageSpecCharacteristic.setConfigurable(Boolean.TRUE);
        usageSpecCharacteristic.setDescription("phone number of the caller");
        uscValue = new UsageSpecCharacteristicValue();
        uscValue.setValueType("number");
        uscValue.setValue("9876543210");
        uscValue.setDefault(Boolean.TRUE);
        l_uscValue.add(uscValue);
        usageSpecCharacteristic.setUsageSpecCharacteristicValue(l_uscValue);
        l_usageSpecCharacteristic.add(usageSpecCharacteristic);

        usageSpecCharacteristic = new UsageSpecCharacteristic();
        usageSpecCharacteristic.setName("destinationNumber");
        usageSpecCharacteristic.setConfigurable(Boolean.TRUE);
        usageSpecCharacteristic.setDescription("phone number of the called party");
        uscValue = new UsageSpecCharacteristicValue();
        uscValue.setValueType("number");
        uscValue.setValue("9999999999");
        uscValue.setDefault(Boolean.FALSE);
        l_uscValue.add(uscValue);
        usageSpecCharacteristic.setUsageSpecCharacteristicValue(l_uscValue);
        l_usageSpecCharacteristic.add(usageSpecCharacteristic);

        usageSpecCharacteristic = new UsageSpecCharacteristic();
        usageSpecCharacteristic.setName("duration");
        usageSpecCharacteristic.setConfigurable(Boolean.TRUE);
        usageSpecCharacteristic.setDescription("duration of the call");
        uscValue = new UsageSpecCharacteristicValue();
        uscValue.setValueType("number");
        uscValue.setValueFrom("0");
        uscValue.setDefault(Boolean.FALSE);
        l_uscValue.add(uscValue);
        usageSpecCharacteristic.setUsageSpecCharacteristicValue(l_uscValue);
        l_usageSpecCharacteristic.add(usageSpecCharacteristic);

        usageSpecCharacteristic = new UsageSpecCharacteristic();
        usageSpecCharacteristic.setName("unit");
        usageSpecCharacteristic.setConfigurable(Boolean.TRUE);
        usageSpecCharacteristic.setDescription("unit of the duration");
        uscValue = new UsageSpecCharacteristicValue();
        uscValue.setValueType("number");
        uscValue.setValue("SEC");
        uscValue.setDefault(Boolean.FALSE);
        l_uscValue.add(uscValue);
        usageSpecCharacteristic.setUsageSpecCharacteristicValue(l_uscValue);
        l_usageSpecCharacteristic.add(usageSpecCharacteristic);

        usageSpecCharacteristic = new UsageSpecCharacteristic();
        usageSpecCharacteristic.setName("startDateTime");
        usageSpecCharacteristic.setConfigurable(Boolean.TRUE);
        usageSpecCharacteristic.setDescription("startdate and starttime of the call");
        uscValue = new UsageSpecCharacteristicValue();
        uscValue.setValueType("dateTime");
        uscValue.setValue("2013-04-19T16:42:23-04:00");
        uscValue.setDefault(Boolean.FALSE);
        l_uscValue.add(uscValue);
        usageSpecCharacteristic.setUsageSpecCharacteristicValue(l_uscValue);
        l_usageSpecCharacteristic.add(usageSpecCharacteristic);

        usageSpecCharacteristic = new UsageSpecCharacteristic();
        usageSpecCharacteristic.setName("endDateTime");
        usageSpecCharacteristic.setConfigurable(Boolean.TRUE);
        usageSpecCharacteristic.setDescription("enddate and endtime of the call");
        uscValue = new UsageSpecCharacteristicValue();
        uscValue.setValueType("dateTime");
        uscValue.setValue("2013-04-19T18:30:25-04:00");
        uscValue.setDefault(Boolean.FALSE);
        l_uscValue.add(uscValue);
        usageSpecCharacteristic.setUsageSpecCharacteristicValue(l_uscValue);
        l_usageSpecCharacteristic.add(usageSpecCharacteristic);

        usageSpecification.setUsageSpecCharacteristic(l_usageSpecCharacteristic);

        return usageSpecification;
    }
}
