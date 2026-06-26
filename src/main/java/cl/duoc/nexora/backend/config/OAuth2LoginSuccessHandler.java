package cl.duoc.nexora.backend.config;

import cl.duoc.nexora.backend.service.OAuth2UsuarioService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2UsuarioService oAuth2UsuarioService;

    @Value("${FRONTEND_URL:${frontend.url:http://localhost:5173}}")
    private String frontendUrl;

    @Value("${OAUTH2_SUCCESS_REDIRECT_URL:}")
    private String successRedirectUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        try {
            if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
                oAuth2UsuarioService.registrarOActualizarDesdeGoogle(oAuth2User);
            }
        } catch (Exception e) {
            String baseUrl = frontendUrl.endsWith("/")
                    ? frontendUrl.substring(0, frontendUrl.length() - 1)
                    : frontendUrl;
            response.sendRedirect(baseUrl + "/login?error=db");
            return;
        }
        response.sendRedirect(appRedirectUrl());
    }

    private String appRedirectUrl() {
        if (successRedirectUrl != null && !successRedirectUrl.isBlank()) {
            return successRedirectUrl;
        }

        String baseUrl = frontendUrl.endsWith("/")
                ? frontendUrl.substring(0, frontendUrl.length() - 1)
                : frontendUrl;
        return baseUrl + "/app";
    }
}
