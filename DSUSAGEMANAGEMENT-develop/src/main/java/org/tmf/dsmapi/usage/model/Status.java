//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.7 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2015.03.31 à 05:37:17 PM CEST 
//


package org.tmf.dsmapi.usage.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;


/**
 * <p>Classe Java pour Status.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="Status">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Received"/>
 *     &lt;enumeration value="Rejected"/>
 *     &lt;enumeration value="Recycled"/>
 *     &lt;enumeration value="Guided"/>
 *     &lt;enumeration value="Rated"/>
 *     &lt;enumeration value="Rerate"/>
 *     &lt;enumeration value="Billed"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "Status")
@XmlEnum
public enum Status {

    @XmlEnumValue("Received")
    Received("Received"),
    @XmlEnumValue("Rejected")
    Rejected("Rejected"),
    @XmlEnumValue("Recycled")
    Recycled("Recycled"),
    @XmlEnumValue("Guided")
    Guided("Guided"),
    @XmlEnumValue("Rated")
    Rated("Rated"),
    @XmlEnumValue("Rerate")
    Rerate("Rerate"),
    @XmlEnumValue("Billed")
    Billed("Billed");
    private final String value;

    Status(String v) {
        value = v;
    }

    @JsonValue
    public String value() {
        return value;
    }

    
    @JsonCreator
    public static Status fromValue(String v) {
        for (Status c: Status.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    @Override
    public String toString() {
        return value();
    }

}
