package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.scope.ApplicationScope;
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
 * Assignment entity linking UserAccount and ApplicationScope.
 *
 * Purpose:
 * - A user exists globally in IDM (single UserAccount).
 * - Access to a specific application+stage requires an explicit assignment.
 *
 * Notes:
 * - Unidirectional relations only.
 * - LAZY relations to avoid loading graphs unintentionally.
 */
@Entity
@Table(
        name = "idm_user_application_scope_assignment",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_account_id", "application_scope_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class UserApplicationScopeAssignment extends DomainEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_account_id", nullable = false)
    private UserAccount userAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_scope_id", nullable = false)
    private ApplicationScope applicationScope;
}