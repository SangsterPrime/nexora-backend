# Resumen 4 - Capa de testing real con JUnit 5, Mockito, MockMvc y H2

Este documento detalla la cuarta iteracion aplicada sobre Nexora Backend. El foco fue agregar una capa de testing real para el backend, manteniendo intacta la configuracion preparada para Render + Neon y sin introducir dependencias o herramientas fuera del alcance del MVP.

## Objetivo de esta iteracion

El objetivo principal fue dejar el proyecto con pruebas automatizadas que cubran distintas capas del backend:

- Smoke test de carga del contexto Spring Boot.
- Unit tests de services usando Mockito.
- Tests MVC de controllers usando MockMvc.
- Tests de persistencia con `@DataJpaTest` y H2 en memoria.

La idea fue validar comportamiento real del backend sin depender de PostgreSQL local, Docker, Neon, Testcontainers ni servicios externos.

## Restricciones respetadas

Durante esta iteracion se respetaron las siguientes restricciones:

- No se agrego Spring Security.
- No se agrego Flyway.
- No se agrego Liquibase.
- No se agrego Testcontainers.
- No se uso Docker para tests.
- No se conecto Neon.
- No se modifico la configuracion preparada para Render + Neon.
- No se cambiaron secretos ni se agregaron credenciales reales.
- Se mantuvo Java 21.
- Se mantuvo Spring Boot `4.0.6`.
- Se mantuvo el package base `cl.duoc.nexora.backend`.
- Se mantuvo `NexoraBackendApplicationTests` como smoke test.

## Logica de entornos mantenida

Se mantuvo separada la configuracion por entorno.

### Local

El entorno local sigue orientado a PostgreSQL 17.6 en Windows.

Configuracion esperada:

- Motor: PostgreSQL `17.6`.
- Host: `localhost`.
- Puerto: `5432`.
- Base: `nexora_db`.
- Usuario: `nexora_user`.
- `SPRING_JPA_HIBERNATE_DDL_AUTO=update`.

### Tests

Los tests usan H2 en memoria.

Configuracion esperada:

- Motor: H2 en memoria.
- No usa Docker.
- No usa PostgreSQL local.
- No usa Neon.
- `spring.jpa.hibernate.ddl-auto=create-drop`.

### Render + Neon

La configuracion productiva preparada se mantiene basada en variables de entorno.

Configuracion esperada en Render:

```env
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_JPA_SHOW_SQL=false
SPRING_DATASOURCE_URL=jdbc:postgresql://HOST/DATABASE?sslmode=require
```

Neon no fue conectado en esta iteracion. Solo se mantiene el backend listo para recibir esa configuracion por variables de entorno.

## Configuracion Render + Neon preservada

Archivo revisado y preservado:

- `src/main/resources/application.properties`

Configuracion importante que se mantuvo:

```properties
server.port=${PORT:8080}
server.address=0.0.0.0

spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/nexora_db}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:nexora_user}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}

spring.jpa.hibernate.ddl-auto=${SPRING_JPA_HIBERNATE_DDL_AUTO:update}
spring.jpa.show-sql=${SPRING_JPA_SHOW_SQL:true}
```

### Importancia de no tocar esta configuracion

Esta configuracion permite que el mismo artefacto funcione en distintos entornos:

- En local usa defaults o variables locales.
- En Render usa `PORT` y variables `SPRING_DATASOURCE_*`.
- En Neon usa URL JDBC con `sslmode=require`.
- En produccion se puede usar `validate` sin cambiar codigo.

Por eso la capa de testing se agrego en `src/test/resources/application.properties`, sin romper la configuracion principal.

## Dependencias de testing

Archivo modificado:

- `pom.xml`

### spring-boot-starter-test

Se agrego:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

### Motivo

`spring-boot-starter-test` agrupa dependencias estandar para pruebas en Spring Boot, incluyendo:

- JUnit Jupiter.
- Mockito.
- AssertJ.
- Spring Test.
- Herramientas de test del ecosistema Spring.

Aunque el proyecto ya tenia starters de test mas especificos de Spring Boot 4, se agrego el starter general porque era un requisito explicito y porque facilita una base completa de testing.

### H2

Se verifico que H2 ya existia con scope test:

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

### Motivo

H2 permite ejecutar pruebas de persistencia en memoria, sin levantar PostgreSQL local ni Docker. Esto hace que `mvn test` sea mas rapido, aislado y portable.

## Configuracion H2 para tests

Archivo modificado:

- `src/test/resources/application.properties`

Contenido final:

```properties
spring.datasource.url=jdbc:h2:mem:nexora_test_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

### Detalle de cada propiedad

#### spring.datasource.url

```properties
spring.datasource.url=jdbc:h2:mem:nexora_test_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
```

Define una base H2 en memoria llamada `nexora_test_db`.

`DB_CLOSE_DELAY=-1` mantiene la base viva mientras dure la JVM de test.

`DB_CLOSE_ON_EXIT=FALSE` evita cierres automaticos no deseados durante el ciclo de tests.

#### spring.datasource.driver-class-name

```properties
spring.datasource.driver-class-name=org.h2.Driver
```

Indica explicitamente el driver H2 para tests.

#### spring.datasource.username y password

```properties
spring.datasource.username=sa
spring.datasource.password=
```

Usa credenciales por defecto de H2 en memoria. No hay secretos reales.

#### spring.jpa.hibernate.ddl-auto

```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

Hibernate crea el esquema al iniciar los tests y lo elimina al finalizar. Esto permite validar mappings y constraints de JPA sin depender de una base persistente.

#### spring.jpa.show-sql

```properties
spring.jpa.show-sql=false
```

Se desactiva SQL verboso en tests para reducir ruido en consola.

#### spring.jpa.database-platform

```properties
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

Indica explicitamente el dialecto H2 para el entorno de pruebas.

#### Actuator

```properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

Se conserva la exposicion de endpoints de observabilidad para que el contexto de test mantenga una configuracion coherente con el runtime.

## Smoke test existente

Archivo mantenido:

- `src/test/java/cl/duoc/nexora/backend/NexoraBackendApplicationTests.java`

Contenido relevante:

```java
@SpringBootTest
class NexoraBackendApplicationTests {

    @Test
    void contextLoads() {
    }
}
```

### Proposito

Este test valida que el contexto Spring Boot completo pueda levantar correctamente usando la configuracion de test.

### Valor tecnico

Aunque es simple, detecta problemas grandes de configuracion:

- Beans faltantes.
- Fallas de wiring.
- Problemas de JPA al iniciar.
- Problemas con properties de test.
- Errores de configuracion general de Spring Boot.

## Unit tests de services con Mockito

Se crearon tests unitarios para los services principales.

Directorio:

- `src/test/java/cl/duoc/nexora/backend/service/`

Tests agregados:

- `ProveedorServiceTest`
- `UsuarioServiceTest`
- `SolicitudCompraServiceTest`
- `CotizacionServiceTest`
- `OrdenCompraServiceTest`

### Patron usado

Todos los tests de service usan JUnit 5 y Mockito:

```java
@ExtendWith(MockitoExtension.class)
class NombreServiceTest {

    @Mock
    private AlgunaRepository repository;

    @InjectMocks
    private AlgunaService service;
}
```

### Motivo del patron

Este enfoque prueba la logica del service de forma aislada:

- No levanta contexto Spring.
- No conecta base de datos.
- No depende de JPA real.
- Permite simular respuestas de repositories.
- Permite validar excepciones y caminos de negocio rapido.

## ProveedorServiceTest

Archivo creado:

- `src/test/java/cl/duoc/nexora/backend/service/ProveedorServiceTest.java`

### Casos cubiertos

#### Crear proveedor correctamente

Valida que `ProveedorService.crear`:

- Reciba un `ProveedorRequest` valido.
- Construya la entidad mediante el mapper existente.
- Llame a `proveedorRepository.save`.
- Devuelva un `ProveedorResponse` con datos esperados.

Verificaciones principales:

```java
assertEquals(1L, response.id());
assertEquals("Proveedor SpA", response.razonSocial());
assertEquals("proveedor@nexora.cl", response.email());
verify(proveedorRepository).save(any(Proveedor.class));
```

#### Listar proveedores

Valida que `ProveedorService.listar`:

- Use `Pageable`.
- Devuelva `Page<ProveedorResponse>`.
- Mapee correctamente la entidad hacia DTO.

#### Buscar proveedor por ID existente

Valida que `obtenerPorId` devuelva respuesta cuando el repository retorna `Optional.of(proveedor)`.

#### Lanzar ResourceNotFoundException si no existe

Valida que `obtenerPorId` lance `ResourceNotFoundException` cuando el repository retorna `Optional.empty()`.

## UsuarioServiceTest

Archivo creado:

- `src/test/java/cl/duoc/nexora/backend/service/UsuarioServiceTest.java`

### Casos cubiertos

#### Crear usuario correctamente

Valida que `UsuarioService.crear`:

- Reciba `UsuarioRequest` valido.
- Llame a `usuarioRepository.save`.
- Devuelva `UsuarioResponse` con ID, nombre y email esperados.

#### Buscar usuario por ID

Valida happy path de `obtenerPorId`.

#### Lanzar ResourceNotFoundException si no existe

Valida el camino negativo cuando el usuario no existe.

## SolicitudCompraServiceTest

Archivo creado:

- `src/test/java/cl/duoc/nexora/backend/service/SolicitudCompraServiceTest.java`

### Casos cubiertos

#### Crear solicitud asociada a usuario existente

Valida que `SolicitudCompraService.crear`:

- Busque el usuario solicitante por ID.
- Falle si el usuario no existe.
- Asocie correctamente el usuario a la solicitud.
- Guarde la solicitud.
- Devuelva `SolicitudCompraResponse` con `usuarioSolicitanteId` y nombre esperados.

Verificaciones principales:

```java
assertEquals(10L, response.id());
assertEquals(1L, response.usuarioSolicitanteId());
assertEquals("Ana Compras", response.usuarioSolicitanteNombre());
verify(solicitudCompraRepository).save(any(SolicitudCompra.class));
```

#### Fallar si usuarioSolicitanteId no existe

Valida que se lance `ResourceNotFoundException` si `usuarioRepository.findById` retorna vacio.

## CotizacionServiceTest

Archivo creado:

- `src/test/java/cl/duoc/nexora/backend/service/CotizacionServiceTest.java`

### Casos cubiertos

#### Crear cotizacion asociada a solicitud y proveedor existentes

Valida que `CotizacionService.crear`:

- Busque la solicitud de compra.
- Busque el proveedor.
- Cree la cotizacion con ambas relaciones.
- Guarde la cotizacion.
- Devuelva IDs correctos en el response.

Verificaciones principales:

```java
assertEquals(5L, response.id());
assertEquals(1L, response.solicitudCompraId());
assertEquals(2L, response.proveedorId());
verify(cotizacionRepository).save(any(Cotizacion.class));
```

#### Fallar si solicitud no existe

Valida que si `solicitudCompraRepository.findById` retorna vacio, se lance `ResourceNotFoundException` y no se guarde nada.

#### Fallar si proveedor no existe

Valida que si el proveedor no existe, se lance `ResourceNotFoundException` y no se guarde la cotizacion.

## OrdenCompraServiceTest

Archivo creado:

- `src/test/java/cl/duoc/nexora/backend/service/OrdenCompraServiceTest.java`

### Casos cubiertos

#### Crear orden de compra con solicitud y cotizacion ganadora validas

Valida que `OrdenCompraService.crear`:

- Busque la solicitud.
- Busque la cotizacion ganadora.
- Valide que la cotizacion pertenezca a la misma solicitud.
- Guarde la orden de compra.
- Devuelva response con IDs esperados.

Verificaciones principales:

```java
assertEquals(20L, response.id());
assertEquals("OC-0001", response.numero());
assertEquals(1L, response.solicitudCompraId());
assertEquals(2L, response.cotizacionGanadoraId());
verify(ordenCompraRepository).save(any(OrdenCompra.class));
```

#### Lanzar IllegalArgumentException si la cotizacion no pertenece a la solicitud

Valida la regla de negocio existente:

```java
private void validarCotizacionGanadora(SolicitudCompra solicitudCompra, Cotizacion cotizacionGanadora) {
    Long solicitudCotizacionId = cotizacionGanadora.getSolicitudCompra().getId();
    if (!solicitudCompra.getId().equals(solicitudCotizacionId)) {
        throw new IllegalArgumentException("La cotizacion ganadora debe pertenecer a la solicitud de compra indicada");
    }
}
```

El test construye una solicitud para la orden y una cotizacion asociada a otra solicitud. El resultado esperado es `IllegalArgumentException` y que no se invoque `save`.

## Tests de controllers con MockMvc

Directorio:

- `src/test/java/cl/duoc/nexora/backend/controller/`

Tests agregados:

- `HealthControllerTest`
- `ProveedorControllerTest`

### Herramientas usadas

Se uso:

```java
@WebMvcTest
MockMvc
```

Para mocks dentro del contexto MVC se uso:

```java
@MockitoBean
```

### Nota sobre Spring Boot 4 y @MockitoBean

La tarea original mencionaba `@MockBean`. En Spring Boot 4, con las dependencias actuales del proyecto, `@MockBean` ya no esta disponible en el mismo paquete historico de Boot 3.

Por compatibilidad con Spring Boot `4.0.6`, se uso:

```java
import org.springframework.test.context.bean.override.mockito.MockitoBean;
```

Esto cumple la misma funcion practica para el test MVC: registrar un mock como bean dentro del contexto de test.

## HealthControllerTest

Archivo creado:

- `src/test/java/cl/duoc/nexora/backend/controller/HealthControllerTest.java`

### Caso cubierto

Valida:

```text
GET /api/health
```

Resultado esperado:

```text
200 OK
```

Tambien valida que el JSON contenga:

```json
{
  "status": "UP"
}
```

Test relevante:

```java
mockMvc.perform(get("/api/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
```

## ProveedorControllerTest

Archivo creado:

- `src/test/java/cl/duoc/nexora/backend/controller/ProveedorControllerTest.java`

### Casos cubiertos

#### GET /api/proveedores devuelve 200 OK

Valida que el endpoint de listado responda correctamente.

```text
GET /api/proveedores
```

Resultado esperado:

```text
200 OK
```

Se mockea `ProveedorService.listar` para devolver una pagina con un proveedor.

#### POST /api/proveedores con payload valido devuelve 201 CREATED

Valida que el endpoint de creacion responda correctamente con payload valido.

```text
POST /api/proveedores
```

Resultado esperado:

```text
201 CREATED
Location: /api/proveedores/1
```

El test valida:

```java
.andExpect(status().isCreated())
.andExpect(header().string(HttpHeaders.LOCATION, "/api/proveedores/1"))
.andExpect(jsonPath("$.id").value(1))
.andExpect(jsonPath("$.razonSocial").value("Proveedor SpA"));
```

#### POST /api/proveedores con payload invalido devuelve 400 BAD REQUEST

Valida que Jakarta Validation funcione en el controller.

Se envia `{}` como body, faltando campos requeridos como:

- `rut`
- `razonSocial`
- `email`

Resultado esperado:

```text
400 BAD REQUEST
```

### Detalle tecnico del ObjectMapper

En esta configuracion de `@WebMvcTest` con Spring Boot 4 no quedo disponible un `ObjectMapper` como bean inyectable en el test.

Para evitar tocar configuracion de aplicacion o crear configuraciones extra, se uso un `ObjectMapper` local en el test:

```java
private final ObjectMapper objectMapper = new ObjectMapper();
```

Esto mantiene el cambio acotado al test y no altera la aplicacion.

## Tests JPA de repositories con H2

Directorio:

- `src/test/java/cl/duoc/nexora/backend/repository/`

Tests agregados:

- `ProveedorRepositoryTest`
- `UsuarioRepositoryTest`

### Patron usado

Se uso:

```java
@DataJpaTest
```

### Motivo

`@DataJpaTest` levanta una porcion acotada del contexto enfocada en JPA:

- Repositories.
- EntityManager.
- Datasource de test.
- Mappings JPA.
- Constraints generadas por Hibernate.

No levanta controllers ni services completos, por lo que es mas rapido que un `@SpringBootTest` completo.

## ProveedorRepositoryTest

Archivo creado:

- `src/test/java/cl/duoc/nexora/backend/repository/ProveedorRepositoryTest.java`

### Casos cubiertos

#### Guardar entidad

Valida que se pueda persistir un `Proveedor` valido.

#### Buscar por ID

Valida que el proveedor guardado pueda recuperarse por ID.

```java
assertTrue(proveedorRepository.findById(guardado.getId()).isPresent());
assertEquals("Proveedor SpA", proveedorRepository.findById(guardado.getId()).orElseThrow().getRazonSocial());
```

#### Unique email

Valida que `Proveedor.email` sea unico.

Se guarda un proveedor con email `proveedor@nexora.cl` y luego se intenta guardar otro proveedor con el mismo email.

Resultado esperado:

```java
DataIntegrityViolationException
```

#### Unique rut

Valida que `Proveedor.rut` sea unico.

Se guarda un proveedor con RUT `76000000-1` y luego se intenta guardar otro proveedor con el mismo RUT.

Resultado esperado:

```java
DataIntegrityViolationException
```

## UsuarioRepositoryTest

Archivo creado:

- `src/test/java/cl/duoc/nexora/backend/repository/UsuarioRepositoryTest.java`

### Casos cubiertos

#### Guardar entidad

Valida que se pueda persistir un `Usuario` valido.

#### Buscar por ID

Valida que el usuario guardado pueda recuperarse por ID.

#### Unique email

Valida que `Usuario.email` sea unico.

Se intenta persistir dos usuarios con el mismo email:

```text
ana@nexora.cl
```

Resultado esperado:

```java
DataIntegrityViolationException
```

## Cambios en README

Archivo modificado:

- `README.md`

Se agrego documentacion sobre la nueva capa de testing.

### Stack actualizado

Se agregaron al stack:

- JUnit 5.
- Mockito.
- MockMvc.

### Seccion de tests actualizada

Se documento que:

- Los tests usan H2 en memoria.
- Los tests no usan Docker.
- Los tests no usan Neon.
- Los tests no usan Testcontainers.
- La configuracion de test vive en `src/test/resources/application.properties`.

### Nueva seccion Testing

Se agrego una seccion dedicada con:

- Smoke test.
- Unit tests de services.
- Tests MVC de controllers.
- Tests JPA de repositories.
- Resultado esperado de `mvn test`.

Resultado documentado:

```text
Tests run: 24, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Resultado de mvn test

Comando ejecutado:

```powershell
.\mvnw.cmd test
```

Resultado final:

```text
Tests run: 24, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Distribucion de tests

Se ejecutaron 24 tests en total:

- 1 smoke test.
- 5 tests de controllers.
- 13 tests unitarios de services.
- 5 tests de repositories.

Detalle:

- `HealthControllerTest`: 1 test.
- `ProveedorControllerTest`: 3 tests.
- `NexoraBackendApplicationTests`: 1 test.
- `ProveedorRepositoryTest`: 3 tests.
- `UsuarioRepositoryTest`: 2 tests.
- `CotizacionServiceTest`: 3 tests.
- `OrdenCompraServiceTest`: 2 tests.
- `ProveedorServiceTest`: 4 tests.
- `SolicitudCompraServiceTest`: 2 tests.
- `UsuarioServiceTest`: 3 tests.

## Observaciones de ejecucion

Durante los tests aparecen algunos warnings esperados.

### Mockito agent warning

Se muestra un warning sobre Mockito self-attaching:

```text
Mockito is currently self-attaching to enable the inline-mock-maker.
```

No rompe los tests. Es una advertencia relacionada con cambios futuros del JDK y la forma en que Mockito carga su agente.

### Springdoc warning

Se muestra un warning indicando que `/v3/api-docs` y `/swagger-ui.html` estan habilitados por defecto.

No rompe los tests. Es informativo.

### PageImpl serialization warning

En `ProveedorControllerTest` aparece un warning de Spring Data sobre serializar `PageImpl` directamente.

No rompe los tests. Indica que en una mejora futura podria configurarse serializacion estable de paginas con `PagedModel` o `@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)`.

No se aplico ese cambio ahora porque la tarea era agregar tests y no modificar la API publica.

### Warnings de constraints unique en H2

En los tests de repositories aparecen warnings de H2/Hibernate por violaciones de unicidad.

Son esperados porque justamente se estan validando constraints unique de email y RUT.

## Archivos agregados

### Service tests

- `src/test/java/cl/duoc/nexora/backend/service/ProveedorServiceTest.java`
- `src/test/java/cl/duoc/nexora/backend/service/UsuarioServiceTest.java`
- `src/test/java/cl/duoc/nexora/backend/service/SolicitudCompraServiceTest.java`
- `src/test/java/cl/duoc/nexora/backend/service/CotizacionServiceTest.java`
- `src/test/java/cl/duoc/nexora/backend/service/OrdenCompraServiceTest.java`

### Controller tests

- `src/test/java/cl/duoc/nexora/backend/controller/HealthControllerTest.java`
- `src/test/java/cl/duoc/nexora/backend/controller/ProveedorControllerTest.java`

### Repository tests

- `src/test/java/cl/duoc/nexora/backend/repository/ProveedorRepositoryTest.java`
- `src/test/java/cl/duoc/nexora/backend/repository/UsuarioRepositoryTest.java`

## Archivos modificados

- `pom.xml`
- `src/test/resources/application.properties`
- `README.md`
- `docs/bitacora/resumen4.md`

## Lo que no se cambio

No se cambio la logica de negocio existente.

No se cambio:

- Services productivos, salvo que ya existian de iteraciones anteriores.
- Controllers productivos.
- Entidades productivas.
- Repositories productivos.
- Configuracion principal para Render + Neon.
- Dockerfile.
- Compose.
- `.env.example`.

## Estado final tras esta iteracion

El backend queda con una base de testing real y ejecutable con:

```powershell
.\mvnw.cmd test
```

La suite valida:

- Que el contexto Spring Boot cargue.
- Que los services principales funcionen en happy path y errores esperados.
- Que endpoints minimos respondan con los codigos HTTP esperados.
- Que JPA persista entidades basicas en H2.
- Que constraints unique importantes se cumplan.

## Pendientes futuros recomendados

Estos puntos quedan como mejoras futuras, no implementadas en esta iteracion:

- Agregar tests para mas controllers.
- Agregar tests para endpoints de `SolicitudCompra`, `Cotizacion` y `OrdenCompra`.
- Agregar tests de transiciones de estado invalidas y validas.
- Agregar tests de updates con campos inmutables.
- Agregar tests de paginacion y filtros en todos los listados.
- Agregar serializacion estable para respuestas `Page` si se quiere evitar el warning de `PageImpl`.
- Agregar Flyway cuando el proyecto empiece a requerir esquema versionado.
- Agregar Testcontainers en una etapa posterior para probar contra PostgreSQL real.
- Agregar Spring Security y tests de seguridad cuando se implemente autenticacion.
