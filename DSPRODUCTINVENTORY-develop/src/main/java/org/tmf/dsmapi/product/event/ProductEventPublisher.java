package org.tmf.dsmapi.product.event;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.tmf.dsmapi.commons.exceptions.BadUsageException;
import org.tmf.dsmapi.product.model.Product;
import org.tmf.dsmapi.product.event.ProductEvent;
import org.tmf.dsmapi.product.event.ProductEventTypeEnum;
import org.tmf.dsmapi.hub.Hub;
import org.tmf.dsmapi.hub.HubFacade;

/**
 *
 * Should be async or called with MDB
 */
@Stateless
@Asynchronous
public class ProductEventPublisher implements ProductEventPublisherLocal {

    @EJB
    HubFacade hubFacade;
    @EJB
    ProductEventFacade eventFacade;
    @EJB
    ProductRESTEventPublisherLocal restEventPublisherLocal;

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
    public void publish(ProductEvent event) {
        try {
            eventFacade.create(event);
        } catch (BadUsageException ex) {
            Logger.getLogger(ProductEventPublisher.class.getName()).log(Level.SEVERE, null, ex);
        }

        List<Hub> hubList = hubFacade.findAll();
        Iterator<Hub> it = hubList.iterator();
        while (it.hasNext()) {
            Hub hub = it.next();
            restEventPublisherLocal.publish(hub, event);
        }
    }

    @Override
    public void createNotification(Product bean, Date date) {
        ProductEvent event = new ProductEvent();
        event.setEventTime(date);
        event.setEventType(ProductEventTypeEnum.ProductCreationNotification);
        event.setResource(bean);
        publish(event);

    }

    @Override
    public void deletionNotification(Product bean, Date date) {
        ProductEvent event = new ProductEvent();
        event.setEventTime(date);
        event.setEventType(ProductEventTypeEnum.ProductDeletionNotification);
        event.setResource(bean);
        publish(event);
    }
	
    @Override
    public void updateNotification(Product bean, Date date) {
        ProductEvent event = new ProductEvent();
        event.setEventTime(date);
        event.setEventType(ProductEventTypeEnum.ProductUpdateNotification);
        event.setResource(bean);
        publish(event);
    }

    @Override
    public void valueChangedNotification(Product bean, Date date) {
        ProductEvent event = new ProductEvent();
        event.setEventTime(date);
        event.setEventType(ProductEventTypeEnum.ProductValueChangeNotification);
        event.setResource(bean);
        publish(event);
    }

    @Override
    public void statusChangedNotification(Product bean, Date date) {
        ProductEvent event = new ProductEvent();
        event.setEventTime(date);
        event.setEventType(ProductEventTypeEnum.ProductStatusChangeNotification);
        event.setResource(bean);
        publish(event);
    }

}
