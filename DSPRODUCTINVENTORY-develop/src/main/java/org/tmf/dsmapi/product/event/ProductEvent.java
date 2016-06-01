/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tmf.dsmapi.product.event;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.ANY;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.tmf.dsmapi.commons.utils.CustomJsonDateSerializer;
import org.tmf.dsmapi.product.model.Product;

@XmlRootElement
@Entity
@Table(name = "Event_Product")
@JsonPropertyOrder(value = {"eventId","eventTime", "eventType", "resource"})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ProductEvent implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
//    @JsonProperty("eventId")
    private String id;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonSerialize(using = CustomJsonDateSerializer.class)
    private Date eventTime;

    @Enumerated(value = EnumType.STRING)
    private ProductEventTypeEnum eventType;

//    @JsonIgnore
    private Product resource; //check for object

    @JsonProperty("eventId")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    public ProductEventTypeEnum getEventType() {
        return eventType;
    }

    public void setEventType(ProductEventTypeEnum eventType) {
        this.eventType = eventType;
    }

    @JsonIgnore
    public Product getResource() {
        return resource;
    }

    public void setResource(Product resource) {
        this.resource = resource;
    }

    @JsonAutoDetect(fieldVisibility = ANY)
    class EventBody {
        private Product product;
        public Product getProduct() {
            return product;
        }
        public EventBody(Product product) {
            this.product = product;
        }
    }

    @JsonProperty("event")
    public EventBody getEvent() {
        return new EventBody(getResource());
    }

    @Override
    public String toString() {
        return "ProductInventoryEvent{" + "id=" + id + ", eventTime=" + eventTime + ", eventType=" + eventType + ", event=" + resource + '}';
    }

}
