package org.tmf.dsmapi.usageSpecification;

import org.tmf.dsmapi.commons.facade.AbstractFacade;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.tmf.dsmapi.commons.exceptions.BadUsageException;
import org.tmf.dsmapi.commons.exceptions.ExceptionType;
import org.tmf.dsmapi.commons.exceptions.UnknownResourceException;
import org.tmf.dsmapi.usage.model.Usage;
import org.tmf.dsmapi.usage.model.UsageSpecCharacteristic;
import org.tmf.dsmapi.usage.model.UsageSpecCharacteristicValue;
import org.tmf.dsmapi.usage.model.UsageSpecification;
import org.tmf.dsmapi.usageSpecification.event.UsageSpecificationEventPublisherLocal;

@Stateless
public class UsageSpecificationFacade extends AbstractFacade<UsageSpecification> {

    @PersistenceContext(unitName = "DSUsagePU")
    private EntityManager em;
    @EJB
    UsageSpecificationEventPublisherLocal publisher;

    public UsageSpecificationFacade() {
        super(UsageSpecification.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public void checkCreation(UsageSpecification newUsageSpecification) throws BadUsageException, UnknownResourceException {
        
        if (newUsageSpecification.getId() != null) {
            if (this.find(newUsageSpecification.getId()) != null) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_GENERIC,
                        "Duplicate Exception, UsageSpecification with same id :" + newUsageSpecification.getId() + " alreay exists");
            }
        }

        if (null != newUsageSpecification.getUsageSpecCharacteristic()
                && ! newUsageSpecification.getUsageSpecCharacteristic().isEmpty() ) {
            for (UsageSpecCharacteristic usc : newUsageSpecification.getUsageSpecCharacteristic()) {
                if (null == usc.getName()
                        || usc.getName().isEmpty()) {
                    throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS, 
                            "usageSpecCharacteristic.name is mandatory");
                }
                if (null == usc.getUsageSpecCharacteristicValue()
                        || usc.getUsageSpecCharacteristicValue().isEmpty()) {
                    throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS, 
                            "usageSpecCharacteristic.usageSpecCharacteristicValue is mandatory");
                } else {
                    for (UsageSpecCharacteristicValue uscValue : usc.getUsageSpecCharacteristicValue()) {
                        if (null == uscValue.getValueType()
                                || uscValue.getValueType().isEmpty()) {
                    throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS, 
                            "usageSpecCharacteristic.usageSpecCharacteristicValue.valueType is mandatory");
                        }
                    }
                }
            }
        }
    }
}
