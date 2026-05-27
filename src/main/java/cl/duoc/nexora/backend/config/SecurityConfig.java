package cl.duoc.nexora.backend.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository;

    @Value("${FRONTEND_URL:${frontend.url:http://localhost:5173}}")
    private String frontendUrl;

    @Value("${OAUTH2_FAILURE_REDIRECT_URL:}")
    private String failureRedirectUrl;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/",
                                "/api/health",
                                "/db-test",
                                "/actuator/health",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers(
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/api/auth/me",
                                "/api/auth/logout"
                        ).permitAll()
                        .requestMatchers(
                                "/api/proveedores/**",
                                "/api/solicitudes-compra/**",
                                "/api/cotizaciones/**",
                                "/api/negociaciones/**",
                                "/api/ordenes-compra/**",
                                "/api/pipelines/**",
                                "/api/pipeline-ejecuciones/**",
                                "/api/integrations/**"
                        ).authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception.authenticationEntryPoint((request, response, authException) ->
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED)));

        if (clientRegistrationRepository.getIfAvailable() != null) {
            http.oauth2Login(oauth2 -> oauth2
                    .successHandler(oAuth2LoginSuccessHandler)
                    .failureHandler((request, response, exception) -> response.sendRedirect(oauth2FailureRedirectUrl()))
            );
        }

        return http.build();
    }

    private String oauth2FailureRedirectUrl() {
        if (failureRedirectUrl != null && !failureRedirectUrl.isBlank()) {
            return failureRedirectUrl;
        }

        String baseUrl = frontendUrl.endsWith("/")
                ? frontendUrl.substring(0, frontendUrl.length() - 1)
                : frontendUrl;
        return baseUrl + "/login?error=oauth";
    }
}
