package cl.duoc.nexora.backend;

import cl.duoc.nexora.backend.config.N8nProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(N8nProperties.class)
public class NexoraBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(NexoraBackendApplication.class, args);
	}

}
