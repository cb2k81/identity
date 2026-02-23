package de.cocondo.app.domain.idm.startup;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Enables binding of IDM bootstrap configuration properties.
 */
@Configuration
@EnableConfigurationProperties(IdmBootstrapProperties.class)
public class IdmBootstrapConfiguration {
}
