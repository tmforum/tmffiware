package org.tmf.dsmapi.catalog.resource.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.tmf.dsmapi.catalog.resource.AbstractCatalogEntity;
import org.tmf.dsmapi.catalog.resource.Attachment;
import org.tmf.dsmapi.catalog.resource.LifecycleStatus;
import org.tmf.dsmapi.catalog.resource.RelatedParty;
import org.tmf.dsmapi.catalog.resource.TimeRange;
import org.tmf.dsmapi.catalog.resource.specification.SpecificationRelationship;
import org.tmf.dsmapi.commons.Utilities;

/**
 *
 * @author bahman.barzideh
 *
 * {
 *     "id": "22",
 *     "version": "2.9",
 *     "href": "http://serverlocation:port/catalogManagement/resourceSpecification/22",
 *     "name": "iPhone 42",
 *     "description": "Siri works on this iPhone",
 *     "lastUpdate": "2013-04-19T16:42:23-04:00",
 *     "lifecycleStatus": "Active",
 *     "validFor": {
 *         "startDateTime": "2013-04-19T16:42:23-04:00",
 *         "endDateTime": "2013-06-19T00:00:00-04:00"
 *     },
 *     "brand": "Apple",
 *     "attachment": [
 *         {
 *             "id": "22",
 *             "href": "http://serverlocation:port/documentManagment/attachment/22",
 *             "type": "Picture",
 *             "url": "http://xxxxx"
 *         }
 *     ],
 *     "relatedParty": [
 *         {
 *             "role": "Owner",
 *             "id": "1234",
 *             "href": "http ://serverLocation:port/partyManagement/partyRole/1234"
 *         }
 *     ],
 *     "resourceSpecificationRelationship": [
 *         {
 *             "type": "dependency",
 *             "id": "23",
 *             "href": " http://serverlocation:port/catalogManagement/resourceSpecification/23",
 *             "validFor": {
 *                 "startDateTime": "2013-04-19T16:42:23-04:00",
 *                 "endDateTime": ""
 *             }
 *         }
 *     ],
 *     "resourceSpecCharacteristic": [
 *         {
 *             "id": "54",
 *             "name": "Screen Size",
 *             "description": "Screen size",
 *             "valueType": "number",
 *             "configurable": "false",
 *             "validFor": {
 *                 "startDateTime": "2013-04-19T16:42:23-04:00",
 *                 "endDateTime": ""
 *             },
 *             "resourceSpecCharRelationship": [
 *                 {
 *                     "type": "dependency",
 *                     "id": "43",
 *                     "validFor": {
 *                         "startDateTime": "2013-04-19T16:42:23-04:00",
 *                         "endDateTime": ""
 *                     }
 *                 }
 *             ],
 *             "resourceSpecCharacteristicValue": [
 *                 {
 *                     "valueType": "number",
 *                     "default": "true",
 *                     "value": "4.2",
 *                     "unitOfMeasure": "inches",
 *                     "valueFrom": "",
 *                     "valueTo": "",
 *                     "validFor": {
 *                         "startDateTime": "2013-04-19T16:42:23-04:00",
 *                         "endDateTime": ""
 *                     }
 *                 }
 *             ]
 *         },
 *         {
 *             "id": "55",
 *             "name": "Colour",
 *             "description": "Colour",
 *             "valueType": "string",
 *             "configurable": "true",
 *             "validFor": {
 *                 "startDateTime": "2013-04-19T16:42:23-04:00",
 *                 "endDateTime": ""
 *             },
 *             "resourceSpecCharacteristicValue": [
 *                 {
 *                     "valueType": "string",
 *                     "default": "true",
 *                     "value": "Black",
 *                     "unitOfMeasure": "",
 *                     "valueFrom": "",
 *                     "valueTo": "",
 *                     "validFor": {
 *                         "startDateTime": "2013-04-19T16:42:23-04:00",
 *                         "endDateTime": ""
 *                     }
 *                 },
 *                 {
 *                     "valueType": "string",
 *                     "default": "false",
 *                     "value": "White",
 *                     "unitOfMeasure": "",
 *                     "valueFrom": "",
 *                     "valueTo": "",
 *                     "validFor": {
 *                         "startDateTime": "2013-04-19T16:42:23-04:00",
 *                         "endDateTime": ""
 *                     }
 *                 }
 *             ]
 *         }
 *     ]
 * }
 *
 */
@MappedSuperclass
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResourceSpecification extends AbstractCatalogEntity implements Serializable {
    private final static long serialVersionUID = 1L;

    private final static Logger logger = Logger.getLogger(ResourceSpecification.class.getName());

    @Column(name = "BRAND", nullable = true)
    private String brand;

    @Embedded
    @ElementCollection
    @CollectionTable(name = "CRI_RESOURCE_SPEC_R_ATTACHMENT", joinColumns = {
        @JoinColumn(name = "CATALOG_ID", referencedColumnName = "CATALOG_ID"),
        @JoinColumn(name = "CATALOG_VERSION", referencedColumnName = "CATALOG_VERSION"),
        @JoinColumn(name = "ENTITY_ID", referencedColumnName = "ID"),
        @JoinColumn(name = "ENTITY_VERSION", referencedColumnName = "VERSION")
    })
    private List<Attachment> attachment;

    @Embedded
    @ElementCollection
    @CollectionTable(name = "CRI_RESOURCE_SPEC_R_PARTY", joinColumns = {
        @JoinColumn(name = "CATALOG_ID", referencedColumnName = "CATALOG_ID"),
        @JoinColumn(name = "CATALOG_VERSION", referencedColumnName = "CATALOG_VERSION"),
        @JoinColumn(name = "ENTITY_ID", referencedColumnName = "ID"),
        @JoinColumn(name = "ENTITY_VERSION", referencedColumnName = "VERSION")
    })
    private List<RelatedParty> relatedParty;

    @Embedded
    @ElementCollection
    @CollectionTable(name = "CRI_RESOURCE_SPEC_R_RELATIONSHIP", joinColumns = {
        @JoinColumn(name = "CATALOG_ID", referencedColumnName = "CATALOG_ID"),
        @JoinColumn(name = "CATALOG_VERSION", referencedColumnName = "CATALOG_VERSION"),
        @JoinColumn(name = "ENTITY_ID", referencedColumnName = "ID"),
        @JoinColumn(name = "ENTITY_VERSION", referencedColumnName = "VERSION")
    })
    private List<SpecificationRelationship> resourceSpecificationRelationship;

    @Embedded
    @ElementCollection
    @CollectionTable(name = "CRI_RESOURCE_SPEC_R_CHARACTERISTIC", joinColumns = {
        @JoinColumn(name = "CATALOG_ID", referencedColumnName = "CATALOG_ID"),
        @JoinColumn(name = "CATALOG_VERSION", referencedColumnName = "CATALOG_VERSION"),
        @JoinColumn(name = "ENTITY_ID", referencedColumnName = "ID"),
        @JoinColumn(name = "ENTITY_VERSION", referencedColumnName = "VERSION")
    })
    private List<ResourceSpecCharacteristic> resourceSpecCharacteristic;

    public ResourceSpecification() {
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public List<Attachment> getAttachment() {
        return attachment;
    }

    public void setAttachment(List<Attachment> attachment) {
        this.attachment = attachment;
    }

    public List<RelatedParty> getRelatedParty() {
        return relatedParty;
    }

    public void setRelatedParty(List<RelatedParty> relatedParty) {
        this.relatedParty = relatedParty;
    }

    public List<SpecificationRelationship> getResourceSpecificationRelationship() {
        return resourceSpecificationRelationship;
    }

    public void setResourceSpecificationRelationship(List<SpecificationRelationship> resourceSpecificationRelationship) {
        this.resourceSpecificationRelationship = resourceSpecificationRelationship;
    }

    public List<ResourceSpecCharacteristic> getResourceSpecCharacteristic() {
        return resourceSpecCharacteristic;
    }

    public void setResourceSpecCharacteristic(List<ResourceSpecCharacteristic> resourceSpecCharacteristic) {
        this.resourceSpecCharacteristic = resourceSpecCharacteristic;
    }

    @JsonProperty(value = "attachment")
    public List<Attachment> attachmentToJson() {
        return (attachment != null && attachment.size() > 0) ? attachment : null;
    }

    @JsonProperty(value = "relatedParty")
    public List<RelatedParty> relatedPartyToJson() {
        return (relatedParty != null && relatedParty.size() > 0) ? relatedParty : null;
    }

    @JsonProperty(value = "resourceSpecificationRelationship")
    public List<SpecificationRelationship> resourceSpecificationRelationshipToJson() {
        return (resourceSpecificationRelationship != null && resourceSpecificationRelationship.size() > 0) ? resourceSpecificationRelationship : null;
    }

    @JsonProperty(value = "resourceSpecCharacteristic")
    public List<ResourceSpecCharacteristic> resourceSpecCharacteristicToJson() {
        return (resourceSpecCharacteristic != null && resourceSpecCharacteristic.size() > 0) ? resourceSpecCharacteristic : null;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = 89 * hash + super.hashCode();

        hash = 89 * hash + (this.brand != null ? this.brand.hashCode() : 0);
        hash = 89 * hash + (this.attachment != null ? this.attachment.hashCode() : 0);
        hash = 89 * hash + (this.relatedParty != null ? this.relatedParty.hashCode() : 0);
        hash = 89 * hash + (this.resourceSpecificationRelationship != null ? this.resourceSpecificationRelationship.hashCode() : 0);
        hash = 89 * hash + (this.resourceSpecCharacteristic != null ? this.resourceSpecCharacteristic.hashCode() : 0);

        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass() || super.equals(object) == false) {
            return false;
        }

        final ResourceSpecification other = (ResourceSpecification) object;
        if (Utilities.areEqual(this.brand, other.brand) == false) {
            return false;
        }

        if (Utilities.areEqual(this.attachment, other.attachment) == false) {
            return false;
        }

        if (Utilities.areEqual(this.relatedParty, other.relatedParty) == false) {
            return false;
        }

        if (Utilities.areEqual(this.resourceSpecificationRelationship, other.resourceSpecificationRelationship) == false) {
            return false;
        }

        if (Utilities.areEqual(this.resourceSpecCharacteristic, other.resourceSpecCharacteristic) == false) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "ResourceSpecification{<" + super.toString() + ">, brand=" + brand + ", attachment=" + attachment + ", relatedParty=" + relatedParty + ", resourceSpecificationRelationship=" + resourceSpecificationRelationship + ", resourceSpecCharacteristic=" + resourceSpecCharacteristic + '}';
    }

    @Override
    @JsonIgnore
    public Logger getLogger() {
        return logger;
    }

    public void edit(ResourceSpecification input) {
        if (input == null || input == this) {
            return;
        }

        super.edit(input);

        if (this.brand == null) {
            this.brand = input.brand;
        }

        if (this.attachment == null) {
            this.attachment = input.attachment;
        }

        if (this.relatedParty == null) {
            this.relatedParty = input.relatedParty;
        }

        if (this.resourceSpecificationRelationship == null) {
            this.resourceSpecificationRelationship = input.resourceSpecificationRelationship;
        }

        if (this.resourceSpecCharacteristic == null) {
            this.resourceSpecCharacteristic = input.resourceSpecCharacteristic;
        }
    }

    @Override
    @JsonIgnore
    public boolean isValid() {
        logger.log(Level.FINE, "ResourceSpecification:valid ()");

        if (super.isValid() == false) {
            return false;
        }

        if (validateCharacteristics() == false) {
            return false;
        }

        return true;
    }

    public boolean validateCharacteristics() {
        if (Utilities.hasContents(this.resourceSpecCharacteristic) == false) {
            return true;
        }

        for (ResourceSpecCharacteristic characteristic : this.resourceSpecCharacteristic) {
            if (characteristic.isValid() == false) {
                return false;
            }
        }

        return true;
    }

    public static ResourceSpecification createProto() {
        ResourceSpecification resourceSpecification = new ResourceSpecification();

        resourceSpecification.setId("id");
        resourceSpecification.setVersion("2.9");
        resourceSpecification.setHref("href");
        resourceSpecification.setName("name");
        resourceSpecification.setDescription("description");
        resourceSpecification.setLastUpdate(new Date ());
        resourceSpecification.setLifecycleStatus(LifecycleStatus.ACTIVE);
        resourceSpecification.setValidFor(TimeRange.createProto ());

        resourceSpecification.brand = "brand";

        resourceSpecification.attachment = new ArrayList<Attachment>();
        resourceSpecification.attachment.add(Attachment.createProto());

        resourceSpecification.relatedParty = new ArrayList<RelatedParty>();
        resourceSpecification.relatedParty.add(RelatedParty.createProto());

        resourceSpecification.resourceSpecificationRelationship = new ArrayList<SpecificationRelationship>();
        resourceSpecification.resourceSpecificationRelationship.add(SpecificationRelationship.createProto());

        resourceSpecification.resourceSpecCharacteristic = new ArrayList<ResourceSpecCharacteristic>();
        resourceSpecification.resourceSpecCharacteristic.add(ResourceSpecCharacteristic.createProto());

        return resourceSpecification;
    }

}
