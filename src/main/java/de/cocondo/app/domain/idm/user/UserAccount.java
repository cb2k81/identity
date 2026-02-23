package de.cocondo.app.domain.idm.user;

import de.cocondo.app.system.entity.DomainEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Aggregate root representing a technical user account in the IDM domain.
 *
 * Minimal MVP structure for authentication.
 */
@Entity
@Table(name = "user_account")
@Getter
@Setter
@NoArgsConstructor
public class UserAccount extends DomainEntity {

    /**
     * Unique login name of the user.
     * Business identifier (not persistence ID).
     */
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    /**
     * BCrypt password hash.
     * Never store plain text passwords.
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
}