package de.cocondo.app.system.security.config;

import de.cocondo.app.system.security.jwt.JwtAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({
        HttpSecurityPathsProperties.class,
        HttpSecurityCorsProperties.class
})
@Slf4j
public class HttpSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthenticationEntryPoint restAuthenticationEntryPoint,
            AccessDeniedHandler restAccessDeniedHandler,
            HttpSecurityPathsProperties paths,
            HttpSecurityCorsProperties corsProperties
    ) throws Exception {

        log.info("Initializing HttpSecurity configuration (JWT protected, all profiles)");
        log.info("CORS configuration: allowedOrigins={}, allowedMethods={}, allowedHeaders={}, exposedHeaders={}, allowCredentials={}",
                corsProperties.getAllowedOrigins(),
                corsProperties.getAllowedMethods(),
                corsProperties.getAllowedHeaders(),
                corsProperties.getExposedHeaders(),
                corsProperties.isAllowCredentials());

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {

                    // Allow CORS preflight requests globally
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                    // public POST endpoints (app-specific, e.g. login)
                    for (String p : paths.getPermitAllPostPaths()) {
                        if (p != null && !p.isBlank()) {
                            auth.requestMatchers(HttpMethod.POST, p).permitAll();
                        }
                    }

                    // public paths (generic defaults: swagger + static)
                    if (paths.getPermitAllPaths() != null && !paths.getPermitAllPaths().isEmpty()) {
                        auth.requestMatchers(paths.getPermitAllPaths().toArray(new String[0])).permitAll();
                    }

                    // protected paths (generic default: /api/**)
                    for (String p : paths.getAuthenticatedPaths()) {
                        if (p != null && !p.isBlank()) {
                            auth.requestMatchers(p).authenticated();
                        }
                    }

                    auth.anyRequest().permitAll();
                })
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(HttpSecurityCorsProperties corsProperties) {

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setExposedHeaders(corsProperties.getExposedHeaders());
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("Registered CorsConfigurationSource for '/**' with allowedOrigins={}, allowCredentials={}",
                corsProperties.getAllowedOrigins(),
                corsProperties.isAllowCredentials());

        return source;
    }
}