package de.cocondo.app.domain.idm.person;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;

/**
 * Aggregate root representing a person within the IDM domain.
 *
 * This entity is a pure domain object:
 * - no DTO dependencies
 * - no security concerns
 * - no application or persistence logic
 */
@Entity
@Table(name = "person")
@Data
public class Person {

    /**
     * Technical identifier (UUID stored as String).
     * Generation is handled outside of the entity.
     */
    @Id
    private String id;

    private String firstName;
    private String middleName;
    private String lastName;

    private String gender;
    private String salutation;
    private String academicTitle;

    private LocalDate birthday;
}
