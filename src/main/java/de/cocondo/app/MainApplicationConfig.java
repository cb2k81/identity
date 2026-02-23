package de.cocondo.app;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Central application configuration.
 */
@Configuration
@EntityScan(basePackages = "de.cocondo.app")
@EnableJpaRepositories(basePackages = "de.cocondo.app")
@ComponentScan(basePackages = "de.cocondo.app")
public class MainApplicationConfig {
}
