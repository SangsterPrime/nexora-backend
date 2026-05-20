# Resumen de implementacion MVP Nexora Backend

Este documento resume en detalle el trabajo realizado sobre el proyecto Spring Boot Java 21 con package base `cl.duoc.nexora.backend`.

## Contexto inicial

Antes de escribir codigo revise la estructura actual del proyecto para no duplicar clases existentes.

Se encontro lo siguiente:

- El proyecto ya tenia la estructura de paquetes requerida bajo `src/main/java/cl/duoc/nexora/backend`.
- Existian las carpetas `model`, `repository`, `service`, `controller`, `dto/request`, `dto/response`, `enums`, `exception`, `mapper` y `config`.
- Las 10 clases de entidades ya existian en `model/`, pero estaban vacias.
- No habia archivos Java existentes en `repository`, `service`, `controller`, `dto/request`, `dto/response`, `enums`, `exception` ni `mapper`.
- El proyecto usaba Maven con Spring Boot 4.0.6 y Java 21.
- Ya estaban presentes dependencias principales como Spring Web MVC, Spring Data JPA, Validation, Actuator, PostgreSQL y Lombok.

Clases vacias encontradas y completadas:

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

## Entidades JPA completadas

Todas las entidades quedaron en `src/main/java/cl/duoc/nexora/backend/model` y mantienen el package `cl.duoc.nexora.backend.model`.

En todas las entidades se uso:

- `jakarta.persistence`
- Lombok para reducir boilerplate
- `@Entity`
- `@Table`
- `@Id`
- `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- `@Getter`
- `@Setter`
- `@NoArgsConstructor`
- `@AllArgsConstructor`
- `@Builder`

Cuando correspondia, se agrego `@Enumerated(EnumType.STRING)` para almacenar enums como texto y no como ordinales.

### Usuario

Archivo: `src/main/java/cl/duoc/nexora/backend/model/Usuario.java`

Campos agregados:

- `id`
- `nombre`
- `email`
- `rol`
- `activo`
- `creadoEn`

Reglas aplicadas:

- `email` es unico.
- `rol` usa el enum `RolUsuario` con `EnumType.STRING`.
- `activo` queda por defecto en `true`.
- `creadoEn` se completa automaticamente con `LocalDateTime.now()` antes de persistir.
- `rol` queda por defecto en `COMPRADOR` si no se informa.

### Proveedor

Archivo: `src/main/java/cl/duoc/nexora/backend/model/Proveedor.java`

Campos agregados:

- `id`
- `rut`
- `razonSocial`
- `nombreContacto`
- `email`
- `telefono`
- `direccion`
- `estado`
- `creadoEn`

Reglas aplicadas:

- `rut` es unico.
- `estado` usa el enum `EstadoProveedor` con `EnumType.STRING`.
- `estado` queda por defecto en `ACTIVO`.
- `creadoEn` se completa automaticamente antes de persistir.

### SolicitudCompra

Archivo: `src/main/java/cl/duoc/nexora/backend/model/SolicitudCompra.java`

Campos agregados:

- `id`
- `titulo`
- `descripcion`
- `categoria`
- `montoEstimado`
- `fechaRequerida`
- `estado`
- `usuarioSolicitante`
- `creadoEn`

Relaciones agregadas:

- `ManyToOne` hacia `Usuario` mediante `usuario_solicitante_id`.

Reglas aplicadas:

- `estado` usa el enum `EstadoSolicitudCompra` con `EnumType.STRING`.
- `estado` queda por defecto en `BORRADOR`.
- `creadoEn` se completa automaticamente antes de persistir.

### Cotizacion

Archivo: `src/main/java/cl/duoc/nexora/backend/model/Cotizacion.java`

Campos agregados:

- `id`
- `solicitudCompra`
- `proveedor`
- `monto`
- `plazoEntregaDias`
- `condiciones`
- `estado`
- `creadoEn`

Relaciones agregadas:

- `ManyToOne` obligatorio hacia `SolicitudCompra` mediante `solicitud_compra_id`.
- `ManyToOne` obligatorio hacia `Proveedor` mediante `proveedor_id`.

Reglas aplicadas:

- `estado` usa el enum `EstadoCotizacion` con `EnumType.STRING`.
- `estado` queda por defecto en `RECIBIDA`.
- `creadoEn` se completa automaticamente antes de persistir.

### Negociacion

Archivo: `src/main/java/cl/duoc/nexora/backend/model/Negociacion.java`

Campos agregados:

- `id`
- `cotizacion`
- `mensaje`
- `montoOfertado`
- `estado`
- `creadoEn`

Relaciones agregadas:

- `ManyToOne` obligatorio hacia `Cotizacion` mediante `cotizacion_id`.

Reglas aplicadas:

- `estado` usa el enum `EstadoNegociacion` con `EnumType.STRING`.
- `estado` queda por defecto en `ABIERTA`.
- `creadoEn` se completa automaticamente antes de persistir.

### OrdenCompra

Archivo: `src/main/java/cl/duoc/nexora/backend/model/OrdenCompra.java`

Campos agregados:

- `id`
- `numero`
- `cotizacion`
- `montoTotal`
- `estado`
- `fechaEmision`

Relaciones agregadas:

- `OneToOne` obligatorio hacia `Cotizacion` mediante `cotizacion_id`.

Reglas aplicadas:

- `numero` es unico.
- `estado` usa el enum `EstadoOrdenCompra` con `EnumType.STRING`.
- `estado` queda por defecto en `EMITIDA`.
- `fechaEmision` se completa automaticamente antes de persistir.

### Pipeline

Archivo: `src/main/java/cl/duoc/nexora/backend/model/Pipeline.java`

Campos agregados:

- `id`
- `nombre`
- `descripcion`
- `tipo`
- `activo`
- `creadoEn`

Reglas aplicadas:

- `tipo` usa el enum `TipoPipeline` con `EnumType.STRING`.
- `activo` queda por defecto en `true`.
- `creadoEn` se completa automaticamente antes de persistir.

### PipelineEjecucion

Archivo: `src/main/java/cl/duoc/nexora/backend/model/PipelineEjecucion.java`

Campos agregados:

- `id`
- `pipeline`
- `estado`
- `iniciadoEn`
- `finalizadoEn`
- `resumen`

Relaciones agregadas:

- `ManyToOne` obligatorio hacia `Pipeline` mediante `pipeline_id`.

Reglas aplicadas:

- `estado` usa el enum `EstadoPipelineEjecucion` con `EnumType.STRING`.
- `estado` queda por defecto en `PENDIENTE`.
- `iniciadoEn` se completa automaticamente antes de persistir.

### PipelineError

Archivo: `src/main/java/cl/duoc/nexora/backend/model/PipelineError.java`

Campos agregados:

- `id`
- `pipelineEjecucion`
- `mensaje`
- `detalle`
- `creadoEn`

Relaciones agregadas:

- `ManyToOne` obligatorio hacia `PipelineEjecucion` mediante `pipeline_ejecucion_id`.

Reglas aplicadas:

- `creadoEn` se completa automaticamente antes de persistir.

### KpiResultado

Archivo: `src/main/java/cl/duoc/nexora/backend/model/KpiResultado.java`

Campos agregados:

- `id`
- `tipo`
- `valor`
- `periodo`
- `calculadoEn`

Reglas aplicadas:

- `tipo` usa el enum `TipoKpi` con `EnumType.STRING`.
- `calculadoEn` se completa automaticamente antes de persistir.

## Enums creados

Todos los enums quedaron en `src/main/java/cl/duoc/nexora/backend/enums` con package `cl.duoc.nexora.backend.enums`.

Archivos creados:

- `RolUsuario.java`
- `EstadoProveedor.java`
- `EstadoSolicitudCompra.java`
- `EstadoCotizacion.java`
- `EstadoNegociacion.java`
- `EstadoOrdenCompra.java`
- `TipoPipeline.java`
- `EstadoPipelineEjecucion.java`
- `TipoKpi.java`

Valores definidos:

`RolUsuario`:

- `ADMIN`
- `COMPRADOR`
- `APROBADOR`

`EstadoProveedor`:

- `ACTIVO`
- `INACTIVO`

`EstadoSolicitudCompra`:

- `BORRADOR`
- `ABIERTA`
- `COTIZANDO`
- `ADJUDICADA`
- `CANCELADA`

`EstadoCotizacion`:

- `RECIBIDA`
- `EN_REVISION`
- `ACEPTADA`
- `RECHAZADA`

`EstadoNegociacion`:

- `ABIERTA`
- `CERRADA`
- `CANCELADA`

`EstadoOrdenCompra`:

- `EMITIDA`
- `APROBADA`
- `CANCELADA`
- `RECIBIDA`

`TipoPipeline`:

- `SOLICITUD_COMPRA`
- `COTIZACION`
- `ORDEN_COMPRA`
- `KPI`

`EstadoPipelineEjecucion`:

- `PENDIENTE`
- `EN_EJECUCION`
- `EXITOSA`
- `FALLIDA`

`TipoKpi`:

- `AHORRO`
- `TIEMPO_CICLO`
- `COTIZACIONES_RECIBIDAS`
- `ORDENES_EMITIDAS`

## Repositories creados

Todos los repositories quedaron en `src/main/java/cl/duoc/nexora/backend/repository` con package `cl.duoc.nexora.backend.repository`.

Cada repository es una interfaz que extiende `JpaRepository<Entidad, Long>`.

Archivos creados:

- `UsuarioRepository.java`
- `ProveedorRepository.java`
- `SolicitudCompraRepository.java`
- `CotizacionRepository.java`
- `NegociacionRepository.java`
- `OrdenCompraRepository.java`
- `PipelineRepository.java`
- `PipelineEjecucionRepository.java`
- `PipelineErrorRepository.java`
- `KpiResultadoRepository.java`

Esto deja disponibles operaciones basicas de persistencia para todas las entidades del MER:

- Buscar por id
- Listar todos
- Guardar
- Eliminar
- Verificar existencia por id

## DTOs request creados

Todos los DTOs de entrada quedaron en `src/main/java/cl/duoc/nexora/backend/dto/request` y son `record`, tal como se solicito.

Tambien se agregaron validaciones con `jakarta.validation`.

### ProveedorRequest

Archivo: `src/main/java/cl/duoc/nexora/backend/dto/request/ProveedorRequest.java`

Campos:

- `rut`
- `razonSocial`
- `nombreContacto`
- `email`
- `telefono`
- `direccion`
- `estado`

Validaciones aplicadas:

- `rut`: obligatorio y maximo 20 caracteres.
- `razonSocial`: obligatorio y maximo 160 caracteres.
- `nombreContacto`: maximo 120 caracteres.
- `email`: obligatorio, formato email y maximo 160 caracteres.
- `telefono`: maximo 30 caracteres.
- `direccion`: maximo 250 caracteres.

### SolicitudCompraRequest

Archivo: `src/main/java/cl/duoc/nexora/backend/dto/request/SolicitudCompraRequest.java`

Campos:

- `titulo`
- `descripcion`
- `categoria`
- `montoEstimado`
- `fechaRequerida`
- `estado`
- `usuarioSolicitanteId`

Validaciones aplicadas:

- `titulo`: obligatorio y maximo 160 caracteres.
- `descripcion`: maximo 1000 caracteres.
- `categoria`: maximo 120 caracteres.
- `montoEstimado`: obligatorio y mayor o igual a cero.
- `fechaRequerida`: fecha presente o futura.
- `usuarioSolicitanteId`: positivo cuando se informa.

### CotizacionRequest

Archivo: `src/main/java/cl/duoc/nexora/backend/dto/request/CotizacionRequest.java`

Campos:

- `solicitudCompraId`
- `proveedorId`
- `monto`
- `plazoEntregaDias`
- `condiciones`
- `estado`

Validaciones aplicadas:

- `solicitudCompraId`: obligatorio y positivo.
- `proveedorId`: obligatorio y positivo.
- `monto`: obligatorio y mayor o igual a cero.
- `plazoEntregaDias`: positivo cuando se informa.
- `condiciones`: maximo 1000 caracteres.

## DTOs response creados

Todos los DTOs de salida quedaron en `src/main/java/cl/duoc/nexora/backend/dto/response` y son `record`.

### ProveedorResponse

Archivo: `src/main/java/cl/duoc/nexora/backend/dto/response/ProveedorResponse.java`

Campos expuestos:

- `id`
- `rut`
- `razonSocial`
- `nombreContacto`
- `email`
- `telefono`
- `direccion`
- `estado`
- `creadoEn`

### SolicitudCompraResponse

Archivo: `src/main/java/cl/duoc/nexora/backend/dto/response/SolicitudCompraResponse.java`

Campos expuestos:

- `id`
- `titulo`
- `descripcion`
- `categoria`
- `montoEstimado`
- `fechaRequerida`
- `estado`
- `usuarioSolicitanteId`
- `usuarioSolicitanteNombre`
- `creadoEn`

### CotizacionResponse

Archivo: `src/main/java/cl/duoc/nexora/backend/dto/response/CotizacionResponse.java`

Campos expuestos:

- `id`
- `solicitudCompraId`
- `solicitudCompraTitulo`
- `proveedorId`
- `proveedorRazonSocial`
- `monto`
- `plazoEntregaDias`
- `condiciones`
- `estado`
- `creadoEn`

### HealthResponse

Archivo: `src/main/java/cl/duoc/nexora/backend/dto/response/HealthResponse.java`

Campos expuestos:

- `status`

Se usa en el endpoint `GET /api/health`.

### ErrorResponse

Archivo: `src/main/java/cl/duoc/nexora/backend/dto/response/ErrorResponse.java`

Campos expuestos:

- `mensaje`
- `errores`

Se usa para respuestas de errores de validacion y recursos no encontrados.

## Mappers creados

Todos los mappers quedaron en `src/main/java/cl/duoc/nexora/backend/mapper`.

Se implementaron como clases finales con metodos estaticos simples, sin dependencias ni framework adicional.

### ProveedorMapper

Archivo: `src/main/java/cl/duoc/nexora/backend/mapper/ProveedorMapper.java`

Responsabilidades:

- Convertir `ProveedorRequest` a `Proveedor`.
- Actualizar una entidad `Proveedor` existente desde `ProveedorRequest`.
- Convertir `Proveedor` a `ProveedorResponse`.

Detalle importante:

- Si el request no trae `estado`, se usa `EstadoProveedor.ACTIVO` al crear.
- En update, el `estado` solo cambia si el request lo trae informado.

### SolicitudCompraMapper

Archivo: `src/main/java/cl/duoc/nexora/backend/mapper/SolicitudCompraMapper.java`

Responsabilidades:

- Convertir `SolicitudCompraRequest` a `SolicitudCompra`.
- Actualizar una entidad `SolicitudCompra` existente desde `SolicitudCompraRequest`.
- Convertir `SolicitudCompra` a `SolicitudCompraResponse`.

Detalle importante:

- Recibe el `Usuario` ya resuelto desde el service.
- Si el request no trae `estado`, se usa `EstadoSolicitudCompra.BORRADOR` al crear.
- En la respuesta se expone `usuarioSolicitanteId` y `usuarioSolicitanteNombre`, no la entidad completa.

### CotizacionMapper

Archivo: `src/main/java/cl/duoc/nexora/backend/mapper/CotizacionMapper.java`

Responsabilidades:

- Convertir `CotizacionRequest` a `Cotizacion`.
- Actualizar una entidad `Cotizacion` existente desde `CotizacionRequest`.
- Convertir `Cotizacion` a `CotizacionResponse`.

Detalle importante:

- Recibe `SolicitudCompra` y `Proveedor` ya resueltos desde el service.
- Si el request no trae `estado`, se usa `EstadoCotizacion.RECIBIDA` al crear.
- En la respuesta se exponen datos resumidos de la solicitud y del proveedor, no entidades completas.

## Services creados

Todos los services quedaron en `src/main/java/cl/duoc/nexora/backend/service` y estan anotados con `@Service`.

Se uso `@RequiredArgsConstructor` de Lombok para inyeccion por constructor.

Se uso `@Transactional` para operaciones de escritura y `@Transactional(readOnly = true)` para consultas.

### ProveedorService

Archivo: `src/main/java/cl/duoc/nexora/backend/service/ProveedorService.java`

Operaciones implementadas:

- `listar()`
- `obtenerPorId(Long id)`
- `crear(ProveedorRequest request)`
- `actualizar(Long id, ProveedorRequest request)`
- `eliminar(Long id)`

Comportamiento:

- Usa `ProveedorRepository`.
- Usa `ProveedorMapper` para no exponer entidades.
- Lanza `ResourceNotFoundException` si no existe el proveedor solicitado.

### SolicitudCompraService

Archivo: `src/main/java/cl/duoc/nexora/backend/service/SolicitudCompraService.java`

Operaciones implementadas:

- `listar()`
- `obtenerPorId(Long id)`
- `crear(SolicitudCompraRequest request)`
- `actualizar(Long id, SolicitudCompraRequest request)`
- `eliminar(Long id)`

Comportamiento:

- Usa `SolicitudCompraRepository`.
- Usa `UsuarioRepository` para resolver `usuarioSolicitanteId` cuando viene informado.
- Permite que `usuarioSolicitanteId` sea nulo, ya que no se pidio login ni seguridad todavia.
- Usa `SolicitudCompraMapper` para no exponer entidades.
- Lanza `ResourceNotFoundException` si no existe la solicitud o el usuario informado.

### CotizacionService

Archivo: `src/main/java/cl/duoc/nexora/backend/service/CotizacionService.java`

Operaciones implementadas:

- `listar()`
- `obtenerPorId(Long id)`
- `crear(CotizacionRequest request)`
- `actualizar(Long id, CotizacionRequest request)`
- `eliminar(Long id)`

Comportamiento:

- Usa `CotizacionRepository`.
- Usa `SolicitudCompraRepository` para resolver la solicitud asociada.
- Usa `ProveedorRepository` para resolver el proveedor asociado.
- Usa `CotizacionMapper` para no exponer entidades.
- Lanza `ResourceNotFoundException` si no existe la cotizacion, solicitud o proveedor informado.

## Controllers creados

Todos los controllers quedaron en `src/main/java/cl/duoc/nexora/backend/controller` y estan anotados con `@RestController`.

No se expone ninguna entidad JPA directamente desde los controllers. Todos los endpoints reciben DTOs request y responden DTOs response.

### HealthController

Archivo: `src/main/java/cl/duoc/nexora/backend/controller/HealthController.java`

Endpoint creado:

- `GET /api/health`

Respuesta esperada:

```json
{
  "status": "UP"
}
```

### ProveedorController

Archivo: `src/main/java/cl/duoc/nexora/backend/controller/ProveedorController.java`

Base path:

- `/api/proveedores`

Endpoints creados:

- `GET /api/proveedores`
- `GET /api/proveedores/{id}`
- `POST /api/proveedores`
- `PUT /api/proveedores/{id}`
- `DELETE /api/proveedores/{id}`

Detalles:

- `POST` devuelve `201 Created` con header `Location` apuntando al recurso creado.
- `DELETE` devuelve `204 No Content`.
- `POST` y `PUT` usan `@Valid` sobre `ProveedorRequest`.

### SolicitudCompraController

Archivo: `src/main/java/cl/duoc/nexora/backend/controller/SolicitudCompraController.java`

Base path:

- `/api/solicitudes-compra`

Endpoints creados:

- `GET /api/solicitudes-compra`
- `GET /api/solicitudes-compra/{id}`
- `POST /api/solicitudes-compra`
- `PUT /api/solicitudes-compra/{id}`
- `DELETE /api/solicitudes-compra/{id}`

Detalles:

- `POST` devuelve `201 Created` con header `Location` apuntando al recurso creado.
- `DELETE` devuelve `204 No Content`.
- `POST` y `PUT` usan `@Valid` sobre `SolicitudCompraRequest`.

### CotizacionController

Archivo: `src/main/java/cl/duoc/nexora/backend/controller/CotizacionController.java`

Base path:

- `/api/cotizaciones`

Endpoints creados:

- `GET /api/cotizaciones`
- `GET /api/cotizaciones/{id}`
- `POST /api/cotizaciones`
- `PUT /api/cotizaciones/{id}`
- `DELETE /api/cotizaciones/{id}`

Detalles:

- `POST` devuelve `201 Created` con header `Location` apuntando al recurso creado.
- `DELETE` devuelve `204 No Content`.
- `POST` y `PUT` usan `@Valid` sobre `CotizacionRequest`.

## Manejo de errores creado

Se agrego una capa basica de excepciones en `src/main/java/cl/duoc/nexora/backend/exception`.

### ResourceNotFoundException

Archivo: `src/main/java/cl/duoc/nexora/backend/exception/ResourceNotFoundException.java`

Uso:

- Representa recursos no encontrados.
- Se lanza desde services cuando un id no existe.

### GlobalExceptionHandler

Archivo: `src/main/java/cl/duoc/nexora/backend/exception/GlobalExceptionHandler.java`

Responsabilidades:

- Capturar `ResourceNotFoundException` y responder `404 Not Found`.
- Capturar `MethodArgumentNotValidException` y responder `400 Bad Request`.
- Devolver siempre un `ErrorResponse` controlado.

Ejemplo de respuesta para recurso no encontrado:

```json
{
  "mensaje": "Proveedor no encontrado: 99",
  "errores": {}
}
```

Ejemplo de respuesta para validacion:

```json
{
  "mensaje": "Solicitud invalida",
  "errores": {
    "email": "must be a well-formed email address"
  }
}
```

## Dependencia agregada para tests

Archivo modificado:

- `pom.xml`

Se agrego H2 con scope `test`:

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

Motivo:

- El primer `mvn test` fallo porque el test `@SpringBootTest` intentaba levantar el contexto con JPA, pero no habia `spring.datasource.url` configurado ni base embebida disponible.
- El proyecto tiene PostgreSQL como dependencia runtime, pero no tiene configuracion local de datasource en `application.properties`.
- Agregar H2 solo en `test` permite que los tests levanten el contexto sin afectar la configuracion productiva ni cambiar la decision de usar PostgreSQL en runtime.

## Verificaciones ejecutadas

### Compilacion

Comando ejecutado:

```bash
mvn compile
```

Resultado:

- Exitoso.
- Compilaron 50 archivos fuente.
- No hubo errores de imports, JPA, Lombok, DTOs, services ni controllers.

### Tests antes de agregar H2

Comando ejecutado:

```bash
mvn test
```

Resultado inicial:

- Fallo el test de contexto.
- La causa fue falta de datasource configurado.
- Error principal: `Failed to configure a DataSource: 'url' attribute is not specified and no embedded datasource could be configured`.

Accion tomada:

- Se agrego H2 con scope `test` en `pom.xml`.

### Tests despues de agregar H2

Comando ejecutado:

```bash
mvn test
```

Resultado final:

- Exitoso.
- `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`.
- Spring detecto los 10 repositories JPA.
- El contexto levanto correctamente usando H2 en memoria para test.

## Endpoints finales disponibles

Health:

- `GET /api/health`

Proveedor:

- `GET /api/proveedores`
- `GET /api/proveedores/{id}`
- `POST /api/proveedores`
- `PUT /api/proveedores/{id}`
- `DELETE /api/proveedores/{id}`

SolicitudCompra:

- `GET /api/solicitudes-compra`
- `GET /api/solicitudes-compra/{id}`
- `POST /api/solicitudes-compra`
- `PUT /api/solicitudes-compra/{id}`
- `DELETE /api/solicitudes-compra/{id}`

Cotizacion:

- `GET /api/cotizaciones`
- `GET /api/cotizaciones/{id}`
- `POST /api/cotizaciones`
- `PUT /api/cotizaciones/{id}`
- `DELETE /api/cotizaciones/{id}`

## Reglas del pedido cumplidas

- Las entidades JPA quedaron como clases en `model/`.
- Los repositories quedaron como interfaces en `repository/` extendiendo `JpaRepository`.
- Los services quedaron como clases con `@Service`.
- Los controllers quedaron como clases con `@RestController`.
- Los DTOs quedaron como records en `dto/request` y `dto/response`.
- Los enums quedaron en `enums/`.
- Se uso Lombok en entidades.
- Se uso `jakarta.persistence`.
- Se uso `jakarta.validation` en DTOs.
- Se uso `@Enumerated(EnumType.STRING)` para enums persistidos.
- No se agrego Spring Security.
- No se creo login.
- No se exponen entidades directamente en controllers.
- Se crearon mappers simples para convertir Entity a DTO y DTO a Entity en los CRUD solicitados.
- Se creo `GET /api/health`.
- Se creo CRUD base para `Proveedor`, `SolicitudCompra` y `Cotizacion`.
- Se mantuvo el package base `cl.duoc.nexora.backend` en todos los archivos.
- Se rellenaron clases existentes vacias en lugar de duplicarlas.

## Consideraciones pendientes para una siguiente etapa

Este MVP deja la base funcional, pero todavia hay decisiones que convendria abordar mas adelante:

- Configurar `spring.datasource.url`, usuario y password para PostgreSQL en `application.properties`, variables de entorno o perfiles.
- Definir reglas de negocio mas estrictas para transiciones de estado.
- Agregar CRUDs para `Usuario`, `Negociacion`, `OrdenCompra`, `Pipeline`, `PipelineEjecucion`, `PipelineError` y `KpiResultado` si el alcance crece.
- Agregar migraciones con Flyway o Liquibase.
- Agregar tests unitarios y de controller para los endpoints CRUD.
- Agregar paginacion y filtros en listados.
- Definir manejo de duplicados, por ejemplo `rut` repetido en proveedores o `email` repetido en usuarios.
- Definir autenticacion y autorizacion cuando corresponda implementar login.
