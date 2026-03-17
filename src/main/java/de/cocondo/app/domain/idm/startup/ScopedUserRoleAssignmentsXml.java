package de.cocondo.app.domain.idm.startup;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * XML container for foreign-scope UserRoleAssignment bootstrap definitions.
 *
 * Important:
 * - Intended for non-self scopes such as PERSONNEL/DEV.
 * - References users by username and roles by (applicationKey, stageKey, roleName).
 * - No foreign-scope permissions are bootstrapped here.
 */
@Getter
@Setter
public class ScopedUserRoleAssignmentsXml {

    @JacksonXmlElementWrapper(useWrapping = true, localName = "items")
    @JacksonXmlProperty(localName = "assignment")
    private List<ScopedUserRoleAssignmentXmlItem> items = new ArrayList<>();

    @Getter
    @Setter
    public static class ScopedUserRoleAssignmentXmlItem {

        @JacksonXmlProperty(localName = "username")
        private String username;

        @JacksonXmlProperty(localName = "applicationKey")
        private String applicationKey;

        @JacksonXmlProperty(localName = "stageKey")
        private String stageKey;

        @JacksonXmlProperty(localName = "roleName")
        private String roleName;
    }
}