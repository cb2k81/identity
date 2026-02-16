package de.cocondo.app.domain.idm.person.gender;

import jakarta.persistence.Id;

public class Gender {

    /**
     * Short type like "m" or "f", maybe extended if "x" is also needed
     */
    @Id
    private String genderValue;

}
