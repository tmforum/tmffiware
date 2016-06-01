package org.tmf.dsmapi.usageSpecification.event;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.tmf.dsmapi.commons.exceptions.BadUsageException;
import org.tmf.dsmapi.usage.model.UsageSpecification;
import org.tmf.dsmapi.hub.Hub;
import org.tmf.dsmapi.hub.HubFacade;

/**
 *
 * Should be async or called with MDB
 */
@Stateless
@Asynchronous
public class UsageSpecificationEventPublisher implements UsageSpecificationEventPublisherLocal {

    @EJB
    HubFacade hubFacade;
    @EJB
    UsageSpecificationEventFacade eventFacade;
    @EJB
    UsageSpecificationRESTEventPublisherLocal restEventPublisherLocal;

    /** 
     * Add business logic below. (Right-click in editor and choose
     * "Insert Code > Add Business Method")
     * Access Hubs using callbacks and send to http publisher 
     *(pool should be configured around the RESTEventPublisher bean)
     * Loop into array of Hubs
     * Call RestEventPublisher - Need to implement resend policy plus eviction
     * Filtering is done in RestEventPublisher based on query expression
    */ 
    @Override
    public void publish(UsageSpecificationEvent event) {
        try {
            eventFacade.create(event);
        } catch (BadUsageException ex) {
            Logger.getLogger(UsageSpecificationEventPublisher.class.getName()).log(Level.SEVERE, null, ex);
        }

        List<Hub> hubList = hubFacade.findAll();
        Iterator<Hub> it = hubList.iterator();
        while (it.hasNext()) {
            Hub hub = it.next();
            restEventPublisherLocal.publish(hub, event);
        }
    }

    @Override
    public void createNotification(UsageSpecification bean, Date date) {
        UsageSpecificationEvent event = new UsageSpecificationEvent();
        event.setEventTime(date);
        event.setEventType(UsageSpecificationEventTypeEnum.UsageSpecificationCreateNotification);
        event.setResource(bean);
        publish(event);

    }

    @Override
    public void deletionNotification(UsageSpecification bean, Date date) {
        UsageSpecificationEvent event = new UsageSpecificationEvent();
        event.setEventTime(date);
        event.setEventType(UsageSpecificationEventTypeEnum.UsageSpecificationDeleteNotification);
        event.setResource(bean);
        publish(event);
    }
	
    @Override
    public void updateNotification(UsageSpecification bean, Date date) {
        UsageSpecificationEvent event = new UsageSpecificationEvent();
        event.setEventTime(date);
        event.setEventType(UsageSpecificationEventTypeEnum.UsageSpecificationUpdateNotification);
        event.setResource(bean);
        publish(event);
    }

}
