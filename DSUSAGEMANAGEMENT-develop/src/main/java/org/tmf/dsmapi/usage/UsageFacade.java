package org.tmf.dsmapi.usage;

import java.util.Date;
import org.tmf.dsmapi.commons.facade.AbstractFacade;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.tmf.dsmapi.commons.exceptions.BadUsageException;
import org.tmf.dsmapi.commons.exceptions.ExceptionType;
import org.tmf.dsmapi.commons.exceptions.UnknownResourceException;
import org.tmf.dsmapi.commons.utils.BeanUtils;
import org.tmf.dsmapi.usage.model.Usage;
import org.tmf.dsmapi.usage.event.UsageEventPublisherLocal;
import org.tmf.dsmapi.usage.model.RatedProductUsage;
import org.tmf.dsmapi.usage.model.RelatedParty;
import org.tmf.dsmapi.usage.model.Status;
import org.tmf.dsmapi.usage.model.UsageCharacteristic;

@Stateless
public class UsageFacade extends AbstractFacade<Usage> {

    @PersistenceContext(unitName = "DSUsagePU")
    private EntityManager em;
    @EJB
    UsageEventPublisherLocal publisher;
    StateModelImpl stateModel = new StateModelImpl();

    public UsageFacade() {
        super(Usage.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public void checkCreation(Usage newUsage) throws BadUsageException, UnknownResourceException {

        if (newUsage.getId() != null) {
            if (this.find(newUsage.getId()) != null) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_GENERIC,
                        "Duplicate Exception, Usage with same id :" + newUsage.getId() + " alreay exists");
            }
        }

        //verify status
        if (null == newUsage.getStatus() || newUsage.getStatus().name().equalsIgnoreCase("")) {
            newUsage.setStatus(Status.Received);
        } else {
            if ( ! newUsage.getStatus().name().equalsIgnoreCase(Status.Received.name())) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_GENERIC, "status "+newUsage.getStatus().name()+" is not the first status, attempt : "+Status.Received.name());
            }
            checkRulesStatus(newUsage);
        }

        if (null == newUsage.getDate()) {
            throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS, "date is mandatory");
        }

        if (null == newUsage.getType()
                || newUsage.getType().isEmpty()) {
            throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS, "type is mandatory");
        }

    }

    public Usage patchAttributs(long id, Usage partialUsage) throws UnknownResourceException, BadUsageException {
        Usage currentProduct = this.find(id);

        if (currentProduct == null) {
            throw new UnknownResourceException(ExceptionType.UNKNOWN_RESOURCE);
        }

        if (null != partialUsage.getId()) {
            throw new BadUsageException(ExceptionType.BAD_USAGE_OPERATOR,
                    "id is not patchable");
        }

        if (null != partialUsage.getHref()) {
            throw new BadUsageException(ExceptionType.BAD_USAGE_OPERATOR,
                    "href is not patchable");
        }
        
        checkRulesStatus(partialUsage);
        
        checkPatchRules(partialUsage);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.convertValue(partialUsage, JsonNode.class);
        if (null != partialUsage.getStatus()) {
            stateModel.checkTransition(currentProduct.getStatus(), partialUsage.getStatus());
            publisher.statusChangedNotification(currentProduct, new Date());
        }

        partialUsage.setId(id);
        if (BeanUtils.patch(currentProduct, partialUsage, node)) {
            publisher.updateNotification(currentProduct, new Date());
        }

        return currentProduct;
    }

    public void checkPatchRules(Usage usage) throws BadUsageException {
        if (null != usage.getUsageCharacteristic()
                && ! usage.getUsageCharacteristic().isEmpty()) {
            for (UsageCharacteristic usageCharacteristic : usage.getUsageCharacteristic()) {
                if (null == usageCharacteristic.getName()
                        || usageCharacteristic.getName().isEmpty()) {
                    throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                            "usageCharacteristic.name is mandatory");
                }
                if (null == usageCharacteristic.getValue()
                        || usageCharacteristic.getValue().isEmpty()) {
                    throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                            "usageCharacteristic.value is mandatory");
                }
            }
        }

        if (null != usage.getRelatedParty()
                && !usage.getRelatedParty().isEmpty()) {
            for (RelatedParty relatedParty : usage.getRelatedParty()) {
                if (null == relatedParty.getRole()
                        || relatedParty.getRole().isEmpty()) {
                    throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                            "relatedParty.role is mandatory");
                }
            }
        }

    }

    public void checkRulesStatus(Usage usage) throws BadUsageException {
        if (usage.getStatus() == Status.Rated || usage.getStatus() == Status.Billed) {
            if (null == usage.getRatedProductUsage()
                    || usage.getRatedProductUsage().isEmpty() ) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                        "ratedProductUsage is mandatory if status is 'rated' or 'billed'");
            } else {
                for (RatedProductUsage rpu : usage.getRatedProductUsage()) {
                    if (null == rpu.getRatingDate()) {
                        throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                                "ratedProductUsage.ratingDate is mandatory if status is 'rated' or 'billed'");
                    }
                    if (null == rpu.getTaxIncludedRatingAmount()) {
                        throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                                "ratedProductUsage.taxIncludedRatingAmount is mandatory if status is 'rated' or 'billed'");
                    }
                    if (null == rpu.getTaxExcludedRatingAmount()) {
                        throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                                "ratedProductUsage.taxExcludedRatingAmount is mandatory if status is 'rated' or 'billed'");
                    }
                    if (null == rpu.getTaxRate()) {
                        throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                                "ratedProductUsage.taxRate is mandatory if status is 'rated' or 'billed'");
                    }
                    if (null == rpu.getCurrencyCode()
                            || rpu.getCurrencyCode().isEmpty()) {
                        throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                                "ratedProductUsage.currencyCode is mandatory if status is 'rated' or 'billed'");
                    }
                    if (null == rpu.getProductRef()
                            || rpu.getProductRef().isEmpty()) {
                        throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                                "ratedProductUsage.productRef is mandatory if status is 'rated' or 'billed'");
                    }
                    if (null == rpu.getUsageRatingTag()
                            || rpu.getUsageRatingTag().isEmpty()) {
                        rpu.setUsageRatingTag("Usage");
                    }
                    if (null == rpu.isIsBilled()) {
                        rpu.setIsBilled(Boolean.FALSE);
                    }
                    if (null == rpu.getRatingAmountType()
                            || rpu.getRatingAmountType().isEmpty()) {
                        rpu.setRatingAmountType("Total");
                    }
                    if (null == rpu.isIsTaxExempt()) {
                        rpu.setIsTaxExempt(Boolean.FALSE);
                    }
                    if (null == rpu.getOfferTariffType()
                            || rpu.getOfferTariffType().isEmpty()) {
                        rpu.setOfferTariffType("Normal");
                    }
                }
            }
        }
    }
}
