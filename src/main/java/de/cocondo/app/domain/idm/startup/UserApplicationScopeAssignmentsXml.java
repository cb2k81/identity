package de.cocondo.app.domain.idm.startup;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * XML container for UserApplicationScopeAssignment bootstrap definitions.
 *
 * Entity-structure 1:1: each item maps to a UserApplicationScopeAssignment.
 */
@Getter
@Setter
public class UserApplicationScopeAssignmentsXml {

    @JacksonXmlElementWrapper(useWrapping = true, localName = "items")
    @JacksonXmlProperty(localName = "assignment")
    private List<UserApplicationScopeAssignmentXmlItem> items = new ArrayList<>();

    @Getter
    @Setter
    public static class UserApplicationScopeAssignmentXmlItem {

        @JacksonXmlProperty(localName = "username")
        private String username;

        @JacksonXmlProperty(localName = "applicationKey")
        private String applicationKey;

        @JacksonXmlProperty(localName = "stageKey")
        private String stageKey;
    }
}