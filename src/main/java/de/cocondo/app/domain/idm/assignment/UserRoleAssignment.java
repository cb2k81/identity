package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.role.Role;
import de.cocondo.app.domain.idm.user.UserAccount;
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
 * Assignment entity linking UserAccount and Role (n:m).
 *
 * Notes:
 * - Unidirectional relations only.
 * - LAZY relations to avoid unnecessary loading.
 * - Unique constraint prevents duplicate assignments.
 */
@Entity
@Table(
        name = "idm_user_role_assignment",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_account_id", "role_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class UserRoleAssignment extends DomainEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_account_id", nullable = false)
    private UserAccount userAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}