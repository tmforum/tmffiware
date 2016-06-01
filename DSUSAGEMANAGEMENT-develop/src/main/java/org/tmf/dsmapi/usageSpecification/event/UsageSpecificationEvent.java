package org.tmf.dsmapi.usageSpecification.event;

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
import org.tmf.dsmapi.usage.model.UsageSpecification;

@XmlRootElement
@Entity
@Table(name = "Event_UsageSpecification")
@JsonPropertyOrder(value = {"eventId", "eventTime", "eventType", "event"})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class UsageSpecificationEvent implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty("eventId")
    private String id;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonSerialize(using = CustomJsonDateSerializer.class)
    private Date eventTime;
    @Enumerated(value = EnumType.STRING)
    private UsageSpecificationEventTypeEnum eventType;
    @JsonIgnore
    private UsageSpecification resource; //check for object
    @JsonIgnore
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

    public UsageSpecificationEventTypeEnum getEventType() {
        return eventType;
    }

    public void setEventType(UsageSpecificationEventTypeEnum eventType) {
        this.eventType = eventType;
    }

    @JsonAutoDetect(fieldVisibility = ANY)
    class EventBody {

        private UsageSpecification usageSpecification;

        public UsageSpecification getUsageSpecification() {
            return usageSpecification;
        }

        public EventBody(UsageSpecification usageSpecification) {
            this.usageSpecification = usageSpecification;
        }
    }

    @JsonProperty("event")
    public EventBody getEvent() {

        return new EventBody(getResource());
    }

    @JsonIgnore
    public UsageSpecification getResource() {


        return resource;
    }

    public void setResource(UsageSpecification resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return "UsageSpecificationEvent{" + "id=" + id + ", eventTime=" + eventTime + ", eventType=" + eventType + ", resource=" + resource + '}';
    }
}
