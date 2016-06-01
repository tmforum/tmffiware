package org.tmf.dsmapi.usageSpecification.event;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.tmf.dsmapi.commons.facade.AbstractFacade;

@Stateless
public class UsageSpecificationEventFacade extends AbstractFacade<UsageSpecificationEvent>{
    
    @PersistenceContext(unitName = "DSUsagePU")
    private EntityManager em;
   

    
    /**
     *
     */
    public UsageSpecificationEventFacade() {
        super(UsageSpecificationEvent.class);
    }


    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
