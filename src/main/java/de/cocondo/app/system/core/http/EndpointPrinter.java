package de.cocondo.app.system.core.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;

@Component
@ConditionalOnProperty(
        prefix = "system.endpoint-printer",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class EndpointPrinter implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(EndpointPrinter.class);

    private final RequestMappingHandlerMapping handlerMapping;

    @Value("${springdoc.api-docs.path:/v3/api-docs}")
    private String apiDocsPath;

    @Value("${springdoc.swagger-ui.path:/swagger-ui}")
    private String swaggerUiPath;

    /**
     * Optional list of base package prefixes used only for grouping output.
     * Completely generic – no hardcoded domain knowledge.
     */
    @Value("#{'${system.endpoint-printer.group-prefixes:}'.empty ? null : '${system.endpoint-printer.group-prefixes}'.split(',')}")
    private List<String> groupPrefixes;

    public EndpointPrinter(
            @Qualifier("requestMappingHandlerMapping")
            RequestMappingHandlerMapping handlerMapping
    ) {
        this.handlerMapping = handlerMapping;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        Map<String, List<String>> groupedEndpoints = new LinkedHashMap<>();

        groupedEndpoints.put("infrastructure", new ArrayList<>());
        groupedEndpoints.get("infrastructure").add("API Docs: " + apiDocsPath);
        groupedEndpoints.get("infrastructure").add("Swagger UI: " + swaggerUiPath + "/index.html");

        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

        for (HandlerMethod handlerMethod : handlerMethods.values()) {
            Class<?> beanType = handlerMethod.getBeanType();
            String packageName = beanType.getPackageName();
            String simpleName = beanType.getSimpleName();

            handlerMethod.getMethod().getDeclaredAnnotations();

            handlerMapping.getHandlerMethods().forEach((info, method) -> {
                if (method.equals(handlerMethod)) {
                    Set<String> patterns = info.getPatternValues();
                    for (String pattern : patterns) {
                        String endpoint = simpleName + ": " + pattern;

                        String group = resolveGroup(packageName);
                        groupedEndpoints
                                .computeIfAbsent(group, k -> new ArrayList<>())
                                .add(endpoint);
                    }
                }
            });
        }

        groupedEndpoints.forEach((group, endpoints) -> {
            logger.info("[{}] {} endpoints", endpoints.size(), group);
            endpoints.stream()
                    .sorted()
                    .forEach(logger::debug);
        });
    }

    private String resolveGroup(String packageName) {

        if (groupPrefixes == null || groupPrefixes.isEmpty()) {
            return "application";
        }

        for (String prefix : groupPrefixes) {
            if (packageName.startsWith(prefix.trim())) {
                return prefix.trim();
            }
        }

        return "application";
    }
}
