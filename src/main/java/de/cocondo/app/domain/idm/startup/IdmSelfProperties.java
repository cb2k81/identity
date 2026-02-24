package de.cocondo.app.domain.idm.startup;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties describing the "self identity" of this running IDM instance.
 *
 * idm.self.*
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "idm.self")
public class IdmSelfProperties {

    /**
     * The application scope this instance represents.
     */
    private Scope scope = new Scope();

    @Getter
    @Setter
    public static class Scope {

        /**
         * Example: "IDM"
         */
        private String applicationKey = "IDM";

        /**
         * Example: "DEV", "TEST", "PROD"
         */
        private String stageKey = "DEV";
    }
}