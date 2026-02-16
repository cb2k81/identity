package de.cocondo.app.domain.idm.person.dto;

import de.cocondo.app.system.dto.DataTransferObject;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Payload DTO carrying person data without use-case semantics.
 *
 * This DTO represents the transferable data of the Person aggregate
 * and contains no technical or persistence-related information.
 */
@Data
public class PersonPayloadDTO implements DataTransferObject, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String firstName;
    private String middleName;
    private String lastName;

    private String gender;
    private String salutation;
    private String academicTitle;

    private LocalDate birthday;
}
