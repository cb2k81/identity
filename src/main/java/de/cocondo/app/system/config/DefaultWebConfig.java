package de.cocondo.app.system.config;

import de.cocondo.app.system.core.http.DefaultFallbackInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class DefaultWebConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(DefaultWebConfig.class);

    @Value("${your.property.api-docs:/api-docs}")
    private String apiDocsPath;

    @Value("${your.property.swagger-ui:/swagger-ui}")
    private String swaggerUiPath;

    @Value("${cors.allowed-origins:null}")
    private String[] allowedOrigins;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new DefaultFallbackInterceptor())
                .addPathPatterns("/**")

                // public authentication endpoints
                .excludePathPatterns("/auth/**")

                // secured API endpoints
                .excludePathPatterns("/api/**")

                // swagger / openapi
                .excludePathPatterns("/v3/api-docs/**")
                .excludePathPatterns("/swagger-ui/**")
                .excludePathPatterns("/swagger-ui.html")

                // actuator
                .excludePathPatterns("/actuator/**")

                // static
                .excludePathPatterns("/error")
                .excludePathPatterns("/favicon.ico")
                .excludePathPatterns("/index.html")
                .excludePathPatterns("/static/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        if (allowedOrigins != null && allowedOrigins.length > 0) {
            registry.addMapping("/**")
                    .allowedOrigins(allowedOrigins)
                    .allowedMethods("*")
                    .allowedHeaders("*");
        }
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {

        configurer.defaultContentType(MediaType.APPLICATION_JSON);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        logger.debug("Configuring static resource handlers");
    }
}