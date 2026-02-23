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
    private String[] corsAllowedOrigins;

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON);
        logger.info("Configuring default content negotiation to MediaType.APPLICATION_JSON");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new DefaultFallbackInterceptor())
                .excludePathPatterns( "/")
                .excludePathPatterns( "/favicon.ico")
                .excludePathPatterns("/index.html")
                .excludePathPatterns(apiDocsPath + "/**")
                .excludePathPatterns(swaggerUiPath + "/**")
                .excludePathPatterns("/auth/**")
                .excludePathPatterns("/api/**")
                .excludePathPatterns("/static/**");

        logger.info("Registering DefaultFallbackInterceptor bean");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(corsAllowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);

        logger.info("Allowed cors origins: ");
        for (String origin : corsAllowedOrigins) {
            logger.info("- " + origin);
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);

        logger.info("Static resources mapped to classpath:/static/");
    }

}
