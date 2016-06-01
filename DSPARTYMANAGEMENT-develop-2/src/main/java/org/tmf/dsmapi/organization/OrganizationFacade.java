package org.tmf.dsmapi.organization;

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
import org.tmf.dsmapi.individual.model.ExternalReference;
import org.tmf.dsmapi.individual.model.Organization;
import org.tmf.dsmapi.individual.model.ValidFor;
import org.tmf.dsmapi.organization.event.OrganizationEventPublisherLocal;

/**
 *
 * @author pierregauthier
 */
@Stateless
public class OrganizationFacade extends AbstractFacade<Organization> {

    @PersistenceContext(unitName = "DSIndividualPU")
    private EntityManager em;
    @EJB
    OrganizationEventPublisherLocal publisher;

    public OrganizationFacade() {
        super(Organization.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public void checkCreationUpdate(Organization newOrganization, String action) throws BadUsageException, UnknownResourceException {

        if (action.equalsIgnoreCase("CREATE")) {
            if (newOrganization.getId() != null) {
                try {
                    this.find(newOrganization.getId()); // Throws exception if item does not exist
                    throw new BadUsageException(ExceptionType.BAD_USAGE_GENERIC,
                            "Duplicate Exception, Organization with same id :" + newOrganization.getId() + " alreay exists");
                } catch (UnknownResourceException ex) {
                    // Nothing to do here...
                }
            } else {
                throw new BadUsageException(ExceptionType.BAD_USAGE_GENERIC, "ID must be included");
            }
        }

        if (null == newOrganization.getTradingName()
                || newOrganization.getTradingName().isEmpty()) {
            throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                    "tradingName is mandatory");
        }

        if (null != newOrganization.getCharacteristic()) {
            if (null == newOrganization.getCharacteristic().getName()
                    || newOrganization.getCharacteristic().getName().isEmpty()) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                        "characteristic.name is mandatory");
            }
            if (null == newOrganization.getCharacteristic().getValue()
                    || newOrganization.getCharacteristic().getValue().isEmpty()) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                        "characteristic.value is mandatory");
            }
        }

        if (null != newOrganization.getOrganizationIdentification()) {
            if (null == newOrganization.getOrganizationIdentification().getType()
                    || newOrganization.getOrganizationIdentification().getType().isEmpty()) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                        "organizationIdentification.type is mandatory");
            }
            if (null == newOrganization.getOrganizationIdentification().getIdentificationId()
                    || newOrganization.getOrganizationIdentification().getIdentificationId().isEmpty()) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                        "organizationIdentification.identificationId is mandatory");
            }
        }

        if (null != newOrganization.getExternalReference()
                && !newOrganization.getExternalReference().isEmpty()) {
            for (ExternalReference reference : newOrganization.getExternalReference()) {
                if (null == reference.getType()
                        || reference.getType().isEmpty()) {
                    throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                            "externalReference.type is mandatory");
                }
                if (null == reference.getHref()
                        || reference.getHref().isEmpty()) {
                    throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                            "externalReference.href is mandatory");
                }
            }
        }

        if (null != newOrganization.getRelatedParty()) {
            if (null == newOrganization.getRelatedParty().getRole()
                    || newOrganization.getRelatedParty().getRole().isEmpty()) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                        "relatedParty.role is mandatory");
            }
            if (null != newOrganization.getRelatedParty().getValidFor()) {
                if (null == newOrganization.getRelatedParty().getValidFor().getStartDateTime()) {
                    newOrganization.getRelatedParty().getValidFor().setStartDateTime(new Date());
                }
            } else {
                ValidFor validFor = new ValidFor();
                validFor.setStartDateTime(new Date());
                newOrganization.getRelatedParty().setValidFor(validFor);
            }
        }

        if (null != newOrganization.getOrganizationParentRelationship()) {
            if (null == newOrganization.getOrganizationParentRelationship().getRelationshipType()
                    || newOrganization.getOrganizationParentRelationship().getRelationshipType().isEmpty()) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                        "organizationParentRelationship.relationshipType is mandatory");
            }
            if (null == newOrganization.getOrganizationParentRelationship().getHref()
                    || newOrganization.getOrganizationParentRelationship().getHref().isEmpty()) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                        "organizationParentRelationship.href is mandatory");
            }
            if (null != newOrganization.getOrganizationParentRelationship().getValidFor()) {
                if (null == newOrganization.getOrganizationParentRelationship().getValidFor().getStartDateTime()) {
                    newOrganization.getOrganizationParentRelationship().getValidFor().setStartDateTime(new Date());
                }
            } else {
                ValidFor validFor = new ValidFor();
                validFor.setStartDateTime(new Date());
                newOrganization.getOrganizationParentRelationship().setValidFor(validFor);
            }
        }

        if (null != newOrganization.getOrganizationChildRelationship()) {
            if (null == newOrganization.getOrganizationChildRelationship().getRelationshipType()
                    || newOrganization.getOrganizationChildRelationship().getRelationshipType().isEmpty()) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                        "organizationChildrenRelationship.relationshipType is mandatory");
            }
            if (null == newOrganization.getOrganizationChildRelationship().getHref()
                    || newOrganization.getOrganizationChildRelationship().getHref().isEmpty()) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                        "organizationChildrenRelationship.href is mandatory");
            }
            if (null != newOrganization.getOrganizationChildRelationship().getValidFor()) {
                if (null == newOrganization.getOrganizationChildRelationship().getValidFor().getStartDateTime()) {
                    newOrganization.getOrganizationChildRelationship().getValidFor().setStartDateTime(new Date());
                }
            } else {
                ValidFor validFor = new ValidFor();
                validFor.setStartDateTime(new Date());
                newOrganization.getOrganizationChildRelationship().setValidFor(validFor);
            }
        }

    }

    public Organization patchAttributs(String id, Organization partialOrganization) throws UnknownResourceException, BadUsageException {
        Organization currentOrganization = this.find(id);

        if (currentOrganization == null) {
            throw new UnknownResourceException(ExceptionType.UNKNOWN_RESOURCE);
        }

        if (null != partialOrganization.getId()) {
            throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                    "id is not patchable");
        }

        if (null != partialOrganization.getCharacteristic()) {
            if (null == partialOrganization.getCharacteristic().getName()
                    || partialOrganization.getCharacteristic().getName().isEmpty()) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                        "characteristic.name is mandatory");
            }
            if (null == partialOrganization.getCharacteristic().getValue()
                    || partialOrganization.getCharacteristic().getValue().isEmpty()) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                        "characteristic.value is mandatory");
            }
        }

        if (null != partialOrganization.getOrganizationIdentification()) {
            if (null == partialOrganization.getOrganizationIdentification().getType()
                    || partialOrganization.getOrganizationIdentification().getType().isEmpty()) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                        "organizationIdentification.type is mandatory");
            }
            if (null == partialOrganization.getOrganizationIdentification().getIdentificationId()
                    || partialOrganization.getOrganizationIdentification().getIdentificationId().isEmpty()) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                        "organizationIdentification.identificationId is mandatory");
            }
        }

        if (null != partialOrganization.getExternalReference()
                && !partialOrganization.getExternalReference().isEmpty()) {
            for (ExternalReference reference : partialOrganization.getExternalReference()) {
                if (null == reference.getType()
                        || reference.getType().isEmpty()) {
                    throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                            "externalReference.type is mandatory");
                }
                if (null == reference.getHref()
                        || reference.getHref().isEmpty()) {
                    throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                            "externalReference.href is mandatory");
                }
            }
        }

        if (null != partialOrganization.getRelatedParty()) {
            if (null == partialOrganization.getRelatedParty().getRole()
                    || partialOrganization.getRelatedParty().getRole().isEmpty()) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                        "relatedParty.role is mandatory");
            }
            if (null != partialOrganization.getRelatedParty().getValidFor()) {
                if (null == partialOrganization.getRelatedParty().getValidFor().getStartDateTime()) {
                    partialOrganization.getRelatedParty().getValidFor().setStartDateTime(new Date());
                }
            } else {
                ValidFor validFor = new ValidFor();
                validFor.setStartDateTime(new Date());
                partialOrganization.getRelatedParty().setValidFor(validFor);
            }
        }

        if (null != partialOrganization.getOrganizationParentRelationship()) {
            if (null == partialOrganization.getOrganizationParentRelationship().getRelationshipType()
                    || partialOrganization.getOrganizationParentRelationship().getRelationshipType().isEmpty()) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                        "organizationParentRelationship.relationshipType is mandatory");
            }
            if (null == partialOrganization.getOrganizationParentRelationship().getHref()
                    || partialOrganization.getOrganizationParentRelationship().getHref().isEmpty()) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                        "organizationParentRelationship.href is mandatory");
            }
            if (null != partialOrganization.getOrganizationParentRelationship().getValidFor()) {
                if (null == partialOrganization.getOrganizationParentRelationship().getValidFor().getStartDateTime()) {
                    partialOrganization.getOrganizationParentRelationship().getValidFor().setStartDateTime(new Date());
                }
            } else {
                ValidFor validFor = new ValidFor();
                validFor.setStartDateTime(new Date());
                partialOrganization.getOrganizationParentRelationship().setValidFor(validFor);
            }
        }

        if (null != partialOrganization.getOrganizationChildRelationship()) {
            if (null == partialOrganization.getOrganizationChildRelationship().getRelationshipType()
                    || partialOrganization.getOrganizationChildRelationship().getRelationshipType().isEmpty()) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                        "organizationChildrenRelationship.relationshipType is mandatory");
            }
            if (null == partialOrganization.getOrganizationChildRelationship().getHref()
                    || partialOrganization.getOrganizationChildRelationship().getHref().isEmpty()) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS,
                        "organizationChildrenRelationship.href is mandatory");
            }
            if (null != partialOrganization.getOrganizationChildRelationship().getValidFor()) {
                if (null == partialOrganization.getOrganizationChildRelationship().getValidFor().getStartDateTime()) {
                    partialOrganization.getOrganizationChildRelationship().getValidFor().setStartDateTime(new Date());
                }
            } else {
                ValidFor validFor = new ValidFor();
                validFor.setStartDateTime(new Date());
                partialOrganization.getOrganizationChildRelationship().setValidFor(validFor);
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.convertValue(partialOrganization, JsonNode.class);
        partialOrganization.setId(id);
        if (BeanUtils.patch(currentOrganization, partialOrganization, node)) {
            publisher.updateNotification(currentOrganization, new Date());
        }

        return currentOrganization;
    }
}
