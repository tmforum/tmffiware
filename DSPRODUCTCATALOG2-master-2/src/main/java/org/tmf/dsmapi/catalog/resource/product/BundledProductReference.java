package org.tmf.dsmapi.catalog.resource.product;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonUnwrapped;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.tmf.dsmapi.catalog.client.CatalogClient;
import org.tmf.dsmapi.catalog.resource.AbstractEntity;
import org.tmf.dsmapi.catalog.resource.LifecycleStatus;
import org.tmf.dsmapi.commons.AbstractEntityReference;
import org.tmf.dsmapi.commons.Utilities;

/**
 *
 * @author bahman.barzideh
 *
 * The prefix 'referenced' was added to the property names of this class to work
 * around an issue in the platform.  Without the prefix, you could not update the
 * id field of entity properties that were of this class.  For example, attempting
 * to update or edit the ProductSpecification.bundledProductSpecification[n].id
 * would throw an exception.  The exception would claim the operation was attempting
 * to update a key field (the real key field is named ENTITY_ID in the database).
 * The 'referenced' prefix fixes this issue while making this class a bit uglier
 * than it needs to be.
 *
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Embeddable
public class BundledProductReference extends AbstractEntityReference implements Serializable {
    public final static long serialVersionUID = 1L;

    @Column(name = "REFERENCED_ID", nullable = true)
    @JsonProperty(value = "id")
    private String referencedId;

    @Column(name = "REFERENCED_HREF", nullable = true)
    @JsonProperty(value = "href")
    private String referencedHref;

    @Column(name = "REFERENCED_NAME", nullable = true)
    @JsonProperty(value = "name")
    private String referencedName;


    @Column(name = "REFERENCED_LIFECYCLE_STATUS", nullable = true)
    @JsonProperty(value = "lifecycleStatus")
    private LifecycleStatus referencedLifecycleStatus;

    @Transient
    @JsonUnwrapped
    private AbstractEntity entity;

    public BundledProductReference() {
        entity = null;
    }

    public String getReferencedId() {
        return referencedId;
    }

    public void setReferencedId(String referencedId) {
        this.referencedId = referencedId;
    }

    public String getReferencedHref() {
        return referencedHref;
    }

    public void setReferencedHref(String referencedHref) {
        this.referencedHref = referencedHref;
    }

    public String getReferencedName() {
        return referencedName;
    }

    public void setReferencedName(String referencedName) {
        this.referencedName = referencedName;
    }

    public LifecycleStatus getReferencedLifecycleStatus() {
        return referencedLifecycleStatus;
    }

    public void setReferencedLifecycleStatus(LifecycleStatus referencedLifecycleStatus) {
        this.referencedLifecycleStatus = referencedLifecycleStatus;
    }

    public AbstractEntity getEntity() {
        return entity;
    }

    public void setEntity(AbstractEntity entity) {
        this.entity = entity;
    }

    @JsonProperty(value = "id")
    public String idToJson() {
        return (entity == null) ? referencedId : null;
    }

    @JsonProperty(value = "href")
    public String hrefToJson() {
        return (entity == null) ? referencedHref : null;
    }

    @JsonProperty(value = "name")
    public String nameToJson() {
        return (entity == null) ? referencedName : null;
    }

    @JsonProperty(value = "lifecycleStatus")
    public LifecycleStatus lifecycleStatusToJson() {
        return (entity == null) ? referencedLifecycleStatus : null;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = 79 * hash + (this.referencedId != null ? this.referencedId.hashCode() : 0);
        hash = 79 * hash + (this.referencedHref != null ? this.referencedHref.hashCode() : 0);
        hash = 79 * hash + (this.referencedName != null ? this.referencedName.hashCode() : 0);
        hash = 79 * hash + (this.referencedLifecycleStatus != null ? this.referencedLifecycleStatus.hashCode() : 0);

        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        final BundledProductReference other = (BundledProductReference) object;
        if (Utilities.areEqual(this.referencedId, other.referencedId) == false) {
            return false;
        }

        if (Utilities.areEqual(this.referencedHref, other.referencedHref) == false) {
            return false;
        }

        if (Utilities.areEqual(this.referencedName, other.referencedName) == false) {
            return false;
        }

        if (Utilities.areEqual(this.referencedLifecycleStatus, other.referencedLifecycleStatus) == false) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "BundledProductReference{" + "referencedId=" + referencedId + ", referencedHref=" + referencedHref + ", referencedName=" + referencedName + ", referencedLifecycleStatus=" + referencedLifecycleStatus + ", entity=" + entity + '}';
    }

    @Override
    public void fetchEntity(Class theClass, int depth) {
        entity = (AbstractEntity) CatalogClient.getObject(referencedHref, theClass, depth);
    }

    public static BundledProductReference createProto() {
        BundledProductReference bundledProductReference = new BundledProductReference ();

        bundledProductReference.referencedId = "id";
        bundledProductReference.referencedHref = "href";
        bundledProductReference.referencedName = "name";
        bundledProductReference.referencedLifecycleStatus = LifecycleStatus.ACTIVE;
        bundledProductReference.entity = null;

        return bundledProductReference;
    }

}
