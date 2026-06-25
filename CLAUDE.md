# CLAUDE.md

Guía para trabajar con código en este repositorio (`nexora-backend`).

## Qué es

Backend MVP de **NEXORA**, plataforma de gestión de compras (proveedores, solicitudes, cotizaciones, negociaciones, órdenes de compra) con pipelines de procesamiento, KPIs e integraciones externas (n8n y un servicio Python de IA, `EntrenamientoAI`).

Arquitectura de despliegue: **Frontend (React/Vite en Vercel) → Backend (Spring Boot en Render) → PostgreSQL (Neon)**. El frontend nunca llama a servicios externos directamente; todo pasa por el backend.

## Stack

- Java 21, Spring Boot 4.0.6 (Web MVC, Data JPA, Security, OAuth2 client, Validation, Actuator)
- PostgreSQL en runtime, H2 en tests
- Lombok, springdoc-openapi (Swagger), Maven (wrapper `mvnw`)

## Comandos

> En Windows `JAVA_HOME` puede no estar configurado. Antes de usar Maven:
> `$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.11"` (PowerShell).

```powershell
.\mvnw.cmd compile            # compilar
.\mvnw.cmd test               # ejecutar toda la suite (usa H2, no requiere DB)
.\mvnw.cmd spring-boot:run    # levantar la API (requiere DB_URL/DB_USER/DB_PASS, normalmente vía .env)
.\mvnw.cmd -o test            # offline si ya hay dependencias en el repo local
```

Ejecutar un solo test: `.\mvnw.cmd test -Dtest=MlServiceTest`

Config local: crea `.env` en la raíz basado en `.env.example` (lo lee `spring.config.import=optional:file:.env[.properties]`). `.env` está en `.gitignore`; **nunca** se commitean credenciales ni API keys.

## Estructura y convenciones

Paquete base: `cl.duoc.nexora.backend`. Capas:

- `model` — entidades JPA. Lombok (`@Getter/@Setter/@NoArgsConstructor/@AllArgsConstructor/@Builder`), `@PrePersist` para defaults y timestamps, enums persistidos con `@Enumerated(EnumType.STRING)`.
- `repository` — interfaces `JpaRepository`. Métodos derivados simples (`findFirstByNombre`, etc.).
- `service` — lógica de aplicación; `@Service` + `@RequiredArgsConstructor` (inyección por constructor); `@Transactional` en operaciones CRUD.
- `controller` — `@RestController` bajo `/api/...`, `@RequiredArgsConstructor`, devuelve DTOs (nunca entidades).
- `dto/request` y `dto/response` — **records**. Requests con anotaciones de Jakarta Validation.
- `mapper` — clases `final` con constructor privado y métodos `static` (`toEntity`, `toResponse`, `updateEntity`). No se usa MapStruct.
- `enums` — estados, tipos y roles.
- `exception` — `ResourceNotFoundException` y `GlobalExceptionHandler` (`@RestControllerAdvice`) que centraliza errores y responde con el record `ApiErrorResponse` (`status`, `mensaje`, `path`, `timestamp`, `errores`).
- `config` — seguridad, CORS, propiedades de integraciones (`@ConfigurationProperties`, registradas en `NexoraBackendApplication` con `@EnableConfigurationProperties`).

Idioma: el dominio, los identificadores y los mensajes están en **español**. Mantener ese estilo.

## Seguridad

`SecurityConfig` define el `SecurityFilterChain`. Endpoints públicos: `/api/health`, `/db-test`, Swagger, OAuth2. El resto requiere autenticación (sesión Google OAuth2). Al agregar un controlador nuevo bajo `/api`, añade su ruta a la lista de `authenticated()` si corresponde. Cookies de sesión `SameSite=None; Secure` para el flujo cross-site Vercel↔Render.

## Integraciones externas

Ambas se configuran por variables de entorno y tienen su `@ConfigurationProperties` en `config`:

- **n8n** (`N8N_ENABLED`, `N8N_WEBHOOK_URL`, `N8N_WEBHOOK_SECRET`): envío de eventos por webhook, opcional y **no bloqueante** (`N8nIntegrationService`). Si falla, el flujo principal continúa.
- **Servicio ML/IA — `EntrenamientoAI`** (`NEXORA_ML_ENABLED`, `NEXORA_ML_URL`, `NEXORA_ML_API_KEY`): fachada `/api/ml/**` hacia el servicio Python.
  - `MlServiceClient` — cliente HTTP (`RestClient`), añade header `X-API-Key`, traduce fallos a `MlServiceException` (502 servicio caído/erróneo, 503 desactivado o sin URL). La API key nunca se loguea ni se expone al frontend.
  - `MlService` — orquesta y deja traza: `train`/`score` se registran como `PipelineEjecucion`; un fallo crea `PipelineError` y marca `FALLIDA`; las métricas se guardan como `KpiResultado` (`ML_*`). Las escrituras **no** son `@Transactional` a propósito, para que el registro de error sobreviva al re-lanzamiento de la excepción.
  - DTOs en `dto/ml` (records con `@JsonIgnoreProperties(ignoreUnknown = true)` y `@JsonProperty`/`@JsonAlias` para mapear `snake_case` del servicio Python, p. ej. `roc_auc`, `matriz_confusion`).

## Testing

- Services: JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`).
- Controllers: `@WebMvcTest` + `MockMvc` + `@MockitoBean` (Spring Boot 4). Se excluye `SecurityConfig` y autoconfig OAuth2 vía `excludeFilters`/`excludeAutoConfiguration` y `@AutoConfigureMockMvc(addFilters = false)` (ver `ProveedorControllerTest`/`MlControllerTest` como plantilla).
- Repositories: `@DataJpaTest` con H2.
- Config de tests en `src/test/resources/application.properties` (H2 en memoria, integraciones desactivadas).

## Notas

- `spring.jpa.hibernate.ddl-auto=update` en local: Hibernate ajusta tablas durante el MVP. Agregar valores a enums o columnas es aditivo; evita cambios destructivos al esquema existente.
- `KpiResultado.valor` es `NUMERIC(15,2)`: las métricas ML se redondean a 2 decimales al persistirse; el valor completo se obtiene vía `GET /api/ml/metrics`.
