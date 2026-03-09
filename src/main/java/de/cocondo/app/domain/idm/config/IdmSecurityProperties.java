package de.cocondo.app.domain.idm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * IDM security configuration properties.
 *
 * Prefix:
 * idm.security
 *
 * Contains security policies used by the IDM domain.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "idm.security")
public class IdmSecurityProperties {

    private PasswordPolicy passwordPolicy = new PasswordPolicy();

    private LoginProtection loginProtection = new LoginProtection();

    @Getter
    @Setter
    public static class PasswordPolicy {

        /**
         * Minimum password length required for user passwords.
         */
        private int minimumLength = 3;
    }

    @Getter
    @Setter
    public static class LoginProtection {

        /**
         * Maximum number of failed login attempts before temporary lock.
         */
        private int maxFailedAttempts = 5;

        /**
         * Duration of temporary account lock in seconds.
         */
        private long lockDurationSeconds = 300;
    }
}