package org.tmf.dsmapi.catalog.resource.service;

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
import org.tmf.dsmapi.catalog.resource.specification.RequiredSpecification;
import org.tmf.dsmapi.catalog.resource.specification.SpecificationRelationship;
import org.tmf.dsmapi.commons.Utilities;

/**
 *
 * @author bahman.barzideh
 *
 * {
 *     "id": "22",
 *     "version": "2.0",
 *     "href": "http://serverlocation:port/catalogManagement/serviceSpecification/22",
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
 *             "id": "56",
 *             "href": "http://serverlocation:port/documentManagment/attachment/56",
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
 *     "serviceSpecificationRelationship": [
 *         {
 *             "type": "dependency",
 *             "id": "23",
 *             "href": " http://serverlocation:port/catalogManagement/serviceSpecification/23",
 *             "validFor": {
 *                 "startDateTime": "2013-04-19T16:42:23-04:00",
 *                 "endDateTime": ""
 *             }
 *         }
 *     ],
 *     "requiredServiceSpecification": [
 *         {
 *             "id": "13",
 *             "href": "http://serverlocation:port/catalogManagement/serviceSpecification/13",
 *             "name": "specification 1",
 *             "validFor": {
 *                 "startDateTime": "2013-04-19T16:42:23-04:00",
 *                 "endDateTime": ""
 *             }
 *         }
 *     ],
 *     "requiredResourceSpecification": [
 *         {
 *             "id": "13",
 *             "href": "http://serverlocation:port/catalogManagement/resourceSpecification/13",
 *             "name": "specification 1",
 *             "validFor": {
 *                 "startDateTime": "2013-04-19T16:42:23-04:00",
 *                 "endDateTime": ""
 *             }
 *         }
 *     ],
 *     "serviceSpecCharacteristic": [
 *         {
 *             "id": "34",
 *             "name": "Screen Size",
 *             "description": "Screen size",
 *             "valueType": "number",
 *             "configurable": false,
 *             "validFor": {
 *                 "startDateTime": "2013-04-19T16:42:23-04:00",
 *                 "endDateTime": ""
 *             },
 *             "serviceSpecCharRelationship": [
 *                 {
 *                     "type": "dependency",
 *                     "id": "43",
 *                     "validFor": {
 *                         "startDateTime": "2013-04-19T16:42:23-04:00",
 *                         "endDateTime": ""
 *                     }
 *                 }
 *             ],
 *             "serviceSpecCharacteristicValue": [
 *                 {
 *                     "valueType": "number",
 *                     "default": true,
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
 *             "id": "54",
 *             "name": "Colour",
 *             "description": "Product colour",
 *             "valueType": "string",
 *             "configurable": true,
 *             "validFor": {
 *                 "startDateTime": "2013-04-19T16:42:23-04:00",
 *                 "endDateTime": ""
 *             },
 *             "serviceSpecCharRelationship": [
 *                 {
 *                     "type": "dependency",
 *                     "id": "43",
 *                     "validFor": {
 *                         "startDateTime": "2013-04-19T16:42:23-04:00",
 *                         "endDateTime": ""
 *                     }
 *                 }
 *             ],
 *             "serviceSpecCharacteristicValue": [
 *                 {
 *                     "valueType": "string",
 *                     "default": true,
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
 *                     "default": false,
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
public class ServiceSpecification extends AbstractCatalogEntity implements Serializable {
    private final static long serialVersionUID = 1L;

    private final static Logger logger = Logger.getLogger(ServiceSpecification.class.getName());

    @Column(name = "BRAND", nullable = false)
    private String brand;

    @Embedded
    @ElementCollection
    @CollectionTable(name = "CRI_SERVICE_SPEC_R_ATTACHMENT", joinColumns = {
        @JoinColumn(name = "CATALOG_ID", referencedColumnName = "CATALOG_ID"),
        @JoinColumn(name = "CATALOG_VERSION", referencedColumnName = "CATALOG_VERSION"),
        @JoinColumn(name = "ENTITY_ID", referencedColumnName = "ID"),
        @JoinColumn(name = "ENTITY_VERSION", referencedColumnName = "VERSION")
    })
    private List<Attachment> attachment;

    @Embedded
    @ElementCollection
    @CollectionTable(name = "CRI_SERVICE_SPEC_R_PARTY", joinColumns = {
        @JoinColumn(name = "CATALOG_ID", referencedColumnName = "CATALOG_ID"),
        @JoinColumn(name = "CATALOG_VERSION", referencedColumnName = "CATALOG_VERSION"),
        @JoinColumn(name = "ENTITY_ID", referencedColumnName = "ID"),
        @JoinColumn(name = "ENTITY_VERSION", referencedColumnName = "VERSION")
    })
    private List<RelatedParty> relatedParty;

    @Embedded
    @ElementCollection
    @CollectionTable(name = "CRI_SERVICE_SPEC_R_RELATIONSHIP", joinColumns = {
        @JoinColumn(name = "CATALOG_ID", referencedColumnName = "CATALOG_ID"),
        @JoinColumn(name = "CATALOG_VERSION", referencedColumnName = "CATALOG_VERSION"),
        @JoinColumn(name = "ENTITY_ID", referencedColumnName = "ID"),
        @JoinColumn(name = "ENTITY_VERSION", referencedColumnName = "VERSION")
    })
    private List<SpecificationRelationship> serviceSpecificationRelationship;

    @Embedded
    @ElementCollection
    @CollectionTable(name = "CRI_SERVICE_SPEC_R_REQ_SERVICE_SPEC", joinColumns = {
        @JoinColumn(name = "CATALOG_ID", referencedColumnName = "CATALOG_ID"),
        @JoinColumn(name = "CATALOG_VERSION", referencedColumnName = "CATALOG_VERSION"),
        @JoinColumn(name = "ENTITY_ID", referencedColumnName = "ID"),
        @JoinColumn(name = "ENTITY_VERSION", referencedColumnName = "VERSION")
    })
    private List<RequiredSpecification> requiredServiceSpecification;

    @Embedded
    @ElementCollection
    @CollectionTable(name = "CRI_SERVICE_SPEC_R_REQ_RESOURCE_SPEC", joinColumns = {
        @JoinColumn(name = "CATALOG_ID", referencedColumnName = "CATALOG_ID"),
        @JoinColumn(name = "CATALOG_VERSION", referencedColumnName = "CATALOG_VERSION"),
        @JoinColumn(name = "ENTITY_ID", referencedColumnName = "ID"),
        @JoinColumn(name = "ENTITY_VERSION", referencedColumnName = "VERSION")
    })
    private List<RequiredSpecification> requiredResourceSpecification;

    @Embedded
    @ElementCollection
    @CollectionTable(name = "CRI_SERVICE_SPEC_R_CHARACTERISTIC", joinColumns = {
        @JoinColumn(name = "CATALOG_ID", referencedColumnName = "CATALOG_ID"),
        @JoinColumn(name = "CATALOG_VERSION", referencedColumnName = "CATALOG_VERSION"),
        @JoinColumn(name = "ENTITY_ID", referencedColumnName = "ID"),
        @JoinColumn(name = "ENTITY_VERSION", referencedColumnName = "VERSION")
    })
    private List<ServiceSpecCharacteristic> serviceSpecCharacteristic;

    public ServiceSpecification() {
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

    public List<SpecificationRelationship> getServiceSpecificationRelationship() {
        return serviceSpecificationRelationship;
    }

    public void setServiceSpecificationRelationship(List<SpecificationRelationship> serviceSpecificationRelationship) {
        this.serviceSpecificationRelationship = serviceSpecificationRelationship;
    }

    public List<RequiredSpecification> getRequiredServiceSpecification() {
        return requiredServiceSpecification;
    }

    public void setRequiredServiceSpecification(List<RequiredSpecification> requiredServiceSpecification) {
        this.requiredServiceSpecification = requiredServiceSpecification;
    }

    public List<RequiredSpecification> getRequiredResourceSpecification() {
        return requiredResourceSpecification;
    }

    public void setRequiredResourceSpecification(List<RequiredSpecification> requiredResourceSpecification) {
        this.requiredResourceSpecification = requiredResourceSpecification;
    }

    public List<ServiceSpecCharacteristic> getServiceSpecCharacteristic() {
        return serviceSpecCharacteristic;
    }

    public void setServiceSpecCharacteristic(List<ServiceSpecCharacteristic> serviceSpecCharacteristic) {
        this.serviceSpecCharacteristic = serviceSpecCharacteristic;
    }

    @JsonProperty(value = "attachment")
    public List<Attachment> attachmentToJson() {
        return (attachment != null && attachment.size() > 0) ? attachment : null;
    }

    @JsonProperty(value = "relatedParty")
    public List<RelatedParty> relatedPartyToJson() {
        return (relatedParty != null && relatedParty.size() > 0) ? relatedParty : null;
    }

    @JsonProperty(value = "serviceSpecificationRelationship")
    public List<SpecificationRelationship> serviceSpecificationRelationshipToJson() {
        return (serviceSpecificationRelationship != null && serviceSpecificationRelationship.size() > 0) ? serviceSpecificationRelationship : null;
    }

    @JsonProperty(value = "requiredServiceSpecification")
    public List<RequiredSpecification> requiredServiceSpecificationToJson() {
        return (requiredServiceSpecification != null && requiredServiceSpecification.size() > 0) ? requiredServiceSpecification : null;
    }

    @JsonProperty(value = "requiredResourceSpecification")
    public List<RequiredSpecification> requiredResourceSpecificationToJson() {
        return (requiredResourceSpecification != null && requiredResourceSpecification.size() > 0) ? requiredResourceSpecification : null;
    }

    @JsonProperty(value = "serviceSpecCharacteristic")
    public List<ServiceSpecCharacteristic> serviceSpecCharacteristicToJson() {
        return (serviceSpecCharacteristic != null && serviceSpecCharacteristic.size() > 0) ? serviceSpecCharacteristic : null;
    }

    @Override
    public int hashCode() {
        int hash = 3;

        hash = 89 * hash + super.hashCode();

        hash = 89 * hash + (this.brand != null ? this.brand.hashCode() : 0);
        hash = 89 * hash + (this.attachment != null ? this.attachment.hashCode() : 0);
        hash = 89 * hash + (this.relatedParty != null ? this.relatedParty.hashCode() : 0);
        hash = 89 * hash + (this.serviceSpecificationRelationship != null ? this.serviceSpecificationRelationship.hashCode() : 0);
        hash = 89 * hash + (this.requiredServiceSpecification != null ? this.requiredServiceSpecification.hashCode() : 0);
        hash = 89 * hash + (this.requiredResourceSpecification != null ? this.requiredResourceSpecification.hashCode() : 0);
        hash = 89 * hash + (this.serviceSpecCharacteristic != null ? this.serviceSpecCharacteristic.hashCode() : 0);

        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass() || super.equals(object) == false) {
            return false;
        }

        final ServiceSpecification other = (ServiceSpecification) object;
        if (Utilities.areEqual(this.brand, other.brand) == false) {
            return false;
        }

        if (Utilities.areEqual(this.attachment, other.attachment) == false) {
            return false;
        }

        if (Utilities.areEqual(this.relatedParty, other.relatedParty) == false) {
            return false;
        }

        if (Utilities.areEqual(this.serviceSpecificationRelationship, other.serviceSpecificationRelationship) == false) {
            return false;
        }

        if (Utilities.areEqual(this.requiredServiceSpecification, other.requiredServiceSpecification) == false) {
            return false;
        }

        if (Utilities.areEqual(this.requiredResourceSpecification, other.requiredResourceSpecification) == false) {
            return false;
        }

        if (Utilities.areEqual(this.serviceSpecCharacteristic, other.serviceSpecCharacteristic) == false) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "ServiceSpecification{<" + super.toString() + ">, brand=" + brand + ", attachment=" + attachment + ", relatedParty=" + relatedParty + ", serviceSpecificationRelationship=" + serviceSpecificationRelationship + ", requiredServiceSpecification=" + requiredServiceSpecification + ", requiredResourceSpecification=" + requiredResourceSpecification + ", serviceSpecCharacteristic=" + serviceSpecCharacteristic + '}';
    }

    @Override
    @JsonIgnore
    public Logger getLogger() {
        return logger;
    }

    public void edit(ServiceSpecification input) {
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

        if (this.serviceSpecificationRelationship == null) {
            this.serviceSpecificationRelationship = input.serviceSpecificationRelationship;
        }

        if (this.requiredServiceSpecification == null) {
            this.requiredServiceSpecification = input.requiredServiceSpecification;
        }

        if (this.requiredResourceSpecification == null) {
            this.requiredResourceSpecification = input.requiredResourceSpecification;
        }

        if (this.serviceSpecCharacteristic == null) {
            this.serviceSpecCharacteristic = input.serviceSpecCharacteristic;
        }
    }

    @Override
    @JsonIgnore
    public boolean isValid() {
        logger.log(Level.FINE, "ServiceSpecification:valid ()");

        if (super.isValid() == false) {
            return false;
        }

        if (validateCharacteristics() == false) {
            return false;
        }

        return true;
    }

    public boolean validateCharacteristics() {
        if (Utilities.hasContents(this.serviceSpecCharacteristic) == false) {
            return true;
        }

        for (ServiceSpecCharacteristic characteristic : this.serviceSpecCharacteristic) {
            if (characteristic.isValid() == false) {
                return false;
            }
        }

        return true;
    }

    public static ServiceSpecification createProto() {
        ServiceSpecification serviceSpecification = new ServiceSpecification();

        serviceSpecification.setId("id");
        serviceSpecification.setVersion("7.3");
        serviceSpecification.setHref("href");
        serviceSpecification.setName("name");
        serviceSpecification.setDescription("description");
        serviceSpecification.setLastUpdate(new Date ());
        serviceSpecification.setLifecycleStatus(LifecycleStatus.ACTIVE);
        serviceSpecification.setValidFor(TimeRange.createProto ());

        serviceSpecification.brand = "brand";

        serviceSpecification.attachment = new ArrayList<Attachment>();
        serviceSpecification.attachment.add(Attachment.createProto());

        serviceSpecification.relatedParty = new ArrayList<RelatedParty>();
        serviceSpecification.relatedParty.add(RelatedParty.createProto());

        serviceSpecification.serviceSpecificationRelationship = new ArrayList<SpecificationRelationship>();
        serviceSpecification.serviceSpecificationRelationship.add(SpecificationRelationship.createProto());

        serviceSpecification.requiredServiceSpecification = new ArrayList<RequiredSpecification>();
        serviceSpecification.requiredServiceSpecification.add(RequiredSpecification.createProto());

        serviceSpecification.requiredResourceSpecification = new ArrayList<RequiredSpecification>();
        serviceSpecification.requiredResourceSpecification.add(RequiredSpecification.createProto());

        serviceSpecification.serviceSpecCharacteristic = new ArrayList<ServiceSpecCharacteristic>();
        serviceSpecification.serviceSpecCharacteristic.add(ServiceSpecCharacteristic.createProto());

        return serviceSpecification;
    }

}
