package de.cocondo.app.domain.idm.startup;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * XML container for RolePermissionAssignment bootstrap definitions.
 *
 * Entity-structure 1:1: each item maps to a RolePermissionAssignment.
 */
@Getter
@Setter
public class RolePermissionAssignmentsXml {

    @JacksonXmlElementWrapper(useWrapping = true, localName = "items")
    @JacksonXmlProperty(localName = "assignment")
    private List<RolePermissionAssignmentXmlItem> items = new ArrayList<>();

    @Getter
    @Setter
    public static class RolePermissionAssignmentXmlItem {

        @JacksonXmlProperty(localName = "roleName")
        private String roleName;

        @JacksonXmlProperty(localName = "permissionName")
        private String permissionName;
    }
}