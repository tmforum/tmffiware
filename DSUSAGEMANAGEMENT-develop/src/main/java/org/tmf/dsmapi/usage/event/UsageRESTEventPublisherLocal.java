package org.tmf.dsmapi.usage.event;

import javax.ejb.Local;
import org.tmf.dsmapi.hub.Hub;

@Local
public interface UsageRESTEventPublisherLocal {

    public void publish(Hub hub, UsageEvent event);
    
}
