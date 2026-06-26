package cl.duoc.nexora.backend.config;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    private static final String[] LOCAL_DEV_ORIGINS = {
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://localhost:3000",
            "http://127.0.0.1:3000"
    };

    private static final String[] ALLOWED_METHODS = {
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "OPTIONS"
    };

    private static final String PROD_FRONTEND_ORIGIN = "https://nexora-fronted.vercel.app";

    @Value("${FRONTEND_URL:${frontend.url:http://localhost:5173}}")
    private String frontendUrl;

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins());
        configuration.setAllowedMethods(List.of(ALLOWED_METHODS));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Location"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private List<String> allowedOrigins() {
        Set<String> origins = new LinkedHashSet<>();
        origins.addAll(List.of(LOCAL_DEV_ORIGINS));
        origins.add(PROD_FRONTEND_ORIGIN);
        if (frontendUrl != null && !frontendUrl.isBlank()) {
            origins.add(stripTrailingSlash(frontendUrl));
        }
        return new ArrayList<>(origins);
    }

    private String stripTrailingSlash(String origin) {
        if (origin.endsWith("/")) {
            return origin.substring(0, origin.length() - 1);
        }
        return origin;
    }
}
