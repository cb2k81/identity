package de.cocondo.app.domain.idm.startup;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * XML container for foreign-scope Role bootstrap definitions.
 *
 * Important:
 * - Intended for non-self scopes such as PERSONNEL/DEV.
 * - Only roles are bootstrapped here.
 * - No permissions or permission-groups for foreign applications.
 */
@Getter
@Setter
public class ScopedRolesXml {

    @JacksonXmlElementWrapper(useWrapping = true, localName = "items")
    @JacksonXmlProperty(localName = "role")
    private List<ScopedRoleXmlItem> items = new ArrayList<>();

    @Getter
    @Setter
    public static class ScopedRoleXmlItem {

        @JacksonXmlProperty(localName = "applicationKey")
        private String applicationKey;

        @JacksonXmlProperty(localName = "stageKey")
        private String stageKey;

        @JacksonXmlProperty(localName = "name")
        private String name;

        @JacksonXmlProperty(localName = "description")
        private String description;

        @JacksonXmlProperty(localName = "systemProtected")
        private boolean systemProtected = true;
    }
}