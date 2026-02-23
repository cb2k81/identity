package de.cocondo.app.domain.idm.startup;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for IDM bootstrap initialization.
 *
 * idm.bootstrap.*
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "idm.bootstrap")
public class IdmBootstrapProperties {

    /**
     * Enables or disables bootstrap execution.
     */
    private boolean enabled = false;

    /**
     * Bootstrap execution mode.
     * Default: safe
     */
    private String mode = "safe";

    /**
     * Default admin configuration.
     */
    private Admin admin = new Admin();

    @Getter
    @Setter
    public static class Admin {

        private String username = "admin";

        private String password = "admin";
    }
}