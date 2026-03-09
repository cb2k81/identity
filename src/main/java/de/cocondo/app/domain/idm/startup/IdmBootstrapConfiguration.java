package de.cocondo.app.domain.idm.startup;

import de.cocondo.app.domain.idm.config.IdmSecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Enables binding of IDM bootstrap configuration properties.
 */
@Configuration
@EnableConfigurationProperties({
        IdmBootstrapProperties.class,
        IdmSelfProperties.class,
        IdmSecurityProperties.class
})
public class IdmBootstrapConfiguration {
}