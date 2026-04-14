package de.cocondo.app.domain.idm.auth.session;

import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.user.UserAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Base64;

/**
 * Lifecycle service for persisted AuthSession objects.
 *
 * Responsibilities:
 * - create a new server-controlled auth session
 * - generate a secure client refresh token
 * - persist only a secure derived representation
 * - validate active sessions
 * - revoke one session
 * - revoke all sessions of a user
 *
 * Important:
 * - This service does not issue JWT access tokens
 * - This service does not contain HTTP/controller logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthSessionLifecycleService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final HexFormat HEX_FORMAT = HexFormat.of();

    private final AuthSessionEntityService authSessionEntityService;

    /**
     * Refresh/session lifetime in milliseconds.
     *
     * Default: 14 days
     */
    @Value("${idm.security.refresh.ttl-ms:1209600000}")
    private long refreshTtlMillis;

    /**
     * Creates a new persisted auth session and returns the client-facing refresh token.
     */
    @Transactional
    public IssuedAuthSession createSession(UserAccount userAccount, ApplicationScope applicationScope) {

        Objects.requireNonNull(userAccount, "userAccount must not be null");
        Objects.requireNonNull(applicationScope, "applicationScope must not be null");

        if (userAccount.getId() == null || userAccount.getId().isBlank()) {
            throw new IllegalArgumentException("userAccount.id must not be blank");
        }
        if (applicationScope.getId() == null || applicationScope.getId().isBlank()) {
            throw new IllegalArgumentException("applicationScope.id must not be blank");
        }
        if (!userAccount.isActive()) {
            throw new IllegalStateException("UserAccount is not active");
        }
        if (refreshTtlMillis <= 0) {
            throw new IllegalStateException("idm.security.refresh.ttl-ms must be > 0");
        }

        String refreshToken = generateRefreshTokenValue();
        String refreshTokenHash = hashRefreshToken(refreshToken);
        Instant expiresAt = Instant.now().plusMillis(refreshTtlMillis);

        AuthSession authSession = new AuthSession();
        authSession.setUserAccount(userAccount);
        authSession.setApplicationScope(applicationScope);
        authSession.setRefreshTokenHash(refreshTokenHash);
        authSession.setExpiresAt(expiresAt);
        authSession.setStatus(AuthSessionStatus.ACTIVE);

        AuthSession saved = authSessionEntityService.save(authSession);

        log.info(
                "AuthSession created: sessionId={}, userId={}, scopeId={}, expiresAt={}",
                saved.getId(),
                userAccount.getId(),
                applicationScope.getId(),
                saved.getExpiresAt()
        );

        return new IssuedAuthSession(
                saved.getId(),
                refreshToken,
                saved.getExpiresAt().toEpochMilli()
        );
    }

    /**
     * Validates that the given refresh token resolves to an active, usable AuthSession.
     *
     * @return validated persisted AuthSession
     */
    @Transactional
    public AuthSession validateActiveSession(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("refreshToken must not be blank");
        }

        String refreshTokenHash = hashRefreshToken(refreshToken);

        AuthSession authSession = authSessionEntityService
                .loadByRefreshTokenHash(refreshTokenHash)
                .orElseThrow(() -> new IllegalArgumentException("AuthSession not found"));

        ensureUsable(authSession);

        return authSession;
    }

    /**
     * Revokes exactly one auth session addressed by the given refresh token.
     */
    @Transactional
    public AuthSession revokeSession(String refreshToken, String reason) {

        AuthSession authSession = validateActiveSession(refreshToken);
        Instant now = Instant.now();

        authSession.revoke(normalizeReason(reason), now);

        AuthSession saved = authSessionEntityService.save(authSession);

        log.info(
                "AuthSession revoked: sessionId={}, userId={}, scopeId={}, reason={}",
                saved.getId(),
                saved.getUserAccount().getId(),
                saved.getApplicationScope().getId(),
                saved.getRevokedReason()
        );

        return saved;
    }

    /**
     * Revokes all currently active auth sessions of the given user.
     *
     * @return number of revoked sessions
     */
    @Transactional
    public int revokeAllSessionsForUser(String userAccountId, String reason) {

        if (userAccountId == null || userAccountId.isBlank()) {
            throw new IllegalArgumentException("userAccountId must not be blank");
        }

        List<AuthSession> activeSessions =
                authSessionEntityService.loadAllActiveByUserAccountId(userAccountId);

        Instant now = Instant.now();
        String normalizedReason = normalizeReason(reason);

        int revokedCount = 0;

        for (AuthSession authSession : activeSessions) {

            if (authSession.isExpired(now)) {
                authSession.markExpired();
                authSessionEntityService.save(authSession);
                continue;
            }

            authSession.revoke(normalizedReason, now);
            authSessionEntityService.save(authSession);
            revokedCount++;
        }

        log.info("All active AuthSessions revoked for userId={}: revokedCount={}", userAccountId, revokedCount);

        return revokedCount;
    }

    private void ensureUsable(AuthSession authSession) {

        Instant now = Instant.now();

        if (authSession.getStatus() == AuthSessionStatus.REVOKED) {
            throw new IllegalStateException("AuthSession has been revoked");
        }

        if (authSession.getStatus() == AuthSessionStatus.EXPIRED) {
            throw new IllegalStateException("AuthSession has expired");
        }

        if (authSession.isExpired(now)) {
            authSessionEntityService.markExpiredInNewTransaction(authSession.getId());
            throw new IllegalStateException("AuthSession has expired");
        }

        UserAccount userAccount = authSession.getUserAccount();
        if (userAccount == null) {
            throw new IllegalStateException("AuthSession has no UserAccount");
        }
        if (!userAccount.isActive()) {
            throw new IllegalStateException("UserAccount is not active");
        }

        if (authSession.getApplicationScope() == null) {
            throw new IllegalStateException("AuthSession has no ApplicationScope");
        }
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return "revoked";
        }
        return reason.trim();
    }

    private String generateRefreshTokenValue() {

        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashRefreshToken(String refreshToken) {

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return HEX_FORMAT.formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}