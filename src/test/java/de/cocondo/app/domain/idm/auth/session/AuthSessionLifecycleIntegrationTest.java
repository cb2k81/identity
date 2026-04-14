package de.cocondo.app.domain.idm.auth.session;

import de.cocondo.app.MainApplicationRunner;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import de.cocondo.app.domain.idm.user.UserAccount;
import de.cocondo.app.domain.idm.user.UserAccountEntityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = MainApplicationRunner.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "idm.bootstrap.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "idm.security.refresh.ttl-ms=600000"
})
class AuthSessionLifecycleIntegrationTest {

    @Autowired
    private AuthSessionLifecycleService authSessionLifecycleService;

    @Autowired
    private AuthSessionEntityService authSessionEntityService;

    @Autowired
    private UserAccountEntityService userAccountEntityService;

    @Autowired
    private ApplicationScopeEntityService applicationScopeEntityService;

    private UserAccount user;
    private ApplicationScope scope;

    @BeforeEach
    void setup() {
        scope = getOrCreateScope("IDM", "TEST");
        user = createActiveUser("auth_session_test_user_" + UUID.randomUUID());
    }

    @Test
    void createSession_persistsActiveContext_withoutStoringPlainRefreshToken() {

        IssuedAuthSession issued = authSessionLifecycleService.createSession(user, scope);

        assertNotNull(issued);
        assertNotNull(issued.sessionId());
        assertNotNull(issued.refreshToken());
        assertTrue(issued.refreshExpiresAt() > System.currentTimeMillis());

        AuthSession persisted = authSessionEntityService.loadById(issued.sessionId())
                .orElseThrow(() -> new IllegalStateException("AuthSession not found: " + issued.sessionId()));

        assertEquals(user.getId(), persisted.getUserAccount().getId());
        assertEquals(scope.getId(), persisted.getApplicationScope().getId());
        assertEquals(AuthSessionStatus.ACTIVE, persisted.getStatus());
        assertNotNull(persisted.getRefreshTokenHash());
        assertNotEquals(issued.refreshToken(), persisted.getRefreshTokenHash());
    }

    @Test
    void validateActiveSession_returnsPersistedContext() {

        IssuedAuthSession issued = authSessionLifecycleService.createSession(user, scope);

        AuthSession validated = authSessionLifecycleService.validateActiveSession(issued.refreshToken());

        assertNotNull(validated);
        assertEquals(issued.sessionId(), validated.getId());
        assertEquals(AuthSessionStatus.ACTIVE, validated.getStatus());
    }

    @Test
    void revokeSession_marksContextAsRevoked() {

        IssuedAuthSession issued = authSessionLifecycleService.createSession(user, scope);

        AuthSession revoked = authSessionLifecycleService.revokeSession(issued.refreshToken(), "test revoke");

        assertEquals(AuthSessionStatus.REVOKED, revoked.getStatus());
        assertNotNull(revoked.getRevokedAt());
        assertEquals("test revoke", revoked.getRevokedReason());
    }

    @Test
    void revokeAllSessionsForUser_revokesMultipleActiveSessions() {

        IssuedAuthSession first = authSessionLifecycleService.createSession(user, scope);
        IssuedAuthSession second = authSessionLifecycleService.createSession(user, scope);

        int revokedCount = authSessionLifecycleService.revokeAllSessionsForUser(user.getId(), "logout-all");

        assertEquals(2, revokedCount);

        AuthSession firstPersisted = authSessionEntityService.loadById(first.sessionId())
                .orElseThrow(() -> new IllegalStateException("AuthSession not found: " + first.sessionId()));
        AuthSession secondPersisted = authSessionEntityService.loadById(second.sessionId())
                .orElseThrow(() -> new IllegalStateException("AuthSession not found: " + second.sessionId()));

        assertEquals(AuthSessionStatus.REVOKED, firstPersisted.getStatus());
        assertEquals(AuthSessionStatus.REVOKED, secondPersisted.getStatus());
    }

    @Test
    void validateActiveSession_rejectsExpiredContext_and_marksItExpired() {

        IssuedAuthSession issued = authSessionLifecycleService.createSession(user, scope);

        AuthSession persisted = authSessionEntityService.loadById(issued.sessionId())
                .orElseThrow(() -> new IllegalStateException("AuthSession not found: " + issued.sessionId()));

        persisted.setExpiresAt(Instant.now().minusSeconds(60));
        authSessionEntityService.save(persisted);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> authSessionLifecycleService.validateActiveSession(issued.refreshToken())
        );

        assertEquals("AuthSession has expired", ex.getMessage());

        AuthSession expired = authSessionEntityService.loadById(issued.sessionId())
                .orElseThrow(() -> new IllegalStateException("AuthSession not found: " + issued.sessionId()));

        assertEquals(AuthSessionStatus.EXPIRED, expired.getStatus());
    }

    private ApplicationScope getOrCreateScope(String applicationKey, String stageKey) {
        return applicationScopeEntityService
                .loadByApplicationKeyAndStageKey(applicationKey, stageKey)
                .orElseGet(() -> {
                    ApplicationScope newScope = new ApplicationScope();
                    newScope.setApplicationKey(applicationKey);
                    newScope.setStageKey(stageKey);
                    newScope.setDescription("AuthSession test scope");
                    return applicationScopeEntityService.save(newScope);
                });
    }

    private UserAccount createActiveUser(String username) {
        UserAccount newUser = new UserAccount();
        newUser.setUsername(username);
        newUser.setPasswordHash("$2a$10$dummyHashValueForSessionOnly");
        newUser.activate();
        return userAccountEntityService.save(newUser);
    }
}