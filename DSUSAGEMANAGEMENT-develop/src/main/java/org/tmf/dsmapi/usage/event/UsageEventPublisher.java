package org.tmf.dsmapi.usage.event;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.tmf.dsmapi.commons.exceptions.BadUsageException;
import org.tmf.dsmapi.usage.model.Usage;
import org.tmf.dsmapi.hub.Hub;
import org.tmf.dsmapi.hub.HubFacade;

/**
 *
 * Should be async or called with MDB
 */
@Stateless
@Asynchronous
public class UsageEventPublisher implements UsageEventPublisherLocal {

    @EJB
    HubFacade hubFacade;
    @EJB
    UsageEventFacade eventFacade;
    @EJB
    UsageRESTEventPublisherLocal restEventPublisherLocal;

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
    public void publish(UsageEvent event) {
        try {
            eventFacade.create(event);
        } catch (BadUsageException ex) {
            Logger.getLogger(UsageEventPublisher.class.getName()).log(Level.SEVERE, null, ex);
        }

        List<Hub> hubList = hubFacade.findAll();
        Iterator<Hub> it = hubList.iterator();
        while (it.hasNext()) {
            Hub hub = it.next();
            restEventPublisherLocal.publish(hub, event);
        }
    }

    @Override
    public void createNotification(Usage bean, Date date) {
        UsageEvent event = new UsageEvent();
        event.setEventTime(date);
        event.setEventType(UsageEventTypeEnum.UsageCreateNotification);
        event.setResource(bean);
        publish(event);

    }

    @Override
    public void deletionNotification(Usage bean, Date date) {
        UsageEvent event = new UsageEvent();
        event.setEventTime(date);
        event.setEventType(UsageEventTypeEnum.UsageDeleteNotification);
        event.setResource(bean);
        publish(event);
    }
	
    @Override
    public void updateNotification(Usage bean, Date date) {
        UsageEvent event = new UsageEvent();
        event.setEventTime(date);
        event.setEventType(UsageEventTypeEnum.UsageUpdateNotification);
        event.setResource(bean);
        publish(event);
    }

    @Override
    public void statusChangedNotification(Usage bean, Date date) {
        UsageEvent event = new UsageEvent();
        event.setEventTime(date);
        event.setEventType(UsageEventTypeEnum.UsageStatusChangedNotification);
        event.setResource(bean);
        publish(event);
    }
}
