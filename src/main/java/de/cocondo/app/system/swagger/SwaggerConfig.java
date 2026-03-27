package de.cocondo.app.system.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Generic OpenAPI configuration for the shared system kernel.
 *
 * Documents JWT Bearer authentication.
 *
 * NOTE:
 * - This configuration only documents security.
 * - Actual enforcement is done by Spring Security.
 * - Application-specific metadata is derived from configuration.
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Value("${spring.application.name:application}")
    private String applicationName;

    @Bean
    public OpenAPI openAPI() {

        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT Bearer authentication");

        return new OpenAPI()
                .info(new Info()
                        .title(applicationName + " API")
                        .version("0.1.0")
                        .description("Application API documentation with JWT authentication"))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, bearerScheme))
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME));
    }
}