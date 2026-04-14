package de.cocondo.app.domain.idm.auth.session;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Entity service for AuthSession.
 *
 * Encapsulates persistence access and operates exclusively on domain entities.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthSessionEntityService {

    private final AuthSessionRepository repository;

    public Optional<AuthSession> loadById(String id) {
        return repository.findById(id);
    }

    public Optional<AuthSession> loadByRefreshTokenHash(String refreshTokenHash) {
        return repository.findByRefreshTokenHash(refreshTokenHash);
    }

    public List<AuthSession> loadAllByUserAccountId(String userAccountId) {
        return repository.findAllByUserAccount_Id(userAccountId);
    }

    public List<AuthSession> loadAllActiveByUserAccountId(String userAccountId) {
        return repository.findAllByUserAccount_IdAndStatus(userAccountId, AuthSessionStatus.ACTIVE);
    }

    @Transactional
    public AuthSession save(AuthSession authSession) {
        return repository.save(authSession);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuthSession markExpiredInNewTransaction(String authSessionId) {

        AuthSession authSession = repository.findById(authSessionId)
                .orElseThrow(() -> new IllegalArgumentException("AuthSession not found: " + authSessionId));

        authSession.markExpired();

        return repository.save(authSession);
    }
}