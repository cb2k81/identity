package de.cocondo.app.domain.idm.user;

import de.cocondo.app.domain.idm.config.IdmSecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validates raw passwords against the configured IDM password policy.
 *
 * Important:
 * - Applies only to domain use-cases that explicitly invoke this validator.
 * - Must not be applied implicitly during bootstrap initialization.
 */
@Component
@RequiredArgsConstructor
public class PasswordPolicyValidator {

    private final IdmSecurityProperties idmSecurityProperties;

    public void validate(String rawPassword) {

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("password must not be blank");
        }

        int minimumLength = idmSecurityProperties.getPasswordPolicy().getMinimumLength();

        if (rawPassword.length() < minimumLength) {
            throw new IllegalArgumentException("password must have at least " + minimumLength + " characters");
        }
    }
}