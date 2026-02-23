package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.permission.Permission;
import de.cocondo.app.domain.idm.role.Role;
import de.cocondo.app.system.entity.DomainEntity;
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
 * Assignment entity linking Role and Permission (n:m).
 *
 * Notes:
 * - Unidirectional relations only.
 * - LAZY relations to avoid loading large permission sets unintentionally.
 * - Unique constraint prevents duplicate assignments.
 */
@Entity
@Table(
        name = "idm_role_permission_assignment",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"role_id", "permission_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class RolePermissionAssignment extends DomainEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;
}