package org.tmf.dsmapi.usage.event;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.tmf.dsmapi.commons.facade.AbstractFacade;

@Stateless
public class UsageEventFacade extends AbstractFacade<UsageEvent>{
    
    @PersistenceContext(unitName = "DSUsagePU")
    private EntityManager em;
   

    
    /**
     *
     */
    public UsageEventFacade() {
        super(UsageEvent.class);
    }


    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
