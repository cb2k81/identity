package de.cocondo.app.domain.idm.auth.session;

import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.user.UserAccount;
import de.cocondo.app.system.entity.DomainEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Persisted server-side authentication session for refresh / logout lifecycle control.
 *
 * Notes:
 * - This is NOT a classic servlet container session.
 * - It represents the server-controlled refresh/login context for a user in a concrete ApplicationScope.
 * - The refresh token itself is never stored in plain text. Only a secure derived representation is persisted.
 */
@Entity
@Table(
        name = "idm_auth_session",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"refresh_token_hash"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class AuthSession extends DomainEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_account_id", nullable = false)
    private UserAccount userAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_scope_id", nullable = false)
    private ApplicationScope applicationScope;

    /**
     * Secure server-side representation of the refresh token.
     *
     * Never store the raw client token value.
     */
    @Column(name = "refresh_token_hash", nullable = false, length = 128)
    private String refreshTokenHash;

    /**
     * Absolute expiration timestamp of this auth session.
     */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AuthSessionStatus status = AuthSessionStatus.ACTIVE;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoked_reason", length = 255)
    private String revokedReason;

    public boolean isActive() {
        return this.status == AuthSessionStatus.ACTIVE;
    }

    public boolean isExpired(Instant now) {
        return this.expiresAt != null && this.expiresAt.isBefore(now);
    }

    public void revoke(String reason, Instant now) {
        this.status = AuthSessionStatus.REVOKED;
        this.revokedAt = now;
        this.revokedReason = reason;
    }

    public void markExpired() {
        this.status = AuthSessionStatus.EXPIRED;
    }
}