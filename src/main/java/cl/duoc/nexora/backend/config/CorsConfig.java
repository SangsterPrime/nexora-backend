package cl.duoc.nexora.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final String[] LOCAL_DEV_ORIGINS = {
            "http://localhost:5173",
            "http://127.0.0.1:5173"
    };

    private static final String[] ALLOWED_METHODS = {
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "OPTIONS"
    };

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registerLocalDevCors(registry, "/api/**");
        registerLocalDevCors(registry, "/actuator/**");
    }

    private void registerLocalDevCors(CorsRegistry registry, String pathPattern) {
        registry.addMapping(pathPattern)
                .allowedOrigins(LOCAL_DEV_ORIGINS)
                .allowedMethods(ALLOWED_METHODS)
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}
