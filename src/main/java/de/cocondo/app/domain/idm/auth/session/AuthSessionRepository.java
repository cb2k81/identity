package de.cocondo.app.domain.idm.auth.session;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for AuthSession.
 *
 * Responsibilities:
 * - Persistence access only
 * - No business logic
 * - No security logic
 */
public interface AuthSessionRepository extends JpaRepository<AuthSession, String> {

    Optional<AuthSession> findByRefreshTokenHash(String refreshTokenHash);

    List<AuthSession> findAllByUserAccount_Id(String userAccountId);

    List<AuthSession> findAllByUserAccount_IdAndStatus(String userAccountId, AuthSessionStatus status);

    long countByUserAccount_Id(String userAccountId);

    Optional<AuthSession> findTopByUserAccount_IdOrderByCreatedAtDesc(String userAccountId);
}