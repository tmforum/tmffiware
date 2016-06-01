//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.7 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2015.03.17 à 04:05:01 PM CET 
//


package org.tmf.dsmapi.product.model;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.tmf.dsmapi.product.model package. 
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

    private final static QName _Agreement_QNAME = new QName("http://orange.com/api/productInventory/tmf/v2/model/business", "Agreement");
    private final static QName _Price_QNAME = new QName("http://orange.com/api/productInventory/tmf/v2/model/business", "Price");
    private final static QName _RelatedParty_QNAME = new QName("http://orange.com/api/productInventory/tmf/v2/model/business", "RelatedParty");
    private final static QName _ProductCharacteristic_QNAME = new QName("http://orange.com/api/productInventory/tmf/v2/model/business", "ProductCharacteristic");
    private final static QName _ProductSpecification_QNAME = new QName("http://orange.com/api/productInventory/tmf/v2/model/business", "ProductSpecification");
    private final static QName _RealizingResource_QNAME = new QName("http://orange.com/api/productInventory/tmf/v2/model/business", "RealizingResource");
    private final static QName _RealizingService_QNAME = new QName("http://orange.com/api/productInventory/tmf/v2/model/business", "RealizingService");
    private final static QName _ProductRelationship_QNAME = new QName("http://orange.com/api/productInventory/tmf/v2/model/business", "ProductRelationship");
    private final static QName _ProductOffering_QNAME = new QName("http://orange.com/api/productInventory/tmf/v2/model/business", "ProductOffering");
    private final static QName _Product_QNAME = new QName("http://orange.com/api/productInventory/tmf/v2/model/business", "Product");
    private final static QName _BillingAccount_QNAME = new QName("http://orange.com/api/productInventory/tmf/v2/model/business", "BillingAccount");
    private final static QName _ProductPrice_QNAME = new QName("http://orange.com/api/productInventory/tmf/v2/model/business", "ProductPrice");
    private final static QName _ProductRef_QNAME = new QName("http://orange.com/api/productInventory/tmf/v2/model/business", "ProductRef");
    private final static QName _ValidFor_QNAME = new QName("http://orange.com/api/productInventory/tmf/v2/model/business", "ValidFor");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.tmf.dsmapi.product.model
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
     * Create an instance of {@link Product }
     * 
     */
    public Product createProduct() {
        return new Product();
    }

    /**
     * Create an instance of {@link Agreement }
     * 
     */
    public Agreement createAgreement() {
        return new Agreement();
    }

    /**
     * Create an instance of {@link ProductRelationship }
     * 
     */
    public ProductRelationship createProductRelationship() {
        return new ProductRelationship();
    }

    /**
     * Create an instance of {@link Price }
     * 
     */
    public Price createPrice() {
        return new Price();
    }

    /**
     * Create an instance of {@link ProductOffering }
     * 
     */
    public ProductOffering createProductOffering() {
        return new ProductOffering();
    }

    /**
     * Create an instance of {@link ValidFor }
     * 
     */
    public ValidFor createValidFor() {
        return new ValidFor();
    }

    /**
     * Create an instance of {@link ProductRef }
     * 
     */
    public ProductRef createProductRef() {
        return new ProductRef();
    }

    /**
     * Create an instance of {@link RealizingResource }
     * 
     */
    public RealizingResource createRealizingResource() {
        return new RealizingResource();
    }

    /**
     * Create an instance of {@link RealizingService }
     * 
     */
    public RealizingService createRealizingService() {
        return new RealizingService();
    }

    /**
     * Create an instance of {@link ProductCharacteristic }
     * 
     */
    public ProductCharacteristic createProductCharacteristic() {
        return new ProductCharacteristic();
    }

    /**
     * Create an instance of {@link ProductSpecification }
     * 
     */
    public ProductSpecification createProductSpecification() {
        return new ProductSpecification();
    }

    /**
     * Create an instance of {@link ProductPrice }
     * 
     */
    public ProductPrice createProductPrice() {
        return new ProductPrice();
    }

    /**
     * Create an instance of {@link BillingAccount }
     * 
     */
    public BillingAccount createBillingAccount() {
        return new BillingAccount();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Agreement }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orange.com/api/productInventory/tmf/v2/model/business", name = "Agreement")
    public JAXBElement<Agreement> createAgreement(Agreement value) {
        return new JAXBElement<Agreement>(_Agreement_QNAME, Agreement.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Price }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orange.com/api/productInventory/tmf/v2/model/business", name = "Price")
    public JAXBElement<Price> createPrice(Price value) {
        return new JAXBElement<Price>(_Price_QNAME, Price.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RelatedParty }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orange.com/api/productInventory/tmf/v2/model/business", name = "RelatedParty")
    public JAXBElement<RelatedParty> createRelatedParty(RelatedParty value) {
        return new JAXBElement<RelatedParty>(_RelatedParty_QNAME, RelatedParty.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProductCharacteristic }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orange.com/api/productInventory/tmf/v2/model/business", name = "ProductCharacteristic")
    public JAXBElement<ProductCharacteristic> createProductCharacteristic(ProductCharacteristic value) {
        return new JAXBElement<ProductCharacteristic>(_ProductCharacteristic_QNAME, ProductCharacteristic.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProductSpecification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orange.com/api/productInventory/tmf/v2/model/business", name = "ProductSpecification")
    public JAXBElement<ProductSpecification> createProductSpecification(ProductSpecification value) {
        return new JAXBElement<ProductSpecification>(_ProductSpecification_QNAME, ProductSpecification.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RealizingResource }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orange.com/api/productInventory/tmf/v2/model/business", name = "RealizingResource")
    public JAXBElement<RealizingResource> createRealizingResource(RealizingResource value) {
        return new JAXBElement<RealizingResource>(_RealizingResource_QNAME, RealizingResource.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RealizingService }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orange.com/api/productInventory/tmf/v2/model/business", name = "RealizingService")
    public JAXBElement<RealizingService> createRealizingService(RealizingService value) {
        return new JAXBElement<RealizingService>(_RealizingService_QNAME, RealizingService.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProductRelationship }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orange.com/api/productInventory/tmf/v2/model/business", name = "ProductRelationship")
    public JAXBElement<ProductRelationship> createProductRelationship(ProductRelationship value) {
        return new JAXBElement<ProductRelationship>(_ProductRelationship_QNAME, ProductRelationship.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProductOffering }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orange.com/api/productInventory/tmf/v2/model/business", name = "ProductOffering")
    public JAXBElement<ProductOffering> createProductOffering(ProductOffering value) {
        return new JAXBElement<ProductOffering>(_ProductOffering_QNAME, ProductOffering.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Product }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orange.com/api/productInventory/tmf/v2/model/business", name = "Product")
    public JAXBElement<Product> createProduct(Product value) {
        return new JAXBElement<Product>(_Product_QNAME, Product.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BillingAccount }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orange.com/api/productInventory/tmf/v2/model/business", name = "BillingAccount")
    public JAXBElement<BillingAccount> createBillingAccount(BillingAccount value) {
        return new JAXBElement<BillingAccount>(_BillingAccount_QNAME, BillingAccount.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProductPrice }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orange.com/api/productInventory/tmf/v2/model/business", name = "ProductPrice")
    public JAXBElement<ProductPrice> createProductPrice(ProductPrice value) {
        return new JAXBElement<ProductPrice>(_ProductPrice_QNAME, ProductPrice.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProductRef }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orange.com/api/productInventory/tmf/v2/model/business", name = "ProductRef")
    public JAXBElement<ProductRef> createProductRef(ProductRef value) {
        return new JAXBElement<ProductRef>(_ProductRef_QNAME, ProductRef.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ValidFor }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orange.com/api/productInventory/tmf/v2/model/business", name = "ValidFor")
    public JAXBElement<ValidFor> createValidFor(ValidFor value) {
        return new JAXBElement<ValidFor>(_ValidFor_QNAME, ValidFor.class, null, value);
    }

}
