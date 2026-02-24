package de.cocondo.app.domain.idm.startup;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * XML container for Role bootstrap definitions.
 */
@Getter
@Setter
public class RolesXml {

    @JacksonXmlElementWrapper(useWrapping = true, localName = "items")
    @JacksonXmlProperty(localName = "role")
    private List<RoleXmlItem> items = new ArrayList<>();

    @Getter
    @Setter
    public static class RoleXmlItem {

        @JacksonXmlProperty(localName = "name")
        private String name;

        @JacksonXmlProperty(localName = "description")
        private String description;

        @JacksonXmlProperty(localName = "systemProtected")
        private boolean systemProtected = true;
    }
}