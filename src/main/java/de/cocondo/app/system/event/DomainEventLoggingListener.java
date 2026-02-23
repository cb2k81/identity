package de.cocondo.app.system.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.cocondo.app.system.core.util.serialize.PayloadSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class DomainEventLoggingListener {

    private static final Logger logger = LoggerFactory.getLogger(DomainEventLoggingListener.class);

    private final DomainEventEntityRepository domainEventEntityRepository;
    private final PayloadSerializer payloadSerializer;

    @Autowired
    public DomainEventLoggingListener(
            DomainEventEntityRepository domainEventEntityRepository,
            PayloadSerializer payloadSerializer
    ) {
        this.domainEventEntityRepository = domainEventEntityRepository;
        this.payloadSerializer = payloadSerializer;
        logger.info("Domain Event Listener initialized: " + DomainEventLoggingListener.class.getName());
    }

    @EventListener
    public void handleDomainEvent(DomainEvent<? extends Serializable> domainEvent) {
        logger.debug("Domain event emitted, name=" + domainEvent.getEventName()
                + ", timestamp=" + domainEvent.getTimestamp()
                + ", source=" + domainEvent.getSource().getClass().getName()
        );
        logger.debug("Domain event emitted {}", domainEvent);

        // Convert Long-Timestamp into LocalDateTime
        Instant instant = Instant.ofEpochMilli(domainEvent.getTimestamp());
        LocalDateTime timestamp = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();

        try {
            // Convert payload into JSON
            String payloadJson = payloadSerializer.serializePayload(domainEvent.getPayload());

            // Create event entity
            DomainEventEntity domainEventEntity = new DomainEventEntity();
            domainEventEntity.setEventName(domainEvent.getEventName());
            domainEventEntity.setTimestamp(timestamp);
            domainEventEntity.setPayload(payloadJson);
            domainEventEntity.setSourceClassName(domainEvent.getSource().getClass().getName());

            domainEventEntityRepository.save(domainEventEntity);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing payload to JSON", e);
        }
    }
}
