package de.cocondo.app.system.entity;

import jakarta.persistence.PrePersist;
import lombok.extern.slf4j.Slf4j;

/**
 * Technical JPA safety listener.
 *
 * Purpose:
 * - Ensures that every DomainEntity has an ID before persistence.
 * - Normally the ID is generated in the DomainEntity constructor.
 * - This listener acts only as a safety net.
 *
 * Important:
 * - No Spring dependencies.
 * - No static injection.
 * - No business logic.
 */
@Slf4j
public class DomainEntityListener {

    @PrePersist
    public void prePersist(DomainEntity entity) {

        if (entity.getId() == null || entity.getId().isBlank()) {

            log.warn("DomainEntity without ID detected during PrePersist: entityClass={}",
                    entity.getClass().getSimpleName());

            // Absolute fallback (should never happen if constructor is correct)
            entity.setId(java.util.UUID.randomUUID().toString());

        } else {
            log.debug("PrePersist check passed: entityClass={}, id={}",
                    entity.getClass().getSimpleName(),
                    entity.getId());
        }
    }
}