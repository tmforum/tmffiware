//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.7 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2015.03.31 à 05:37:17 PM CEST 
//


package org.tmf.dsmapi.usage.model;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.tmf.dsmapi.usage.model package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _RelatedParty_QNAME = new QName("http://orange.com/api/usageManagement/tmf/v2/model/business", "RelatedParty");
    private final static QName _UsageSpecification_QNAME = new QName("http://orange.com/api/usageManagement/tmf/v2/model/business", "UsageSpecification");
    private final static QName _UsageCharacteristic_QNAME = new QName("http://orange.com/api/usageManagement/tmf/v2/model/business", "UsageCharacteristic");
    private final static QName _ValidFor_QNAME = new QName("http://orange.com/api/usageManagement/tmf/v2/model/business", "ValidFor");
    private final static QName _UsageSpecCharacteristicValue_QNAME = new QName("http://orange.com/api/usageManagement/tmf/v2/model/business", "UsageSpecCharacteristicValue");
    private final static QName _Usage_QNAME = new QName("http://orange.com/api/usageManagement/tmf/v2/model/business", "Usage");
    private final static QName _UsageSpecCharacteristic_QNAME = new QName("http://orange.com/api/usageManagement/tmf/v2/model/business", "UsageSpecCharacteristic");
    private final static QName _RatedProductUsage_QNAME = new QName("http://orange.com/api/usageManagement/tmf/v2/model/business", "RatedProductUsage");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.tmf.dsmapi.usage.model
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RelatedParty }
     * 
     */
    public RelatedParty createRelatedParty() {
        return new RelatedParty();
    }

    /**
     * Create an instance of {@link UsageSpecification }
     * 
     */
    public UsageSpecification createUsageSpecification() {
        return new UsageSpecification();
    }

    /**
     * Create an instance of {@link ValidFor }
     * 
     */
    public ValidFor createValidFor() {
        return new ValidFor();
    }

    /**
     * Create an instance of {@link UsageCharacteristic }
     * 
     */
    public UsageCharacteristic createUsageCharacteristic() {
        return new UsageCharacteristic();
    }

    /**
     * Create an instance of {@link UsageSpecCharacteristic }
     * 
     */
    public UsageSpecCharacteristic createUsageSpecCharacteristic() {
        return new UsageSpecCharacteristic();
    }

    /**
     * Create an instance of {@link RatedProductUsage }
     * 
     */
    public RatedProductUsage createRatedProductUsage() {
        return new RatedProductUsage();
    }

    /**
     * Create an instance of {@link UsageSpecCharacteristicValue }
     * 
     */
    public UsageSpecCharacteristicValue createUsageSpecCharacteristicValue() {
        return new UsageSpecCharacteristicValue();
    }

    /**
     * Create an instance of {@link Usage }
     * 
     */
    public Usage createUsage() {
        return new Usage();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RelatedParty }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orange.com/api/usageManagement/tmf/v2/model/business", name = "RelatedParty")
    public JAXBElement<RelatedParty> createRelatedParty(RelatedParty value) {
        return new JAXBElement<RelatedParty>(_RelatedParty_QNAME, RelatedParty.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UsageSpecification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orange.com/api/usageManagement/tmf/v2/model/business", name = "UsageSpecification")
    public JAXBElement<UsageSpecification> createUsageSpecification(UsageSpecification value) {
        return new JAXBElement<UsageSpecification>(_UsageSpecification_QNAME, UsageSpecification.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UsageCharacteristic }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orange.com/api/usageManagement/tmf/v2/model/business", name = "UsageCharacteristic")
    public JAXBElement<UsageCharacteristic> createUsageCharacteristic(UsageCharacteristic value) {
        return new JAXBElement<UsageCharacteristic>(_UsageCharacteristic_QNAME, UsageCharacteristic.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ValidFor }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orange.com/api/usageManagement/tmf/v2/model/business", name = "ValidFor")
    public JAXBElement<ValidFor> createValidFor(ValidFor value) {
        return new JAXBElement<ValidFor>(_ValidFor_QNAME, ValidFor.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UsageSpecCharacteristicValue }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orange.com/api/usageManagement/tmf/v2/model/business", name = "UsageSpecCharacteristicValue")
    public JAXBElement<UsageSpecCharacteristicValue> createUsageSpecCharacteristicValue(UsageSpecCharacteristicValue value) {
        return new JAXBElement<UsageSpecCharacteristicValue>(_UsageSpecCharacteristicValue_QNAME, UsageSpecCharacteristicValue.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Usage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orange.com/api/usageManagement/tmf/v2/model/business", name = "Usage")
    public JAXBElement<Usage> createUsage(Usage value) {
        return new JAXBElement<Usage>(_Usage_QNAME, Usage.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UsageSpecCharacteristic }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orange.com/api/usageManagement/tmf/v2/model/business", name = "UsageSpecCharacteristic")
    public JAXBElement<UsageSpecCharacteristic> createUsageSpecCharacteristic(UsageSpecCharacteristic value) {
        return new JAXBElement<UsageSpecCharacteristic>(_UsageSpecCharacteristic_QNAME, UsageSpecCharacteristic.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RatedProductUsage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orange.com/api/usageManagement/tmf/v2/model/business", name = "RatedProductUsage")
    public JAXBElement<RatedProductUsage> createRatedProductUsage(RatedProductUsage value) {
        return new JAXBElement<RatedProductUsage>(_RatedProductUsage_QNAME, RatedProductUsage.class, null, value);
    }

}
