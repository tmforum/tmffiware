package org.tmf.dsmapi.product.event;

import javax.ejb.Local;
import org.tmf.dsmapi.product.event.ProductEvent;
import org.tmf.dsmapi.hub.Hub;

@Local
public interface ProductRESTEventPublisherLocal {

    public void publish(Hub hub, ProductEvent event);
    
}
