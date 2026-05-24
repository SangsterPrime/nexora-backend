package cl.duoc.nexora.backend.controller;

import java.sql.Connection;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DatabaseTestController {

    private final DataSource dataSource;

    @GetMapping("/db-test")
    public String probarConexion() {
        try (Connection connection = dataSource.getConnection()) {
            return "Conectado a Neon PostgreSQL: " + connection.getCatalog();
        } catch (Exception exception) {
            return "Error conectando a la base: " + exception.getMessage();
        }
    }
}
