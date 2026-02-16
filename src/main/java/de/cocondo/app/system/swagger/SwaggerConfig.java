package de.cocondo.app.system.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class SwaggerConfig {

    @Value("${api.security.token.header-name:X-API-Token}")
    private String apiTokenHeaderName;

    @Value("${app.label:Application}")
    private String appLabel;

    @Bean
    public OpenAPI customOpenAPI() {
        Components components = createComponents();
        sortAndAddHeaderSchemas(components);
        sortAndAddSecuritySchemas(components);

        OpenAPI openAPI = new OpenAPI()
                .components(components)
                .info(new Info().title(appLabel + " - API Documentation").version("1.0.0"));

        openAPI.addSecurityItem(new SecurityRequirement().addList("API-Token"));

        return openAPI;
    }

    private Components createComponents() {
        Header header = new Header()
                .description("API-Token")
                .required(true);

        SecurityScheme securityScheme = new SecurityScheme()
                .name(apiTokenHeaderName)
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER);

        Components components = new Components();
        components.addHeaders("API-Token", header);
        components.addSecuritySchemes("API-Token", securityScheme);

        return components;
    }

    private void sortAndAddHeaderSchemas(Components components) {
        List<Map.Entry<String, Header>> headerSchemas = new ArrayList<>(components.getHeaders().entrySet());
        headerSchemas.sort(Map.Entry.comparingByKey());
        headerSchemas.forEach(entry -> components.addHeaders(entry.getKey(), entry.getValue()));
    }

    private void sortAndAddSecuritySchemas(Components components) {
        List<Map.Entry<String, SecurityScheme>> securitySchemas = new ArrayList<>(components.getSecuritySchemes().entrySet());
        securitySchemas.sort(Map.Entry.comparingByKey());
        securitySchemas.forEach(entry -> components.addSecuritySchemes(entry.getKey(), entry.getValue()));
    }
}
