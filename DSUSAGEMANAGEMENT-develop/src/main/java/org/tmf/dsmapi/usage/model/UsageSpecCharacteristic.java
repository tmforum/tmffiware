//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.7 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2015.03.31 à 05:37:17 PM CEST 
//


package org.tmf.dsmapi.usage.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;


/**
 * <p>Classe Java pour UsageSpecCharacteristic complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="UsageSpecCharacteristic">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="configurable" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="usageSpecCharacteristicValue" type="{http://orange.com/api/usageManagement/tmf/v2/model/business}UsageSpecCharacteristicValue" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UsageSpecCharacteristic", propOrder = {
    "name",
    "description",
    "configurable",
    "usageSpecCharacteristicValue"
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Entity(name = "UsageSpecCharacteristic")
@Table(name = "USAGE_SPEC_CHARACTERISTIC")
@Inheritance(strategy = InheritanceType.JOINED)
public class UsageSpecCharacteristic
    implements Serializable
{

    private final static long serialVersionUID = 11L;
    protected String name;
    protected String description;
    protected Boolean configurable;
    protected List<UsageSpecCharacteristicValue> usageSpecCharacteristicValue;
   @JsonIgnore
    protected Long hjid;

    /**
     * Obtient la valeur de la propriété name.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Basic
    @Column(name = "NAME_", length = 255)
    public String getName() {
        return name;
    }

    /**
     * Définit la valeur de la propriété name.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
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
     * Obtient la valeur de la propriété configurable.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    @Basic
    @Column(name = "CONFIGURABLE")
    public Boolean isConfigurable() {
        return configurable;
    }

    /**
     * Définit la valeur de la propriété configurable.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setConfigurable(Boolean value) {
        this.configurable = value;
    }

    /**
     * Gets the value of the usageSpecCharacteristicValue property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the usageSpecCharacteristicValue property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUsageSpecCharacteristicValue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link UsageSpecCharacteristicValue }
     * 
     * 
     */
    @OneToMany(targetEntity = UsageSpecCharacteristicValue.class, cascade = {
        CascadeType.ALL
    })
    @JoinColumn(name = "USAGE_SPEC_CHARACTERISTIC_VA_1")
    public List<UsageSpecCharacteristicValue> getUsageSpecCharacteristicValue() {
        if (usageSpecCharacteristicValue == null) {
            usageSpecCharacteristicValue = new ArrayList<UsageSpecCharacteristicValue>();
        }
        return this.usageSpecCharacteristicValue;
    }

    /**
     * 
     * 
     */
    public void setUsageSpecCharacteristicValue(List<UsageSpecCharacteristicValue> usageSpecCharacteristicValue) {
        this.usageSpecCharacteristicValue = usageSpecCharacteristicValue;
    }

    /**
     * Obtient la valeur de la propriété hjid.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    @Id
    @Column(name = "HJID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    public Long getHjid() {
        return hjid;
    }

    /**
     * Définit la valeur de la propriété hjid.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setHjid(Long value) {
        this.hjid = value;
    }

}
