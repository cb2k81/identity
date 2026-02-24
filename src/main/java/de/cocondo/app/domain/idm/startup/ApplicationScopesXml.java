package de.cocondo.app.domain.idm.startup;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * XML container for application scopes.
 */
@Getter
@Setter
public class ApplicationScopesXml {

    @JacksonXmlElementWrapper(useWrapping = true, localName = "items")
    @JacksonXmlProperty(localName = "scope")
    private List<ApplicationScopeXmlItem> items = new ArrayList<>();

    @Getter
    @Setter
    public static class ApplicationScopeXmlItem {

        @JacksonXmlProperty(localName = "applicationKey")
        private String applicationKey;

        @JacksonXmlProperty(localName = "stageKey")
        private String stageKey;

        @JacksonXmlProperty(localName = "description")
        private String description;
    }
}