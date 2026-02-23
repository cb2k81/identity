package de.cocondo.app.system.entity;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuditingEntityListener {

    private static final Logger logger = LoggerFactory.getLogger(AuditingEntityListener.class);

    @PrePersist
    public void prePersist(Object entity) {
        logger.debug("AuditingEntityListener prePersist triggered for Entity: {}", entity.getClass().getSimpleName());

        if (entity instanceof Auditable auditable) {
            setCreationAttributes(auditable);
            setLastModifiedAttributes(auditable); // Set last modified attributes as well
        }
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        logger.debug("AuditingEntityListener preUpdate triggered for Entity: {}", entity.getClass().getSimpleName());

        if (entity instanceof Auditable auditable) {
            setLastModifiedAttributes(auditable);
        }
    }

    private void setCreationAttributes(Auditable auditable) {
        if (auditable.getCreatedBy() == null) {
            auditable.setCreatedBy("SYSTEM");
        }
        if (auditable.getCreatedAt() == null) {
            auditable.setCreatedAt(LocalDateTime.now());
            logger.debug("Set createdAt for Entity: {} to {}", auditable.getClass().getSimpleName(), auditable.getCreatedAt());
        }
        // Set last modified attributes to the same values on creation
        auditable.setLastModifiedAt(auditable.getCreatedAt());
        auditable.setLastModifiedBy(auditable.getCreatedBy());
        logger.debug("Set lastModifiedAt for Entity: {} to {}", auditable.getClass().getSimpleName(), auditable.getLastModifiedAt());
    }

    private void setLastModifiedAttributes(Auditable auditable) {
        auditable.setLastModifiedAt(LocalDateTime.now());
        auditable.setLastModifiedBy("SYSTEM");
        logger.debug("Updated lastModifiedAt for Entity: {} to {}", auditable.getClass().getSimpleName(), auditable.getLastModifiedAt());
    }
}
