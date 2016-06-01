package org.tmf.dsmapi.usage.event;

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
import org.tmf.dsmapi.usage.model.Usage;

@XmlRootElement
@Entity
@Table(name = "Event_Usage")
@JsonPropertyOrder(value = {"eventId", "eventTime", "eventType", "event"})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class UsageEvent implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty("eventId")
    private String id;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonSerialize(using = CustomJsonDateSerializer.class)
    private Date eventTime;
    @Enumerated(value = EnumType.STRING)
    private UsageEventTypeEnum eventType;
    @JsonIgnore
    private Usage resource; //check for object
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

    public UsageEventTypeEnum getEventType() {
        return eventType;
    }

    public void setEventType(UsageEventTypeEnum eventType) {
        this.eventType = eventType;
    }

    @JsonAutoDetect(fieldVisibility = ANY)
    class EventBody {

        private Usage usage;

        public Usage getUsage() {
            return usage;
        }

        public EventBody(Usage usage) {
            this.usage = usage;
        }
    }

    @JsonProperty("event")
    public EventBody getEvent() {

        return new EventBody(getResource());
    }

    @JsonIgnore
    public Usage getResource() {


        return resource;
    }

    public void setResource(Usage resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return "UsageEvent{" + "id=" + id + ", eventTime=" + eventTime + ", eventType=" + eventType + ", resource=" + resource + '}';
    }
}
