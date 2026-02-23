package de.cocondo.app.system.entity;

import de.cocondo.app.system.core.id.IdGeneratorService;
import jakarta.persistence.PrePersist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Technical JPA listener.
 * Generates IDs for DomainEntity instances if not set.
 */
@Component
public class DomainEntityListener {

    private static IdGeneratorService idGeneratorService;

    @Autowired
    public void setIdGeneratorService(IdGeneratorService service) {
        DomainEntityListener.idGeneratorService = service;
    }

    @PrePersist
    public void prePersist(DomainEntity entity) {
        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(idGeneratorService.generateId());
        }
    }
}
