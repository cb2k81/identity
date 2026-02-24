package de.cocondo.app.domain.idm.startup;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * XML container for UserRoleAssignment bootstrap definitions.
 *
 * Entity-structure 1:1: each item maps to a UserRoleAssignment.
 *
 * Note:
 * - We only bootstrap within the Self-Scope.
 * - User identification is by username (stable & aligns with admin bootstrap).
 */
@Getter
@Setter
public class UserRoleAssignmentsXml {

    @JacksonXmlElementWrapper(useWrapping = true, localName = "items")
    @JacksonXmlProperty(localName = "assignment")
    private List<UserRoleAssignmentXmlItem> items = new ArrayList<>();

    @Getter
    @Setter
    public static class UserRoleAssignmentXmlItem {

        @JacksonXmlProperty(localName = "username")
        private String username;

        @JacksonXmlProperty(localName = "roleName")
        private String roleName;
    }
}