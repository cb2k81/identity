package de.cocondo.app.system.event;

import de.cocondo.app.system.core.http.RequestErrorEvent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class EventPublisher {

    private final ApplicationEventPublisher eventPublisher;
    private final ErrorIdService errorIdService;
    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);

    @Autowired
    public EventPublisher(ApplicationEventPublisher eventPublisher, ErrorIdService errorIdService) {
        this.eventPublisher = eventPublisher;
        this.errorIdService = errorIdService;
    }

    public <T extends Serializable> void publishEvent(String eventName, T payload) {
        publishEvent(eventName, payload, this);
    }

    public <T extends Serializable> void publishEvent(String eventName, T payload, Object source) {
        DomainEvent<T> domainEvent = new DomainEvent<>(source);
        domainEvent.setEventName(eventName);
        domainEvent.setPayload(payload);

        logger.debug("Publishing domain event '{}' with payload: {}", eventName, payload);
        eventPublisher.publishEvent(domainEvent);
    }

    public void publishEvent(DomainEvent<?> domainEvent) {
        logger.debug("Publishing domain event '{}' with payload: {}", domainEvent.getEventName(), domainEvent.getPayload());
        eventPublisher.publishEvent(domainEvent);
    }


    @Transactional
    public RequestErrorEvent publishRequestErrorEvent(Object source, Exception exception, HttpServletRequest request) {
        Long errorId = errorIdService.generateErrorId();
        RequestErrorEvent errorEvent = new RequestErrorEvent(source, exception, request, errorId);
        logger.debug("Publishing error event '{}': {}", errorEvent.getErrorType(), errorEvent.getErrorMessage());
        eventPublisher.publishEvent(errorEvent);
        return errorEvent;
    }
}
