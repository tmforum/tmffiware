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
import org.tmf.dsmapi.usage.event.UsageEvent;
import org.tmf.dsmapi.usage.event.UsageEventTypeEnum;
import org.tmf.dsmapi.usage.model.RatedProductUsage;
import org.tmf.dsmapi.usage.model.Reference;
import org.tmf.dsmapi.usage.model.RelatedParty;
import org.tmf.dsmapi.usage.model.Status;
import org.tmf.dsmapi.usage.model.Usage;
import org.tmf.dsmapi.usage.model.UsageCharacteristic;
import org.tmf.dsmapi.usage.model.UsageSpecCharacteristic;
import org.tmf.dsmapi.usage.model.UsageSpecCharacteristicValue;
import org.tmf.dsmapi.usage.model.UsageSpecification;
import org.tmf.dsmapi.usage.model.ValidFor;
import org.tmf.dsmapi.usageSpecification.event.UsageSpecificationEvent;
import org.tmf.dsmapi.usageSpecification.event.UsageSpecificationEventTypeEnum;

@Stateless
@Path("/usageManagement/v2/hub")
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
    @Path("proto/usage/event")
    public UsageEvent usageevent() {
        UsageEvent event = new UsageEvent();
        UsageEventTypeEnum x = UsageEventTypeEnum.UsageCreateNotification;
        event.setEventType(x);
        event.setEventTime(new Date());
        event.setId("22");

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
        
        event.setResource(usage);
        
        return event;
    }

    @GET
    @Produces({"application/json"})
    @Path("proto/usageSpecification/event")
    public UsageSpecificationEvent usagespecificationevent() {
        UsageSpecificationEvent event = new UsageSpecificationEvent();
        UsageSpecificationEventTypeEnum x = UsageSpecificationEventTypeEnum.UsageSpecificationCreateNotification;
        event.setEventType(x);
        event.setEventTime(new Date());
        event.setId("32");

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
        l_uscValue = new ArrayList<UsageSpecCharacteristicValue>();
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
        l_uscValue = new ArrayList<UsageSpecCharacteristicValue>();
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
        l_uscValue = new ArrayList<UsageSpecCharacteristicValue>();
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
        l_uscValue = new ArrayList<UsageSpecCharacteristicValue>();
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
        l_uscValue = new ArrayList<UsageSpecCharacteristicValue>();
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
        l_uscValue = new ArrayList<UsageSpecCharacteristicValue>();
        uscValue = new UsageSpecCharacteristicValue();
        uscValue.setValueType("dateTime");
        uscValue.setValue("2013-04-19T18:30:25-04:00");
        uscValue.setDefault(Boolean.FALSE);
        l_uscValue.add(uscValue);
        usageSpecCharacteristic.setUsageSpecCharacteristicValue(l_uscValue);
        l_usageSpecCharacteristic.add(usageSpecCharacteristic);
        
        usageSpecification.setUsageSpecCharacteristic(l_usageSpecCharacteristic);  
        
        event.setResource(usageSpecification);
        
        return event;
    }
}
