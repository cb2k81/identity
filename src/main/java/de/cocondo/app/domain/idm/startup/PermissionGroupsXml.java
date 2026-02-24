package de.cocondo.app.domain.idm.startup;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * XML container for PermissionGroup bootstrap definitions.
 */
@Getter
@Setter
public class PermissionGroupsXml {

    @JacksonXmlElementWrapper(useWrapping = true, localName = "items")
    @JacksonXmlProperty(localName = "permissionGroup")
    private List<PermissionGroupXmlItem> items = new ArrayList<>();

    @Getter
    @Setter
    public static class PermissionGroupXmlItem {

        @JacksonXmlProperty(localName = "name")
        private String name;

        @JacksonXmlProperty(localName = "description")
        private String description;
    }
}