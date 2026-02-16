package de.cocondo.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import jakarta.annotation.PostConstruct;

/**
 * Main runner class, referenced as application entry point.
 */
@SpringBootApplication
@Import(MainApplicationConfig.class)
@Slf4j
public class MainApplicationRunner {

    @Value("${app.name:MyApp}")
    private String applicationName;

    @Value("${app.label:My App}")
    private String applicationLabel;

    @PostConstruct
    void onStartup() {
        log.info("{} ({}) initialized", applicationName, applicationLabel);
    }

    public static void main(String[] args) {

        SpringApplication application =
                new SpringApplication(MainApplicationRunner.class);

        application.setBannerMode(Banner.Mode.OFF);

        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> log.info("Application shutdown requested"))
        );

        application.run(args);
    }
}
