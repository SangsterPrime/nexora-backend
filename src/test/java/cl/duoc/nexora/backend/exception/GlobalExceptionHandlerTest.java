package cl.duoc.nexora.backend.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(
        value = GlobalExceptionHandlerTest.TestErrorController.class,
        excludeAutoConfiguration = {OAuth2ClientAutoConfiguration.class, OAuth2ClientWebSecurityAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestErrorController.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void methodArgumentNotValidDevuelveErroresPorCampo() throws Exception {
        mockMvc.perform(post("/test-errors/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").value("Datos invalidos"))
                .andExpect(jsonPath("$.path").value("/test-errors/validation"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errores.nombre").value("El nombre es obligatorio"));
    }

    @Test
    void constraintViolationDevuelveBadRequest() throws Exception {
        mockMvc.perform(get("/test-errors/constraint"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").value("Datos invalidos"))
                .andExpect(jsonPath("$.errores.codigo").value("Codigo invalido"));
    }

    @Test
    void entityNotFoundDevuelveNotFound() throws Exception {
        mockMvc.perform(get("/test-errors/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Entidad no encontrada"));
    }

    @Test
    void dataIntegrityDevuelveConflictSinSqlInterno() throws Exception {
        mockMvc.perform(get("/test-errors/data-integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensaje").value("No se pudo completar la operacion por una restriccion de datos"));
    }

    @Test
    void authenticationDevuelveUnauthorized() throws Exception {
        mockMvc.perform(get("/test-errors/authentication"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensaje").value("No autenticado"));
    }

    @Test
    void accessDeniedDevuelveForbidden() throws Exception {
        mockMvc.perform(get("/test-errors/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensaje").value("Acceso denegado"));
    }

    @Test
    void exceptionDevuelveErrorGenerico() throws Exception {
        mockMvc.perform(get("/test-errors/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.mensaje").value("Error interno del servidor"));
    }

    @RestController
    public static class TestErrorController {

        @PostMapping("/test-errors/validation")
        void validation(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/test-errors/constraint")
        void constraintViolation() {
            throw new ConstraintViolationException(Set.of(new TestConstraintViolation("codigo", "Codigo invalido")));
        }

        @GetMapping("/test-errors/not-found")
        void notFound() {
            throw new EntityNotFoundException("Entidad no encontrada");
        }

        @GetMapping("/test-errors/data-integrity")
        void dataIntegrity() {
            throw new DataIntegrityViolationException("duplicate key value violates unique constraint proveedores_email_key");
        }

        @GetMapping("/test-errors/authentication")
        void authentication() {
            throw new BadCredentialsException("credenciales invalidas");
        }

        @GetMapping("/test-errors/access-denied")
        void accessDenied() {
            throw new AccessDeniedException("sin permisos");
        }

        @GetMapping("/test-errors/generic")
        void generic() {
            throw new RuntimeException("detalle interno sensible");
        }
    }

    record TestRequest(@NotBlank(message = "El nombre es obligatorio") String nombre) {
    }

    record TestConstraintViolation(String propertyPath, String message) implements ConstraintViolation<Object> {

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public String getMessageTemplate() {
            return message;
        }

        @Override
        public Object getRootBean() {
            return null;
        }

        @Override
        public Class<Object> getRootBeanClass() {
            return Object.class;
        }

        @Override
        public Object getLeafBean() {
            return null;
        }

        @Override
        public Object[] getExecutableParameters() {
            return new Object[0];
        }

        @Override
        public Object getExecutableReturnValue() {
            return null;
        }

        @Override
        public jakarta.validation.Path getPropertyPath() {
            return new TestPath(propertyPath);
        }

        @Override
        public Object getInvalidValue() {
            return null;
        }

        @Override
        public jakarta.validation.metadata.ConstraintDescriptor<?> getConstraintDescriptor() {
            return null;
        }

        @Override
        public <U> U unwrap(Class<U> type) {
            throw new UnsupportedOperationException();
        }
    }

    record TestPath(String name) implements jakarta.validation.Path {

        @Override
        public Iterator<Node> iterator() {
            return List.<Node>of(new TestPathNode(name)).iterator();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    record TestPathNode(String name) implements jakarta.validation.Path.Node {

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isInIterable() {
            return false;
        }

        @Override
        public Integer getIndex() {
            return null;
        }

        @Override
        public Object getKey() {
            return null;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.PROPERTY;
        }

        @Override
        public <T extends jakarta.validation.Path.Node> T as(Class<T> nodeType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
