//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.7 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2015.03.17 à 04:05:01 PM CET 
//


package org.tmf.dsmapi.product.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;


/**
 * <p>Classe Java pour State.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="State">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Created"/>
 *     &lt;enumeration value="Pending_active"/>
 *     &lt;enumeration value="Aborted"/>
 *     &lt;enumeration value="Cancelled"/>
 *     &lt;enumeration value="Active"/>
 *     &lt;enumeration value="Pending_terminate"/>
 *     &lt;enumeration value="Terminated"/>
 *     &lt;enumeration value="Suspended"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "State")
@XmlEnum
public enum State {

    @XmlEnumValue("Created")
    Created("Created"),
    @XmlEnumValue("Pending_active")
    Pending_active("Pending_active"),
    @XmlEnumValue("Aborted")
    Aborted("Aborted"),
    @XmlEnumValue("Cancelled")
    Cancelled("Cancelled"),
    @XmlEnumValue("Active")
    Active("Active"),
    @XmlEnumValue("Pending_terminate")
    Pending_terminate("Pending_terminate"),
    @XmlEnumValue("Terminated")
    Terminated("Terminated"),
    @XmlEnumValue("Suspended")
    Suspended("Suspended");
    private final String value;

    State(String v) {
        value = v;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static State fromValue(String v) {
        for (State c: State.values()) {
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
