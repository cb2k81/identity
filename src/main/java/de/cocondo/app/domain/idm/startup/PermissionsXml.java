package de.cocondo.app.domain.idm.startup;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * XML container for Permission bootstrap definitions.
 */
@Getter
@Setter
public class PermissionsXml {

    @JacksonXmlElementWrapper(useWrapping = true, localName = "items")
    @JacksonXmlProperty(localName = "permission")
    private List<PermissionXmlItem> items = new ArrayList<>();

    @Getter
    @Setter
    public static class PermissionXmlItem {

        @JacksonXmlProperty(localName = "permissionGroupName")
        private String permissionGroupName;

        @JacksonXmlProperty(localName = "name")
        private String name;

        @JacksonXmlProperty(localName = "description")
        private String description;

        @JacksonXmlProperty(localName = "systemProtected")
        private boolean systemProtected = true;
    }
}