# Resumen 2 - Alineacion MER, configuracion y ampliacion API Nexora Backend

Este documento describe con detalle la segunda iteracion realizada sobre Nexora Backend, tomando en cuenta el mapa entidad relacion del producto y las tareas adicionales solicitadas.

## Objetivo de esta iteracion

El objetivo fue alinear el backend Spring Boot Java 21 con el MER revisado del producto y dejar el proyecto mejor preparado para ejecucion local con PostgreSQL, documentacion de uso y nuevos endpoints del flujo operativo.

Las tareas cubiertas fueron:

- Completar configuracion de `application.properties` para PostgreSQL, JPA, Actuator y puerto `8080`.
- Ajustar `compose.yaml` para PostgreSQL 16 con credenciales y base de datos definidas.
- Crear `README.md` de raiz.
- Crear `.env.example`.
- Actualizar `.gitignore`.
- Ajustar entidades JPA segun el MER.
- Crear CRUD para `OrdenCompra`.
- Crear capa basica para `Pipeline` y `PipelineEjecucion`.
- Mantener package base `cl.duoc.nexora.backend`.
- No agregar Spring Security ni login.
- Verificar compilacion.

## Revision previa

Antes de modificar codigo se revisaron los archivos afectados para no duplicar clases existentes.

Archivos revisados:

- `compose.yaml`
- `.gitignore`
- `src/main/resources/application.properties`
- `src/main/java/cl/duoc/nexora/backend/model/OrdenCompra.java`
- `src/main/java/cl/duoc/nexora/backend/model/PipelineEjecucion.java`
- `src/main/java/cl/duoc/nexora/backend/model/KpiResultado.java`
- `src/main/java/cl/duoc/nexora/backend/model/Cotizacion.java`
- `src/main/java/cl/duoc/nexora/backend/model/Proveedor.java`
- `src/main/java/cl/duoc/nexora/backend/model/Pipeline.java`
- `src/main/java/cl/duoc/nexora/backend/model/PipelineError.java`
- `src/main/java/cl/duoc/nexora/backend/model/SolicitudCompra.java`
- DTOs, mappers, services y controllers existentes.

Resultado de la revision:

- Ya existian entidades JPA para el modelo base.
- Ya existian CRUDs para `Proveedor`, `SolicitudCompra` y `Cotizacion`.
- No existian DTOs, mapper, service ni controller para `OrdenCompra`.
- No existian services ni controllers para `Pipeline` y `PipelineEjecucion`.
- `application.properties` ya tenia una configuracion PostgreSQL parcial, pero se dejo mas completa y parametrizable.
- `compose.yaml` usaba `postgres:latest` y credenciales genericas, por lo que fue ajustado.
- `.gitignore` no ignoraba `.env`, `.env.*` ni `.qodo`.
- No existia `README.md` ni `.env.example`.

## Configuracion de application.properties

Archivo modificado:

- `src/main/resources/application.properties`

Se dejo configurado para:

- Nombre de aplicacion.
- Puerto `8080`.
- PostgreSQL local.
- JPA/Hibernate.
- Actuator.

Configuracion final relevante:

```properties
spring.application.name=nexora-backend

server.port=${SERVER_PORT:8080}

spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/nexora_db}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:nexora_user}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:nexora_password}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=${SPRING_JPA_HIBERNATE_DDL_AUTO:update}
spring.jpa.show-sql=${SPRING_JPA_SHOW_SQL:true}
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
management.info.env.enabled=true
```

Decisiones tomadas:

- Se usaron variables de entorno con valores por defecto para facilitar ejecucion local y despliegue futuro.
- Se mantuvo PostgreSQL como base principal del runtime.
- Se configuro `spring.jpa.open-in-view=false` para evitar consultas JPA durante renderizado de respuestas.
- Se expusieron endpoints Actuator basicos: `health`, `info` y `metrics`.

## Ajuste de compose.yaml

Archivo modificado:

- `compose.yaml`

Antes:

- Imagen `postgres:latest`.
- Base `mydatabase`.
- Usuario `myuser`.
- Password `secret`.
- Puerto sin mapeo explicito host-contenedor.

Despues:

```yaml
services:
  postgres:
    image: postgres:16
    container_name: nexora-postgres
    environment:
      POSTGRES_DB: nexora_db
      POSTGRES_USER: nexora_user
      POSTGRES_PASSWORD: nexora_password
    ports:
      - "5432:5432"
    volumes:
      - nexora_postgres_data:/var/lib/postgresql/data

volumes:
  nexora_postgres_data:
```

Decisiones tomadas:

- Se fijo la version `postgres:16` para evitar cambios inesperados de `latest`.
- Se agrego nombre de contenedor `nexora-postgres`.
- Se agrego volumen persistente `nexora_postgres_data`.
- Se expuso explicitamente el puerto `5432:5432`.
- Las credenciales quedaron alineadas con `application.properties`.

## Creacion de .env.example

Archivo creado:

- `.env.example`

Contenido:

```env
SERVER_PORT=8080

SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/nexora_db
SPRING_DATASOURCE_USERNAME=nexora_user
SPRING_DATASOURCE_PASSWORD=nexora_password

SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=true
```

Proposito:

- Documentar variables configurables.
- Dar una plantilla segura para que cada desarrollador cree su `.env` local si lo necesita.
- Evitar versionar secretos reales.

## Actualizacion de .gitignore

Archivo modificado:

- `.gitignore`

Entradas agregadas:

```gitignore
.env
.env.*
!.env.example
.qodo
```

Tambien se mantuvo:

```gitignore
target/
```

Resultado:

- `.env` queda ignorado.
- `.env.*` queda ignorado.
- `.env.example` sigue versionable por la regla `!.env.example`.
- `.qodo` queda ignorado.
- `target/` sigue ignorado.

## Creacion de README.md

Archivo creado:

- `README.md`

Contenido incluido:

- Que es Nexora.
- Stack tecnologico.
- Requisitos.
- Como levantar PostgreSQL con Docker Compose.
- Como compilar.
- Como ejecutar tests.
- Como correr la API.
- Endpoints disponibles.
- Estructura de paquetes.
- Estado actual del proyecto.
- Funcionalidades no incluidas todavia.

Se documento que Nexora es una plataforma para centralizar el ciclo de abastecimiento desde solicitud de compra hasta orden de compra, incluyendo cotizaciones, proveedores, pipelines, errores y KPI.

Endpoints documentados:

- `GET /api/health`
- CRUD de `/api/proveedores`
- CRUD de `/api/solicitudes-compra`
- CRUD de `/api/cotizaciones`
- CRUD de `/api/ordenes-compra`
- CRUD basico de `/api/pipelines`
- CRUD basico de `/api/pipeline-ejecuciones`
- Actuator: `/actuator/health`, `/actuator/info`, `/actuator/metrics`

## Cambios en entidades JPA

Todos los cambios se hicieron sobre clases existentes en `src/main/java/cl/duoc/nexora/backend/model`, sin duplicar entidades.

### Proveedor

Archivo modificado:

- `src/main/java/cl/duoc/nexora/backend/model/Proveedor.java`

Campos agregados:

```java
@Column(precision = 5, scale = 2)
private BigDecimal reputacionScore;

@Column(precision = 5, scale = 2)
private BigDecimal cumplimientoScore;
```

Motivo:

- El MER indica que proveedor debe tener metricas de reputacion y cumplimiento.
- Se modelaron como `BigDecimal` para representar valores decimales con precision controlada.
- Se uso `precision = 5, scale = 2`, suficiente para scores tipo `0.00` a `999.99`.

### Cotizacion

Archivo modificado:

- `src/main/java/cl/duoc/nexora/backend/model/Cotizacion.java`

Campo agregado:

```java
@Column(precision = 5, scale = 2)
private BigDecimal riskScore;
```

Motivo:

- El MER incluye `risk_score` en `COTIZACION`.
- Se agrego para reflejar riesgo calculado o informado por cotizacion.

### OrdenCompra

Archivo modificado:

- `src/main/java/cl/duoc/nexora/backend/model/OrdenCompra.java`

Antes:

- Existia una relacion `OneToOne` con `Cotizacion` llamada `cotizacion`.

Despues:

```java
@OneToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "solicitud_compra_id", nullable = false, unique = true)
private SolicitudCompra solicitudCompra;

@OneToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "cotizacion_ganadora_id", nullable = false, unique = true)
private Cotizacion cotizacionGanadora;
```

Cambios realizados:

- Se agrego relacion `OneToOne` obligatoria con `SolicitudCompra`.
- Se renombro la relacion hacia `Cotizacion` como `cotizacionGanadora`.
- Se uso columna `cotizacion_ganadora_id`.
- Ambas relaciones tienen `unique = true` para representar relaciones 1 a 0..1 segun el MER.

Motivo:

- El MER muestra que una `ORDEN_COMPRA` nace de una `SOLICITUD_COMPRA` y apunta a una cotizacion ganadora.
- El nombre `cotizacionGanadora` expresa mejor la semantica de negocio que `cotizacion`.

### PipelineEjecucion

Archivo modificado:

- `src/main/java/cl/duoc/nexora/backend/model/PipelineEjecucion.java`

Relacion agregada:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "solicitud_compra_id")
private SolicitudCompra solicitudCompra;
```

Campos agregados:

```java
private Integer registrosProcesados;

private Integer erroresEncontrados;

private Long duracionMs;
```

Tambien se ajusto `prePersist()` para valores por defecto:

```java
if (registrosProcesados == null) {
    registrosProcesados = 0;
}
if (erroresEncontrados == null) {
    erroresEncontrados = 0;
}
```

Motivo:

- El MER indica que `PIPELINE_EJECUCION` puede estar asociado opcionalmente a una solicitud.
- El MER incluye metricas operativas de ejecucion: registros procesados, errores encontrados y duracion.
- La relacion con `SolicitudCompra` es opcional porque no todas las ejecuciones tienen que corresponder a una solicitud especifica.

### KpiResultado

Archivo modificado:

- `src/main/java/cl/duoc/nexora/backend/model/KpiResultado.java`

Relacion agregada:

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "pipeline_ejecucion_id", nullable = false)
private PipelineEjecucion pipelineEjecucion;
```

Motivo:

- El MER indica que `KPI_RESULTADO` depende de una ejecucion de pipeline.
- Se hizo obligatoria porque el resultado KPI debe estar trazado a la ejecucion que lo calculo.

## Cambios en DTOs existentes

Se actualizaron DTOs ya existentes para reflejar campos nuevos del MER.

### ProveedorRequest

Archivo modificado:

- `src/main/java/cl/duoc/nexora/backend/dto/request/ProveedorRequest.java`

Campos agregados:

- `reputacionScore`
- `cumplimientoScore`

Validacion agregada:

- `@PositiveOrZero` para ambos scores.

### ProveedorResponse

Archivo modificado:

- `src/main/java/cl/duoc/nexora/backend/dto/response/ProveedorResponse.java`

Campos agregados:

- `reputacionScore`
- `cumplimientoScore`

### CotizacionRequest

Archivo modificado:

- `src/main/java/cl/duoc/nexora/backend/dto/request/CotizacionRequest.java`

Campo agregado:

- `riskScore`

Validacion agregada:

- `@PositiveOrZero`

### CotizacionResponse

Archivo modificado:

- `src/main/java/cl/duoc/nexora/backend/dto/response/CotizacionResponse.java`

Campo agregado:

- `riskScore`

## Cambios en mappers existentes

### ProveedorMapper

Archivo modificado:

- `src/main/java/cl/duoc/nexora/backend/mapper/ProveedorMapper.java`

Se agrego mapeo de:

- `reputacionScore`
- `cumplimientoScore`

En:

- `toEntity`
- `updateEntity`
- `toResponse`

### CotizacionMapper

Archivo modificado:

- `src/main/java/cl/duoc/nexora/backend/mapper/CotizacionMapper.java`

Se agrego mapeo de:

- `riskScore`

En:

- `toEntity`
- `updateEntity`
- `toResponse`

## CRUD de OrdenCompra

Se creo una capa completa para `OrdenCompra` siguiendo el mismo patron usado en los CRUDs existentes.

### OrdenCompraRequest

Archivo creado:

- `src/main/java/cl/duoc/nexora/backend/dto/request/OrdenCompraRequest.java`

Campos:

- `numero`
- `solicitudCompraId`
- `cotizacionGanadoraId`
- `montoTotal`
- `estado`

Validaciones:

- `numero`: obligatorio, maximo 40 caracteres.
- `solicitudCompraId`: obligatorio y positivo.
- `cotizacionGanadoraId`: obligatorio y positivo.
- `montoTotal`: obligatorio y mayor o igual a cero.

### OrdenCompraResponse

Archivo creado:

- `src/main/java/cl/duoc/nexora/backend/dto/response/OrdenCompraResponse.java`

Campos:

- `id`
- `numero`
- `solicitudCompraId`
- `solicitudCompraTitulo`
- `cotizacionGanadoraId`
- `proveedorGanadorId`
- `proveedorGanadorRazonSocial`
- `montoTotal`
- `estado`
- `fechaEmision`

Decision:

- No se expone la entidad `SolicitudCompra`, `Cotizacion` ni `Proveedor` directamente.
- Se exponen solo identificadores y datos resumidos utiles para la respuesta.

### OrdenCompraMapper

Archivo creado:

- `src/main/java/cl/duoc/nexora/backend/mapper/OrdenCompraMapper.java`

Responsabilidades:

- Convertir `OrdenCompraRequest` a `OrdenCompra`.
- Actualizar una `OrdenCompra` existente.
- Convertir `OrdenCompra` a `OrdenCompraResponse`.

Detalles:

- Recibe `SolicitudCompra` y `Cotizacion` ya resueltas por el service.
- Usa `EstadoOrdenCompra.EMITIDA` como default si el request no trae estado.
- En response incluye datos del proveedor ganador a traves de la cotizacion ganadora.

### OrdenCompraService

Archivo creado:

- `src/main/java/cl/duoc/nexora/backend/service/OrdenCompraService.java`

Anotaciones:

- `@Service`
- `@RequiredArgsConstructor`
- `@Transactional`

Dependencias:

- `OrdenCompraRepository`
- `SolicitudCompraRepository`
- `CotizacionRepository`

Operaciones implementadas:

- `listar()`
- `obtenerPorId(Long id)`
- `crear(OrdenCompraRequest request)`
- `actualizar(Long id, OrdenCompraRequest request)`
- `eliminar(Long id)`

Manejo de errores:

- Si no existe la orden, lanza `ResourceNotFoundException`.
- Si no existe la solicitud, lanza `ResourceNotFoundException`.
- Si no existe la cotizacion ganadora, lanza `ResourceNotFoundException`.

### OrdenCompraController

Archivo creado:

- `src/main/java/cl/duoc/nexora/backend/controller/OrdenCompraController.java`

Base path:

- `/api/ordenes-compra`

Endpoints:

- `GET /api/ordenes-compra`
- `GET /api/ordenes-compra/{id}`
- `POST /api/ordenes-compra`
- `PUT /api/ordenes-compra/{id}`
- `DELETE /api/ordenes-compra/{id}`

Detalles:

- `POST` devuelve `201 Created` con header `Location`.
- `DELETE` devuelve `204 No Content`.
- `POST` y `PUT` validan el body con `@Valid`.

## Capa basica para Pipeline

Se agrego CRUD basico para `Pipeline` con DTOs, mapper, service y controller, manteniendo la regla de no exponer entidades JPA directamente.

### PipelineRequest

Archivo creado:

- `src/main/java/cl/duoc/nexora/backend/dto/request/PipelineRequest.java`

Campos:

- `nombre`
- `descripcion`
- `tipo`
- `activo`

Validaciones:

- `nombre`: obligatorio y maximo 120 caracteres.
- `descripcion`: maximo 500 caracteres.
- `tipo`: obligatorio.

### PipelineResponse

Archivo creado:

- `src/main/java/cl/duoc/nexora/backend/dto/response/PipelineResponse.java`

Campos:

- `id`
- `nombre`
- `descripcion`
- `tipo`
- `activo`
- `creadoEn`

### PipelineMapper

Archivo creado:

- `src/main/java/cl/duoc/nexora/backend/mapper/PipelineMapper.java`

Responsabilidades:

- Convertir request a entidad.
- Actualizar entidad existente.
- Convertir entidad a response.

Decision:

- Si `activo` no viene informado al crear, se usa `true`.

### PipelineService

Archivo creado:

- `src/main/java/cl/duoc/nexora/backend/service/PipelineService.java`

Operaciones:

- `listar()`
- `obtenerPorId(Long id)`
- `crear(PipelineRequest request)`
- `actualizar(Long id, PipelineRequest request)`
- `eliminar(Long id)`

### PipelineController

Archivo creado:

- `src/main/java/cl/duoc/nexora/backend/controller/PipelineController.java`

Base path:

- `/api/pipelines`

Endpoints:

- `GET /api/pipelines`
- `GET /api/pipelines/{id}`
- `POST /api/pipelines`
- `PUT /api/pipelines/{id}`
- `DELETE /api/pipelines/{id}`

## Capa basica para PipelineEjecucion

Se agrego CRUD basico para `PipelineEjecucion` con DTOs, mapper, service y controller.

### PipelineEjecucionRequest

Archivo creado:

- `src/main/java/cl/duoc/nexora/backend/dto/request/PipelineEjecucionRequest.java`

Campos:

- `pipelineId`
- `solicitudCompraId`
- `estado`
- `registrosProcesados`
- `erroresEncontrados`
- `duracionMs`
- `finalizadoEn`
- `resumen`

Validaciones:

- `pipelineId`: obligatorio y positivo.
- `solicitudCompraId`: positivo cuando viene informado.
- `registrosProcesados`: mayor o igual a cero.
- `erroresEncontrados`: mayor o igual a cero.
- `duracionMs`: mayor o igual a cero.
- `resumen`: maximo 1000 caracteres.

### PipelineEjecucionResponse

Archivo creado:

- `src/main/java/cl/duoc/nexora/backend/dto/response/PipelineEjecucionResponse.java`

Campos:

- `id`
- `pipelineId`
- `pipelineNombre`
- `solicitudCompraId`
- `solicitudCompraTitulo`
- `estado`
- `registrosProcesados`
- `erroresEncontrados`
- `duracionMs`
- `iniciadoEn`
- `finalizadoEn`
- `resumen`

Decision:

- Si no hay solicitud asociada, `solicitudCompraId` y `solicitudCompraTitulo` salen como `null`.

### PipelineEjecucionMapper

Archivo creado:

- `src/main/java/cl/duoc/nexora/backend/mapper/PipelineEjecucionMapper.java`

Responsabilidades:

- Convertir request a entidad.
- Actualizar entidad existente.
- Convertir entidad a response.

Defaults aplicados:

- Si `estado` no viene informado al crear, se usa `EstadoPipelineEjecucion.PENDIENTE`.
- Si `registrosProcesados` no viene informado, se usa `0`.
- Si `erroresEncontrados` no viene informado, se usa `0`.

### PipelineEjecucionService

Archivo creado:

- `src/main/java/cl/duoc/nexora/backend/service/PipelineEjecucionService.java`

Dependencias:

- `PipelineEjecucionRepository`
- `PipelineRepository`
- `SolicitudCompraRepository`

Operaciones:

- `listar()`
- `obtenerPorId(Long id)`
- `crear(PipelineEjecucionRequest request)`
- `actualizar(Long id, PipelineEjecucionRequest request)`
- `eliminar(Long id)`

Manejo de relaciones:

- `pipelineId` es obligatorio y debe existir.
- `solicitudCompraId` es opcional.
- Si `solicitudCompraId` viene informado, debe existir.

### PipelineEjecucionController

Archivo creado:

- `src/main/java/cl/duoc/nexora/backend/controller/PipelineEjecucionController.java`

Base path:

- `/api/pipeline-ejecuciones`

Endpoints:

- `GET /api/pipeline-ejecuciones`
- `GET /api/pipeline-ejecuciones/{id}`
- `POST /api/pipeline-ejecuciones`
- `PUT /api/pipeline-ejecuciones/{id}`
- `DELETE /api/pipeline-ejecuciones/{id}`

## Configuracion de tests con H2

Durante la verificacion, `mvn test` intento usar la configuracion PostgreSQL real y fallo porque el PostgreSQL local tenia credenciales incompatibles o no estaba alineado con el nuevo `compose.yaml`.

Error observado:

```text
FATAL: la autentificacion password fallo para el usuario nexora_user
```

Para evitar que los tests dependan del estado de Docker o de una base local, se agrego configuracion especifica para tests.

Archivo creado:

- `src/test/resources/application.properties`

Contenido relevante:

```properties
spring.datasource.url=jdbc:h2:mem:nexora_test_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
spring.jpa.open-in-view=false

management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

Motivo:

- Los tests deben ser reproducibles sin PostgreSQL externo.
- H2 ya estaba agregado con scope `test` en el `pom.xml` desde la iteracion anterior.
- `create-drop` permite crear el esquema en memoria durante el test y eliminarlo al finalizar.

## Endpoints disponibles despues de esta iteracion

### Health

- `GET /api/health`

### Proveedores

- `GET /api/proveedores`
- `GET /api/proveedores/{id}`
- `POST /api/proveedores`
- `PUT /api/proveedores/{id}`
- `DELETE /api/proveedores/{id}`

### Solicitudes de compra

- `GET /api/solicitudes-compra`
- `GET /api/solicitudes-compra/{id}`
- `POST /api/solicitudes-compra`
- `PUT /api/solicitudes-compra/{id}`
- `DELETE /api/solicitudes-compra/{id}`

### Cotizaciones

- `GET /api/cotizaciones`
- `GET /api/cotizaciones/{id}`
- `POST /api/cotizaciones`
- `PUT /api/cotizaciones/{id}`
- `DELETE /api/cotizaciones/{id}`

### Ordenes de compra

- `GET /api/ordenes-compra`
- `GET /api/ordenes-compra/{id}`
- `POST /api/ordenes-compra`
- `PUT /api/ordenes-compra/{id}`
- `DELETE /api/ordenes-compra/{id}`

### Pipelines

- `GET /api/pipelines`
- `GET /api/pipelines/{id}`
- `POST /api/pipelines`
- `PUT /api/pipelines/{id}`
- `DELETE /api/pipelines/{id}`

### Ejecuciones de pipeline

- `GET /api/pipeline-ejecuciones`
- `GET /api/pipeline-ejecuciones/{id}`
- `POST /api/pipeline-ejecuciones`
- `PUT /api/pipeline-ejecuciones/{id}`
- `DELETE /api/pipeline-ejecuciones/{id}`

### Actuator

- `GET /actuator/health`
- `GET /actuator/info`
- `GET /actuator/metrics`

## Verificaciones ejecutadas

### Compilacion

Comando ejecutado:

```bash
mvn compile
```

Resultado:

- Exitoso.
- Compilaron 65 archivos fuente.
- No hubo errores de Java, Lombok, JPA, DTOs, mappers, services ni controllers.

### Tests antes de separar configuracion H2

Comando ejecutado:

```bash
mvn test
```

Resultado:

- Fallo por autenticacion contra PostgreSQL local.
- El test intentaba levantar el contexto con `nexora_user` y `nexora_password`.
- Esto evidencio que los tests estaban usando la configuracion runtime.

### Tests despues de agregar configuracion test H2

Comando ejecutado:

```bash
mvn test
```

Resultado:

- Exitoso.
- `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`.
- Spring levanto el contexto con H2 en memoria.
- Spring Data detecto 10 repositories JPA.
- Actuator expuso 3 endpoints bajo `/actuator`.

## Reglas respetadas

- No se duplicaron clases existentes.
- Se modificaron entidades existentes en `model/`.
- Se mantuvo package base `cl.duoc.nexora.backend`.
- No se agrego Spring Security.
- No se creo login.
- No se expusieron entidades JPA directamente desde controllers.
- DTOs nuevos fueron creados como `record`.
- Se uso `jakarta.validation` en DTOs.
- Se mantuvo `jakarta.persistence` en entidades.
- Se mantuvo Lombok en entidades.
- Los services nuevos son clases con `@Service`.
- Los controllers nuevos son clases con `@RestController`.
- Los repositories existentes se reutilizaron.
- Los nuevos endpoints siguen el patron REST ya existente.

## Estado final del proyecto tras esta iteracion

El backend queda con:

- Configuracion local lista para PostgreSQL 16.
- Docker Compose alineado con la configuracion de Spring Boot.
- README completo para onboarding y ejecucion.
- `.env.example` para variables locales.
- `.gitignore` actualizado para no versionar variables sensibles.
- Entidades mas alineadas al MER revisado.
- CRUDs disponibles para recursos principales del MVP ampliado.
- Tests desacoplados de PostgreSQL local mediante H2.
- Compilacion y tests pasando.

## Archivos creados en esta iteracion

- `README.md`
- `.env.example`
- `src/test/resources/application.properties`
- `src/main/java/cl/duoc/nexora/backend/dto/request/OrdenCompraRequest.java`
- `src/main/java/cl/duoc/nexora/backend/dto/response/OrdenCompraResponse.java`
- `src/main/java/cl/duoc/nexora/backend/mapper/OrdenCompraMapper.java`
- `src/main/java/cl/duoc/nexora/backend/service/OrdenCompraService.java`
- `src/main/java/cl/duoc/nexora/backend/controller/OrdenCompraController.java`
- `src/main/java/cl/duoc/nexora/backend/dto/request/PipelineRequest.java`
- `src/main/java/cl/duoc/nexora/backend/dto/response/PipelineResponse.java`
- `src/main/java/cl/duoc/nexora/backend/mapper/PipelineMapper.java`
- `src/main/java/cl/duoc/nexora/backend/service/PipelineService.java`
- `src/main/java/cl/duoc/nexora/backend/controller/PipelineController.java`
- `src/main/java/cl/duoc/nexora/backend/dto/request/PipelineEjecucionRequest.java`
- `src/main/java/cl/duoc/nexora/backend/dto/response/PipelineEjecucionResponse.java`
- `src/main/java/cl/duoc/nexora/backend/mapper/PipelineEjecucionMapper.java`
- `src/main/java/cl/duoc/nexora/backend/service/PipelineEjecucionService.java`
- `src/main/java/cl/duoc/nexora/backend/controller/PipelineEjecucionController.java`

## Archivos modificados en esta iteracion

- `compose.yaml`
- `.gitignore`
- `src/main/resources/application.properties`
- `src/main/java/cl/duoc/nexora/backend/model/Proveedor.java`
- `src/main/java/cl/duoc/nexora/backend/model/Cotizacion.java`
- `src/main/java/cl/duoc/nexora/backend/model/OrdenCompra.java`
- `src/main/java/cl/duoc/nexora/backend/model/PipelineEjecucion.java`
- `src/main/java/cl/duoc/nexora/backend/model/KpiResultado.java`
- `src/main/java/cl/duoc/nexora/backend/dto/request/ProveedorRequest.java`
- `src/main/java/cl/duoc/nexora/backend/dto/response/ProveedorResponse.java`
- `src/main/java/cl/duoc/nexora/backend/mapper/ProveedorMapper.java`
- `src/main/java/cl/duoc/nexora/backend/dto/request/CotizacionRequest.java`
- `src/main/java/cl/duoc/nexora/backend/dto/response/CotizacionResponse.java`
- `src/main/java/cl/duoc/nexora/backend/mapper/CotizacionMapper.java`

## Pendientes recomendados

- Agregar migraciones con Flyway o Liquibase.
- Agregar CRUD para `Negociacion`, `PipelineError` y `KpiResultado`.
- Definir reglas de negocio para adjudicacion de cotizacion ganadora.
- Validar que `cotizacionGanadora` pertenezca a la misma `SolicitudCompra` de la orden.
- Agregar restricciones de unicidad controladas desde services para mensajes de error mas claros.
- Agregar tests unitarios y de controller.
- Agregar paginacion y filtros en listados.
- Agregar autenticacion y autorizacion cuando se defina login.
