//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.7 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2015.03.31 à 05:37:17 PM CEST 
//


package org.tmf.dsmapi.usage.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.tmf.dsmapi.commons.utils.CustomJsonDateSerializer;


/**
 * <p>Classe Java pour Usage complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="Usage">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="href" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="date" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="status" type="{http://orange.com/api/usageManagement/tmf/v2/model/business}Status" minOccurs="0"/>
 *         &lt;element name="usageSpecification" type="{http://orange.com/api/usageManagement/tmf/v2/model/business}UsageSpecification" minOccurs="0"/>
 *         &lt;element name="usageCharacteristic" type="{http://orange.com/api/usageManagement/tmf/v2/model/business}UsageCharacteristic" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="relatedParty" type="{http://orange.com/api/usageManagement/tmf/v2/model/business}RelatedParty" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ratedProductUsage" type="{http://orange.com/api/usageManagement/tmf/v2/model/business}RatedProductUsage" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Usage", propOrder = {
    "id",
    "href",
    "date",
    "type",
    "description",
    "status",
    "usageSpecification",
    "usageCharacteristic",
    "relatedParty",
    "ratedProductUsage"
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Entity(name = "Usage")
@Table(name = "USAGE_")
@Inheritance(strategy = InheritanceType.JOINED)
public class Usage
    implements Serializable
{

    private final static long serialVersionUID = 11L;
    protected Long id;
    protected String href;
    @XmlElement(type = String.class)
    @JsonSerialize(using = CustomJsonDateSerializer.class)
    @XmlSchemaType(name = "dateTime")
    protected Date date;
    protected String type;
    protected String description;
    protected Status status;
    protected Reference usageSpecification;
    protected List<UsageCharacteristic> usageCharacteristic;
    protected List<RelatedParty> relatedParty;
    protected List<RatedProductUsage> ratedProductUsage;

    /**
     * Obtient la valeur de la propriété id.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    @Id
    @Column(name = "ID", scale = 0)
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    /**
     * Définit la valeur de la propriété id.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setId(Long value) {
        this.id = value;
    }

    /**
     * Obtient la valeur de la propriété href.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Basic
    @Column(name = "HREF", length = 255)
    public String getHref() {
        return href;
    }

    /**
     * Définit la valeur de la propriété href.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHref(String value) {
        this.href = value;
    }

    /**
     * Obtient la valeur de la propriété date.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Basic
    @Column(name = "DATE_")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getDate() {
        return date;
    }

    /**
     * Définit la valeur de la propriété date.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDate(Date value) {
        this.date = value;
    }

    /**
     * Obtient la valeur de la propriété type.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Basic
    @Column(name = "TYPE_", length = 255)
    public String getType() {
        return type;
    }

    /**
     * Définit la valeur de la propriété type.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Obtient la valeur de la propriété description.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Basic
    @Column(name = "DESCRIPTION", length = 255)
    public String getDescription() {
        return description;
    }

    /**
     * Définit la valeur de la propriété description.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Obtient la valeur de la propriété status.
     * 
     * @return
     *     possible object is
     *     {@link Status }
     *     
     */
    @Basic
    @Column(name = "STATUS", length = 255)
    @Enumerated(EnumType.STRING)
    public Status getStatus() {
        return status;
    }

    /**
     * Définit la valeur de la propriété status.
     * 
     * @param value
     *     allowed object is
     *     {@link Status }
     *     
     */
    public void setStatus(Status value) {
        this.status = value;
    }

    /**
     * Obtient la valeur de la propriété usageSpecification.
     * 
     * @return
     *     possible object is
     *     {@link UsageSpecification }
     *     
     */
    @ManyToOne(targetEntity = Reference.class, cascade = {
        CascadeType.ALL
    })
    @JoinColumn(name = "USAGE_SPECIFICATION_USAGE__ID")
    public Reference getUsageSpecification() {
        return usageSpecification;
    }

    /**
     * Définit la valeur de la propriété usageSpecification.
     * 
     * @param value
     *     allowed object is
     *     {@link UsageSpecification }
     *     
     */
    public void setUsageSpecification(Reference value) {
        this.usageSpecification = value;
    }

    /**
     * Gets the value of the usageCharacteristic property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the usageCharacteristic property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUsageCharacteristic().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link UsageCharacteristic }
     * 
     * 
     */
    @OneToMany(targetEntity = UsageCharacteristic.class, cascade = {
        CascadeType.ALL
    })
    @JoinColumn(name = "USAGE_CHARACTERISTIC_USAGE___0")
    public List<UsageCharacteristic> getUsageCharacteristic() {
        if (usageCharacteristic == null) {
            usageCharacteristic = new ArrayList<UsageCharacteristic>();
        }
        return this.usageCharacteristic;
    }

    /**
     * 
     * 
     */
    public void setUsageCharacteristic(List<UsageCharacteristic> usageCharacteristic) {
        this.usageCharacteristic = usageCharacteristic;
    }

    /**
     * Gets the value of the relatedParty property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the relatedParty property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRelatedParty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RelatedParty }
     * 
     * 
     */
    @OneToMany(targetEntity = RelatedParty.class, cascade = {
        CascadeType.ALL
    })
    @JoinColumn(name = "RELATED_PARTY_USAGE__ID")
    public List<RelatedParty> getRelatedParty() {
        if (relatedParty == null) {
            relatedParty = new ArrayList<RelatedParty>();
        }
        return this.relatedParty;
    }

    /**
     * 
     * 
     */
    public void setRelatedParty(List<RelatedParty> relatedParty) {
        this.relatedParty = relatedParty;
    }

    /**
     * Gets the value of the ratedProductUsage property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ratedProductUsage property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRatedProductUsage().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RatedProductUsage }
     * 
     * 
     */
    @OneToMany(targetEntity = RatedProductUsage.class, cascade = {
        CascadeType.ALL
    })
    @JoinColumn(name = "RATED_PRODUCT_USAGE_USAGE__ID")
    public List<RatedProductUsage> getRatedProductUsage() {
        if (ratedProductUsage == null) {
            ratedProductUsage = new ArrayList<RatedProductUsage>();
        }
        return this.ratedProductUsage;
    }

    /**
     * 
     * 
     */
    public void setRatedProductUsage(List<RatedProductUsage> ratedProductUsage) {
        this.ratedProductUsage = ratedProductUsage;
    }

}
