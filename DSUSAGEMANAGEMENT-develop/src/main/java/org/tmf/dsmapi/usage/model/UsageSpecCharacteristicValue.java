//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.7 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2015.03.31 à 05:37:17 PM CEST 
//


package org.tmf.dsmapi.usage.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;


/**
 * <p>Classe Java pour UsageSpecCharacteristicValue complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="UsageSpecCharacteristicValue">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="valueType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="default" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="valueFrom" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="valueTo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UsageSpecCharacteristicValue", propOrder = {
    "valueType",
    "_default",
    "value",
    "valueFrom",
    "valueTo"
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Entity(name = "UsageSpecCharacteristicValue")
@Table(name = "USAGE_SPEC_CHARACTERISTIC_VA_0")
@Inheritance(strategy = InheritanceType.JOINED)
public class UsageSpecCharacteristicValue
    implements Serializable
{

    private final static long serialVersionUID = 11L;
    protected String valueType;
    @XmlElement(name = "default")
    protected Boolean _default;
    protected String value;
    protected String valueFrom;
    protected String valueTo;
    @JsonIgnore
    protected Long hjid;

    /**
     * Obtient la valeur de la propriété valueType.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Basic
    @Column(name = "VALUE_TYPE", length = 255)
    public String getValueType() {
        return valueType;
    }

    /**
     * Définit la valeur de la propriété valueType.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValueType(String value) {
        this.valueType = value;
    }

    /**
     * Obtient la valeur de la propriété default.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    @Basic
    @Column(name = "DEFAULT_")
    public Boolean isDefault() {
        return _default;
    }

    /**
     * Définit la valeur de la propriété default.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDefault(Boolean value) {
        this._default = value;
    }

    /**
     * Obtient la valeur de la propriété value.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Basic
    @Column(name = "VALUE_", length = 255)
    public String getValue() {
        return value;
    }

    /**
     * Définit la valeur de la propriété value.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Obtient la valeur de la propriété valueFrom.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Basic
    @Column(name = "VALUE_FROM", length = 255)
    public String getValueFrom() {
        return valueFrom;
    }

    /**
     * Définit la valeur de la propriété valueFrom.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValueFrom(String value) {
        this.valueFrom = value;
    }

    /**
     * Obtient la valeur de la propriété valueTo.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Basic
    @Column(name = "VALUE_TO", length = 255)
    public String getValueTo() {
        return valueTo;
    }

    /**
     * Définit la valeur de la propriété valueTo.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValueTo(String value) {
        this.valueTo = value;
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
