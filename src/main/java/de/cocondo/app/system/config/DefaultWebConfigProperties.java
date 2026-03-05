package de.cocondo.app.system.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Additional configuration for DefaultWebConfig.
 *
 * system.web.fallback-interceptor.*
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "system.web.fallback-interceptor")
public class DefaultWebConfigProperties {

    /**
     * Additional exclude path patterns for DefaultFallbackInterceptor.
     * Example (IDM): "/auth/**"
     */
    private List<String> additionalExcludePathPatterns = new ArrayList<>();
}