package de.cocondo.app.domain.idm.scope;

import de.cocondo.app.system.entity.DomainEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a concrete application in a concrete stage (environment).
 *
 * Examples:
 * - (IDM, DEV)
 * - (IDM, PROD)
 * - (PERSONNEL, TEST)
 *
 * This entity enables stage-specific role/permission definitions.
 */
@Entity
@Table(
        name = "idm_application_scope",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"application_key", "stage_key"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ApplicationScope extends DomainEntity {

    @Column(name = "application_key", nullable = false, length = 64)
    private String applicationKey;

    @Column(name = "stage_key", nullable = false, length = 64)
    private String stageKey;

    @Column(name = "description", length = 512)
    private String description;
}