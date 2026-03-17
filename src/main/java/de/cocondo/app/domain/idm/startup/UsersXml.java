package de.cocondo.app.domain.idm.startup;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * XML container for additional UserAccount bootstrap definitions.
 *
 * Entity-structure 1:1: each item maps to a UserAccount.
 */
@Getter
@Setter
public class UsersXml {

    @JacksonXmlElementWrapper(useWrapping = true, localName = "items")
    @JacksonXmlProperty(localName = "user")
    private List<UserXmlItem> items = new ArrayList<>();

    @Getter
    @Setter
    public static class UserXmlItem {

        @JacksonXmlProperty(localName = "username")
        private String username;

        @JacksonXmlProperty(localName = "displayName")
        private String displayName;

        @JacksonXmlProperty(localName = "email")
        private String email;

        @JacksonXmlProperty(localName = "password")
        private String password;
    }
}