package de.cocondo.app.domain.idm.permission;

import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.system.entity.DomainEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Groups logically related permissions for easier assignment and UI organization.
 *
 * Notes:
 * - Bound to an ApplicationScope (application + stage).
 * - No bidirectional relations (performance, simplicity).
 */
@Entity
@Table(
        name = "idm_permission_group",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"application_scope_id", "name"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class PermissionGroup extends DomainEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_scope_id", nullable = false)
    private ApplicationScope applicationScope;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "description", length = 512)
    private String description;
}