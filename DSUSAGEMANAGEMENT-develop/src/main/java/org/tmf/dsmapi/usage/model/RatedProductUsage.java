//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.7 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2015.03.31 à 05:37:17 PM CEST 
//


package org.tmf.dsmapi.usage.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.tmf.dsmapi.commons.utils.CustomJsonDateSerializer;


/**
 * <p>Classe Java pour RatedProductUsage complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="RatedProductUsage">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ratingDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="usageRatingTag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isBilled" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ratingAmountType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="taxIncludedRatingAmount" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="taxExcludedRatingAmount" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="taxRate" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="isTaxExempt" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="offerTariffType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bucketValueConvertedInAmount" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="currencyCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="productRef" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RatedProductUsage", propOrder = {
    "ratingDate",
    "usageRatingTag",
    "isBilled",
    "ratingAmountType",
    "taxIncludedRatingAmount",
    "taxExcludedRatingAmount",
    "taxRate",
    "isTaxExempt",
    "offerTariffType",
    "bucketValueConvertedInAmount",
    "currencyCode",
    "productRef"
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Entity(name = "RatedProductUsage")
@Table(name = "RATED_PRODUCT_USAGE")
@Inheritance(strategy = InheritanceType.JOINED)
public class RatedProductUsage
    implements Serializable
{

    private final static long serialVersionUID = 11L;
    @XmlElement(type = String.class)
    @JsonSerialize(using = CustomJsonDateSerializer.class)
    @XmlSchemaType(name = "dateTime")
    protected Date ratingDate;
    protected String usageRatingTag;
    protected Boolean isBilled;
    protected String ratingAmountType;
    protected Float taxIncludedRatingAmount;
    protected Float taxExcludedRatingAmount;
    protected Float taxRate;
    protected Boolean isTaxExempt;
    protected String offerTariffType;
    protected Float bucketValueConvertedInAmount;
    protected String currencyCode;
    protected String productRef;
    @JsonIgnore
    protected Long hjid;

    /**
     * Obtient la valeur de la propriété ratingDate.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Basic
    @Column(name = "RATING_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getRatingDate() {
        return ratingDate;
    }

    /**
     * Définit la valeur de la propriété ratingDate.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRatingDate(Date value) {
        this.ratingDate = value;
    }

    /**
     * Obtient la valeur de la propriété usageRatingTag.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Basic
    @Column(name = "USAGE_RATING_TAG", length = 255)
    public String getUsageRatingTag() {
        return usageRatingTag;
    }

    /**
     * Définit la valeur de la propriété usageRatingTag.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsageRatingTag(String value) {
        this.usageRatingTag = value;
    }

    /**
     * Obtient la valeur de la propriété isBilled.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    @Basic
    @Column(name = "IS_BILLED")
    public Boolean isIsBilled() {
        return isBilled;
    }

    /**
     * Définit la valeur de la propriété isBilled.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsBilled(Boolean value) {
        this.isBilled = value;
    }

    /**
     * Obtient la valeur de la propriété ratingAmountType.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Basic
    @Column(name = "RATING_AMOUNT_TYPE", length = 255)
    public String getRatingAmountType() {
        return ratingAmountType;
    }

    /**
     * Définit la valeur de la propriété ratingAmountType.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRatingAmountType(String value) {
        this.ratingAmountType = value;
    }

    /**
     * Obtient la valeur de la propriété taxIncludedRatingAmount.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    @Basic
    @Column(name = "TAX_INCLUDED_RATING_AMOUNT", precision = 20, scale = 10)
    public Float getTaxIncludedRatingAmount() {
        return taxIncludedRatingAmount;
    }

    /**
     * Définit la valeur de la propriété taxIncludedRatingAmount.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setTaxIncludedRatingAmount(Float value) {
        this.taxIncludedRatingAmount = value;
    }

    /**
     * Obtient la valeur de la propriété taxExcludedRatingAmount.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    @Basic
    @Column(name = "TAX_EXCLUDED_RATING_AMOUNT", precision = 20, scale = 10)
    public Float getTaxExcludedRatingAmount() {
        return taxExcludedRatingAmount;
    }

    /**
     * Définit la valeur de la propriété taxExcludedRatingAmount.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setTaxExcludedRatingAmount(Float value) {
        this.taxExcludedRatingAmount = value;
    }

    /**
     * Obtient la valeur de la propriété taxRate.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    @Basic
    @Column(name = "TAX_RATE", precision = 20, scale = 10)
    public Float getTaxRate() {
        return taxRate;
    }

    /**
     * Définit la valeur de la propriété taxRate.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setTaxRate(Float value) {
        this.taxRate = value;
    }

    /**
     * Obtient la valeur de la propriété isTaxExempt.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    @Basic
    @Column(name = "IS_TAX_EXEMPT")
    public Boolean isIsTaxExempt() {
        return isTaxExempt;
    }

    /**
     * Définit la valeur de la propriété isTaxExempt.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsTaxExempt(Boolean value) {
        this.isTaxExempt = value;
    }

    /**
     * Obtient la valeur de la propriété offerTariffType.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Basic
    @Column(name = "OFFER_TARIFF_TYPE", length = 255)
    public String getOfferTariffType() {
        return offerTariffType;
    }

    /**
     * Définit la valeur de la propriété offerTariffType.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOfferTariffType(String value) {
        this.offerTariffType = value;
    }

    /**
     * Obtient la valeur de la propriété bucketValueConvertedInAmount.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    @Basic
    @Column(name = "BUCKET_VALUE_CONVERTED_IN_AM_0", precision = 20, scale = 10)
    public Float getBucketValueConvertedInAmount() {
        return bucketValueConvertedInAmount;
    }

    /**
     * Définit la valeur de la propriété bucketValueConvertedInAmount.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setBucketValueConvertedInAmount(Float value) {
        this.bucketValueConvertedInAmount = value;
    }

    /**
     * Obtient la valeur de la propriété currencyCode.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Basic
    @Column(name = "CURRENCY_CODE", length = 255)
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * Définit la valeur de la propriété currencyCode.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCurrencyCode(String value) {
        this.currencyCode = value;
    }

    /**
     * Obtient la valeur de la propriété productRef.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Basic
    @Column(name = "PRODUCT_REF", length = 255)
    public String getProductRef() {
        return productRef;
    }

    /**
     * Définit la valeur de la propriété productRef.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProductRef(String value) {
        this.productRef = value;
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
