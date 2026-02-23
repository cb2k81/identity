package de.cocondo.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security-related bean configuration.
 *
 * Provides PasswordEncoder for hashing and verifying user passwords.
 *
 * Uses BCrypt (adaptive hashing algorithm).
 */
@Configuration
public class PasswordConfig {

    /**
     * PasswordEncoder bean using BCrypt.
     *
     * Default strength (10) is used.
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}