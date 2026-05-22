# Nexora Backend

Backend MVP para Nexora, una plataforma orientada a gestionar procesos de compra, proveedores, cotizaciones, negociaciones, ordenes de compra, ejecuciones de pipelines y resultados KPI.

El proyecto esta alineado con el mapa entidad relacion del producto y expone una API REST para operar el flujo base de compras.

## Que es Nexora

Nexora centraliza el ciclo de abastecimiento desde una solicitud de compra hasta la seleccion de una cotizacion ganadora y la emision de una orden de compra.

El modelo tambien contempla pipelines de procesamiento para automatizar o auditar etapas del flujo, registrar errores y calcular KPI de negocio.

Entidades principales:

- `Usuario`
- `Proveedor`
- `SolicitudCompra`
- `Cotizacion`
- `Negociacion`
- `OrdenCompra`
- `Pipeline`
- `PipelineEjecucion`
- `PipelineError`
- `KpiResultado`

## Stack

- Java 21
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Data JPA
- Hibernate
- Jakarta Persistence
- Jakarta Validation
- PostgreSQL 17.6
- H2 para tests
- JUnit 5
- Mockito
- MockMvc
- Lombok
- Maven
- Docker Compose opcional
- Spring Boot Actuator
- Springdoc OpenAPI / Swagger UI

## Como correrlo

### Requisitos

- Java 21
- Maven 3.9+
- PostgreSQL 17.6 instalado localmente en Windows
- Docker y Docker Compose solo si quieres usar el entorno alternativo opcional

### Variables de entorno

El proyecto incluye `.env.example` con valores locales sugeridos para PostgreSQL 17.6:

```env
PORT=8080

SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/nexora_db
SPRING_DATASOURCE_USERNAME=nexora_user
SPRING_DATASOURCE_PASSWORD=nexora_password

SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=true
```

`application.properties` lee estas variables de entorno y trae defaults seguros para desarrollo. El password por defecto es vacio; si tu PostgreSQL local usa password, define `SPRING_DATASOURCE_PASSWORD` en tu terminal, IDE o entorno de ejecucion.

### PostgreSQL Local En Windows

El entorno local principal usa PostgreSQL 17.6 instalado en Windows, no Docker.

Configuracion esperada:

- Host: `localhost`
- Puerto: `5432`
- Base de datos: `nexora_db`
- Usuario: `nexora_user`
- Password: `nexora_password`

En desarrollo local se usa `SPRING_JPA_HIBERNATE_DDL_AUTO=update` para que Hibernate pueda crear o ajustar tablas durante el MVP.

Ejemplo en PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/nexora_db"
$env:SPRING_DATASOURCE_USERNAME="nexora_user"
$env:SPRING_DATASOURCE_PASSWORD="nexora_password"
$env:SPRING_JPA_HIBERNATE_DDL_AUTO="update"
$env:SPRING_JPA_SHOW_SQL="true"
.\mvnw.cmd spring-boot:run
```

### Docker Compose Opcional

`compose.yaml` queda disponible solo como alternativa de desarrollo local. No es requisito para correr el proyecto si ya tienes PostgreSQL 17.6 instalado en Windows.

```bash
docker compose up -d
```

Esto levanta PostgreSQL 17.6 con:

- Base de datos: `nexora_db`
- Usuario: `nexora_user`
- Password: `nexora_password`
- Puerto: `5432:5432`

## Despliegue En Render Con Neon

Render debe ejecutar el backend como Web Service con Java 21. La aplicacion escucha en `0.0.0.0` y toma el puerto desde `PORT`:

```properties
server.port=${PORT:8080}
server.address=0.0.0.0
```

Variables recomendadas en Render:

```env
PORT=10000
SPRING_DATASOURCE_URL=jdbc:postgresql://TU_HOST_NEON/TU_DATABASE?sslmode=require
SPRING_DATASOURCE_USERNAME=TU_USER_NEON
SPRING_DATASOURCE_PASSWORD=TU_PASSWORD_NEON
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_JPA_SHOW_SQL=false
```

Neon entrega una connection string PostgreSQL. Para Spring Boot debe quedar en formato JDBC:

```text
jdbc:postgresql://HOST/DATABASE?sslmode=require
```

Neon requiere SSL/TLS; por eso la URL debe incluir normalmente `sslmode=require`.

En produccion usa `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`, no `update`. Con `validate`, Hibernate solo valida el esquema: las tablas deben existir previamente en Neon antes de arrancar la aplicacion.

Las credenciales reales de Neon deben configurarse solo en Environment Variables de Render. No deben guardarse en `.env.example`, `application.properties`, README ni ningun archivo versionado.

### Dockerfile Para Render

El Dockerfile disponible en `backend/Dockerfile` usa Java 21:

```dockerfile
FROM maven:3.9.9-eclipse-temurin-21 AS build
FROM eclipse-temurin:21-jre
```

El Dockerfile expone `8080`, pero el puerto real lo decide Render mediante la variable `PORT`, que Spring Boot lee desde `application.properties`. No hay credenciales hardcodeadas en la imagen.

### Compilar

```bash
mvn compile
```

### Ejecutar tests

```bash
mvn test
```

Los tests usan H2 en memoria por scope `test`. No usan Docker, Neon ni Testcontainers.

Configuracion de tests:

```properties
spring.datasource.url=jdbc:h2:mem:nexora_test_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

La configuracion vive en:

```text
src/test/resources/application.properties
```

## Testing

El proyecto incluye una capa de testing para validar smoke test, services, controllers y persistencia basica.

### Smoke test

- `NexoraBackendApplicationTests`

Valida que el contexto Spring Boot cargue correctamente usando la configuracion de test.

### Unit tests de services

Usan JUnit 5 y Mockito con `@ExtendWith(MockitoExtension.class)`, `@Mock` para repositories y `@InjectMocks` para services.

Tests incluidos:

- `ProveedorServiceTest`
- `UsuarioServiceTest`
- `SolicitudCompraServiceTest`
- `CotizacionServiceTest`
- `OrdenCompraServiceTest`

Cobertura principal:

- Creacion de proveedor y usuario.
- Busqueda por ID existente e inexistente.
- Excepciones `ResourceNotFoundException`.
- Creacion de solicitud asociada a usuario existente.
- Fallo si el usuario solicitante no existe.
- Creacion de cotizacion con solicitud y proveedor existentes.
- Fallo si falta solicitud o proveedor.
- Creacion de orden de compra con cotizacion ganadora valida.
- Validacion de que la cotizacion ganadora pertenece a la solicitud indicada.

### Tests MVC de controllers

Usan `@WebMvcTest`, `MockMvc` y mocks del service layer. En Spring Boot 4 se usa `@MockitoBean` para registrar mocks en el contexto de test MVC.

Tests incluidos:

- `HealthControllerTest`
- `ProveedorControllerTest`

Cobertura principal:

- `GET /api/health` devuelve `200 OK`.
- `GET /api/proveedores` devuelve `200 OK`.
- `POST /api/proveedores` con payload valido devuelve `201 CREATED`.
- `POST /api/proveedores` con payload invalido devuelve `400 BAD REQUEST`.

### Tests JPA de repositories

Usan `@DataJpaTest` con H2 en memoria.

Tests incluidos:

- `ProveedorRepositoryTest`
- `UsuarioRepositoryTest`

Cobertura principal:

- Guardar entidad.
- Buscar por ID.
- Validar unicidad de `Usuario.email`.
- Validar unicidad de `Proveedor.email`.
- Validar unicidad de `Proveedor.rut`.

### Resultado esperado

```text
Tests run: 24, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Ejecutar la API

```bash
mvn spring-boot:run
```

La API queda disponible en:

```text
http://localhost:8080
```

Health check:

```text
GET http://localhost:8080/api/health
```

Swagger UI:

```text
GET http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
GET http://localhost:8080/v3/api-docs
```

## Endpoints

### Health

- `GET /api/health`

### Usuarios

- `GET /api/usuarios?page=0&size=20`
- `GET /api/usuarios/{id}`
- `POST /api/usuarios`
- `PUT /api/usuarios/{id}`
- `DELETE /api/usuarios/{id}`

Campos relevantes:

- `nombre`
- `email`
- `rol`
- `activo`

### Proveedores

- `GET /api/proveedores?page=0&size=20`
- `GET /api/proveedores/{id}`
- `POST /api/proveedores`
- `PUT /api/proveedores/{id}`
- `DELETE /api/proveedores/{id}`

Campos relevantes:

- `rut`
- `razonSocial`
- `nombreContacto`
- `email`
- `telefono`
- `direccion`
- `reputacionScore`
- `cumplimientoScore`
- `estado`

### Solicitudes de compra

- `GET /api/solicitudes-compra?page=0&size=20`
- `GET /api/solicitudes-compra/{id}`
- `POST /api/solicitudes-compra`
- `PUT /api/solicitudes-compra/{id}`
- `DELETE /api/solicitudes-compra/{id}`

Campos relevantes:

- `titulo`
- `descripcion`
- `categoria`
- `montoEstimado`
- `fechaRequerida`
- `estado`
- `usuarioSolicitanteId`

### Cotizaciones

- `GET /api/cotizaciones?page=0&size=20`
- `GET /api/cotizaciones/{id}`
- `POST /api/cotizaciones`
- `PUT /api/cotizaciones/{id}`
- `DELETE /api/cotizaciones/{id}`

Campos relevantes:

- `solicitudCompraId`
- `proveedorId`
- `monto`
- `plazoEntregaDias`
- `condiciones`
- `riskScore`
- `estado`

### Negociaciones

- `GET /api/negociaciones`
- `GET /api/negociaciones/{id}`
- `POST /api/negociaciones`
- `PUT /api/negociaciones/{id}`
- `DELETE /api/negociaciones/{id}`

Campos relevantes:

- `cotizacionId`
- `mensaje`
- `montoOfertado`
- `estado`

### Ordenes de compra

- `GET /api/ordenes-compra?page=0&size=20`
- `GET /api/ordenes-compra/{id}`
- `POST /api/ordenes-compra`
- `PUT /api/ordenes-compra/{id}`
- `DELETE /api/ordenes-compra/{id}`

Campos relevantes:

- `numero`
- `solicitudCompraId`
- `cotizacionGanadoraId`
- `montoTotal`
- `estado`

### Pipelines

- `GET /api/pipelines`
- `GET /api/pipelines/{id}`
- `POST /api/pipelines`
- `PUT /api/pipelines/{id}`
- `DELETE /api/pipelines/{id}`

Campos relevantes:

- `nombre`
- `descripcion`
- `tipo`
- `activo`

### Ejecuciones de pipeline

- `GET /api/pipeline-ejecuciones`
- `GET /api/pipeline-ejecuciones/{id}`
- `POST /api/pipeline-ejecuciones`
- `PUT /api/pipeline-ejecuciones/{id}`
- `DELETE /api/pipeline-ejecuciones/{id}`

Campos relevantes:

- `pipelineId`
- `solicitudCompraId`
- `estado`
- `registrosProcesados`
- `erroresEncontrados`
- `duracionMs`
- `finalizadoEn`
- `resumen`

### Actuator

- `GET /actuator/health`
- `GET /actuator/info`
- `GET /actuator/metrics`

## Estructura

```text
src/main/java/cl/duoc/nexora/backend
|-- config
|-- controller
|-- dto
|   |-- request
|   `-- response
|-- enums
|-- exception
|-- mapper
|-- model
|-- repository
`-- service
```

Responsabilidades por paquete:

- `model`: entidades JPA del MER.
- `repository`: interfaces Spring Data JPA.
- `service`: logica de aplicacion y transacciones.
- `controller`: API REST.
- `dto/request`: records de entrada con validaciones.
- `dto/response`: records de salida.
- `mapper`: conversion simple entre entidades y DTOs.
- `enums`: estados, roles y tipos persistidos como `EnumType.STRING`.
- `exception`: manejo centralizado de errores.
- `config`: configuraciones futuras del proyecto.

## Estado del proyecto

Estado actual: MVP backend funcional en desarrollo.

Incluye:

- Entidades JPA base alineadas al MER.
- Repositories para todas las entidades principales.
- CRUD REST para proveedores, solicitudes de compra, cotizaciones, ordenes de compra, pipelines y ejecuciones de pipeline.
- Health endpoint propio en `/api/health`.
- Actuator habilitado para `health`, `info` y `metrics`.
- Configuracion local para PostgreSQL 17.6 instalado en Windows.
- Docker Compose disponible solo como alternativa opcional.
- H2 para tests de contexto.
- Validaciones de entrada con Jakarta Validation.
- Mappers para evitar exponer entidades JPA directamente.
- Paginacion y filtros en listados principales.
- Configuracion preparada para Render con Neon PostgreSQL via variables de entorno.

No incluye todavia:

- Spring Security.
- Login o autenticacion.
- Migraciones Flyway/Liquibase.
- CRUD completo para errores de pipeline y KPI.
