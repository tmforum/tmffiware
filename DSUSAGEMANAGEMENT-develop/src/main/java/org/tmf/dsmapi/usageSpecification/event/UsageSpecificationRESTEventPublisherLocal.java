package org.tmf.dsmapi.usageSpecification.event;

import javax.ejb.Local;
import org.tmf.dsmapi.hub.Hub;

@Local
public interface UsageSpecificationRESTEventPublisherLocal {

    public void publish(Hub hub, UsageSpecificationEvent event);
    
}
