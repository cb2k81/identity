package de.cocondo.app.system.event;

import org.springframework.context.ApplicationEvent;

import java.io.Serializable;
import java.time.Clock;

public class DomainEvent<T extends Serializable> extends ApplicationEvent {
    private String eventName;
    private T payload;

    public DomainEvent(Object source) {
        super(source);
    }

    public DomainEvent(Object source, Clock clock) {
        super(source, clock);
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "DomainEvent{" +
                "eventName='" + eventName + '\'' +
                ", payload=" + payload +
                ", timestamp=" + getTimestamp() +
                '}';
    }

}
