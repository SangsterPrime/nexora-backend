# Resumen 3 - Mejoras de calidad, escalabilidad y preparacion Render + Neon

Este documento consolida con detalle las mejoras recientes aplicadas al proyecto Nexora Backend. La iteracion tuvo dos focos principales:

- Elevar la calidad tecnica del MVP sin romper la funcionalidad existente.
- Dejar el backend preparado para despliegue en Render conectado a Neon PostgreSQL, manteniendo desarrollo local con PostgreSQL 17.6 en Windows.

Se trabajo sobre clases y archivos existentes. No se duplicaron clases, no se agrego Spring Security, no se agrego Flyway y no se subieron secretos reales.

## Alcance general

Las mejoras abarcaron estas areas:

- Documentacion tecnica con Springdoc OpenAPI / Swagger UI.
- Logging en services principales usando Lombok `@Slf4j`.
- Respuestas de error mas completas y utiles para clientes REST.
- Auditoria basica de entidades con timestamps automaticos de Hibernate.
- Restricciones de unicidad relevantes para datos criticos.
- Paginacion en listados principales.
- Queries utiles en repositories para filtros de negocio.
- Validaciones de transiciones de estado.
- Proteccion de campos inmutables en operaciones `PUT`.
- Configuracion lista para Render y Neon PostgreSQL con SSL/TLS.
- Documentacion README actualizada para local y produccion.

## Restricciones respetadas

Durante la implementacion se mantuvieron estas restricciones:

- No se agrego Spring Security.
- No se agrego Flyway ni Liquibase.
- No se agregaron secretos reales al repositorio.
- No se cambio el package base `cl.duoc.nexora.backend`.
- No se duplicaron clases.
- Se modificaron las clases existentes.
- Se mantuvo Java 21.
- Se mantuvo Spring Boot `4.0.6`.
- Se mantuvo PostgreSQL local 17.6 como entorno principal de desarrollo.
- Se mantuvo `compose.yaml` solo como alternativa local opcional.

## Springdoc OpenAPI / Swagger UI

Archivo modificado:

- `pom.xml`

Se agrego la dependencia:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.0.3</version>
</dependency>
```

### Motivo

El backend necesitaba una forma estandar de exponer documentacion interactiva de la API REST. Springdoc genera automaticamente la especificacion OpenAPI a partir de los controllers, DTOs y validaciones existentes.

### Compatibilidad

Se reviso la compatibilidad antes de tocar versiones. El proyecto usa Spring Boot `4.0.6`, por lo que se uso `springdoc-openapi-starter-webmvc-ui:3.0.3`, que corresponde a la linea nueva de Springdoc compatible con generaciones recientes de Spring Boot.

No se cambio la version de Spring Boot.

### Endpoints agregados por Springdoc

En ejecucion local quedan disponibles:

```text
GET http://localhost:8080/swagger-ui.html
GET http://localhost:8080/v3/api-docs
```

En Render el host cambia al dominio del servicio, pero las rutas se mantienen:

```text
GET https://TU_SERVICIO.onrender.com/swagger-ui.html
GET https://TU_SERVICIO.onrender.com/v3/api-docs
```

### Observacion operativa

Springdoc queda habilitado por defecto. Durante `mvn test`, Springdoc emite warnings informativos indicando que `/v3/api-docs` y `/swagger-ui.html` estan habilitados. No son errores.

## Logging con @Slf4j en services principales

Archivos modificados:

- `src/main/java/cl/duoc/nexora/backend/service/UsuarioService.java`
- `src/main/java/cl/duoc/nexora/backend/service/ProveedorService.java`
- `src/main/java/cl/duoc/nexora/backend/service/SolicitudCompraService.java`
- `src/main/java/cl/duoc/nexora/backend/service/CotizacionService.java`
- `src/main/java/cl/duoc/nexora/backend/service/OrdenCompraService.java`

Se agrego:

```java
@Slf4j
```

y el import correspondiente:

```java
import lombok.extern.slf4j.Slf4j;
```

### Operaciones logueadas

Se agregaron logs informativos en operaciones de escritura:

- Creacion de usuarios.
- Actualizacion de usuarios.
- Eliminacion de usuarios.
- Creacion de proveedores.
- Actualizacion de proveedores.
- Eliminacion de proveedores.
- Creacion de solicitudes de compra.
- Actualizacion de solicitudes de compra.
- Eliminacion de solicitudes de compra.
- Creacion de cotizaciones.
- Actualizacion de cotizaciones.
- Eliminacion de cotizaciones.
- Creacion de ordenes de compra.
- Actualizacion de ordenes de compra.
- Eliminacion de ordenes de compra.

### Motivo

Los logs permiten observar eventos de negocio importantes sin necesidad de depurar paso a paso. Esto es especialmente util en Render, donde la visibilidad principal del runtime son los logs del servicio.

### Criterio usado

Se uso nivel `info` para eventos esperados de negocio. No se agregaron logs excesivos en consultas simples para evitar ruido.

## ErrorResponse mejorado

Archivos modificados:

- `src/main/java/cl/duoc/nexora/backend/dto/response/ErrorResponse.java`
- `src/main/java/cl/duoc/nexora/backend/exception/GlobalExceptionHandler.java`

Antes el record tenia solo:

```java
public record ErrorResponse(String mensaje, Map<String, String> errores) {
}
```

Ahora incluye:

```java
public record ErrorResponse(
        Instant timestamp,
        int status,
        String path,
        String mensaje,
        Map<String, String> errores
) {
}
```

### Campos agregados

- `timestamp`: instante exacto en que se genero la respuesta de error.
- `status`: codigo HTTP numerico.
- `path`: ruta solicitada que provoco el error.
- `mensaje`: mensaje general del error.
- `errores`: mapa de errores especificos, usado especialmente para validaciones de campos.

### GlobalExceptionHandler

Se actualizo el handler global para construir respuestas uniformes con:

```java
private ErrorResponse buildError(
        HttpStatus status,
        HttpServletRequest request,
        String mensaje,
        Map<String, String> errores
) {
    return new ErrorResponse(Instant.now(), status.value(), request.getRequestURI(), mensaje, errores);
}
```

### Casos cubiertos

El handler sigue cubriendo:

- `ResourceNotFoundException`: responde `404`.
- `MethodArgumentNotValidException`: responde `400` con errores por campo.
- `IllegalArgumentException`: responde `400`.
- `DataIntegrityViolationException`: responde `409`.

### Beneficio

Los clientes de la API reciben informacion suficiente para depurar errores sin depender de logs internos. Tambien mejora la observabilidad en integraciones futuras.

## Timestamps automaticos en entidades principales

Archivos modificados:

- `src/main/java/cl/duoc/nexora/backend/model/Usuario.java`
- `src/main/java/cl/duoc/nexora/backend/model/Proveedor.java`
- `src/main/java/cl/duoc/nexora/backend/model/SolicitudCompra.java`
- `src/main/java/cl/duoc/nexora/backend/model/Cotizacion.java`
- `src/main/java/cl/duoc/nexora/backend/model/OrdenCompra.java`

Se agregaron anotaciones de Hibernate:

```java
@CreationTimestamp
@UpdateTimestamp
```

### Cambios por entidad

#### Usuario

Se mantuvo:

```java
private LocalDateTime creadoEn;
```

con:

```java
@CreationTimestamp
```

Se agrego:

```java
@UpdateTimestamp
private LocalDateTime actualizadoEn;
```

#### Proveedor

Se mantuvo `creadoEn` con `@CreationTimestamp` y se agrego `actualizadoEn` con `@UpdateTimestamp`.

#### SolicitudCompra

Se mantuvo `creadoEn` con `@CreationTimestamp` y se agrego `actualizadoEn` con `@UpdateTimestamp`.

#### Cotizacion

Se mantuvo `creadoEn` con `@CreationTimestamp` y se agrego `actualizadoEn` con `@UpdateTimestamp`.

#### OrdenCompra

Se mantuvo `fechaEmision` y se paso a manejar con:

```java
@CreationTimestamp
```

Tambien se agrego:

```java
@UpdateTimestamp
private LocalDateTime actualizadoEn;
```

### Logica @PrePersist

Antes algunas entidades asignaban manualmente fechas en `@PrePersist` usando `LocalDateTime.now()`.

Esa logica manual de fechas fue removida donde correspondia. Los `@PrePersist` se conservaron solo para defaults de negocio, por ejemplo:

- Rol por defecto en `Usuario`.
- Activo por defecto en `Usuario`.
- Estado por defecto en `Proveedor`.
- Estado por defecto en `SolicitudCompra`.
- Estado por defecto en `Cotizacion`.
- Estado por defecto en `OrdenCompra`.

### Beneficio

Hibernate se encarga de crear y actualizar timestamps de forma consistente. Esto evita duplicar logica manual y reduce errores por fechas no seteadas.

## Restricciones unique verificadas o agregadas

### Usuario.email

Archivo:

- `src/main/java/cl/duoc/nexora/backend/model/Usuario.java`

Se verifico que ya existia:

```java
@Column(nullable = false, unique = true, length = 160)
private String email;
```

### Proveedor.email

Archivo:

- `src/main/java/cl/duoc/nexora/backend/model/Proveedor.java`

Se agrego `unique = true`:

```java
@Column(nullable = false, unique = true, length = 160)
private String email;
```

### OrdenCompra.numero

Archivo:

- `src/main/java/cl/duoc/nexora/backend/model/OrdenCompra.java`

Se verifico que ya existia:

```java
@Column(nullable = false, unique = true, length = 40)
private String numero;
```

### Consideracion importante para Neon con validate

Si en produccion se usa:

```env
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
```

entonces Neon no creara ni modificara tablas automaticamente. Las constraints `unique` deben existir previamente en la base de datos cloud.

## Paginacion en listados principales

Se cambiaron los listados principales para usar `Pageable` y devolver `Page<T>`.

Controllers modificados:

- `UsuarioController`
- `ProveedorController`
- `SolicitudCompraController`
- `CotizacionController`
- `OrdenCompraController`

Services modificados:

- `UsuarioService`
- `ProveedorService`
- `SolicitudCompraService`
- `CotizacionService`
- `OrdenCompraService`

### Antes

Los listados devolvian listas completas:

```java
public List<UsuarioResponse> listar() {
    return usuarioRepository.findAll().stream()
            .map(UsuarioMapper::toResponse)
            .toList();
}
```

### Ahora

Los listados reciben `Pageable`:

```java
public Page<UsuarioResponse> listar(Boolean activo, Pageable pageable) {
    if (activo != null) {
        return usuarioRepository.findByActivo(activo, pageable).map(UsuarioMapper::toResponse);
    }
    return usuarioRepository.findAll(pageable).map(UsuarioMapper::toResponse);
}
```

### Ejemplos de uso HTTP

Usuarios:

```text
GET /api/usuarios?page=0&size=20
GET /api/usuarios?activo=true&page=0&size=20
```

Proveedores:

```text
GET /api/proveedores?page=0&size=20
GET /api/proveedores?estado=ACTIVO&page=0&size=20
```

Solicitudes de compra:

```text
GET /api/solicitudes-compra?page=0&size=20
GET /api/solicitudes-compra?estado=ABIERTA&page=0&size=20
GET /api/solicitudes-compra?usuarioSolicitanteId=1&page=0&size=20
GET /api/solicitudes-compra?estado=ABIERTA&usuarioSolicitanteId=1&page=0&size=20
```

Cotizaciones:

```text
GET /api/cotizaciones?page=0&size=20
GET /api/cotizaciones?estado=RECIBIDA&page=0&size=20
GET /api/cotizaciones?proveedorId=1&page=0&size=20
GET /api/cotizaciones?solicitudCompraId=1&page=0&size=20
```

Ordenes de compra:

```text
GET /api/ordenes-compra?page=0&size=20
GET /api/ordenes-compra?estado=EMITIDA&page=0&size=20
GET /api/ordenes-compra?proveedorId=1&page=0&size=20
GET /api/ordenes-compra?solicitudCompraId=1&page=0&size=20
```

### Defaults de paginacion

En controllers se uso:

```java
@PageableDefault(size = 20, sort = "id") Pageable pageable
```

Esto deja un tamano por defecto razonable y orden estable por `id`.

### Beneficio

Evita cargar tablas completas en memoria a medida que crecen usuarios, proveedores, solicitudes, cotizaciones y ordenes. Esto prepara mejor al backend para datos reales en Neon.

## Queries utiles en repositories

Repositories modificados:

- `UsuarioRepository`
- `ProveedorRepository`
- `SolicitudCompraRepository`
- `CotizacionRepository`
- `OrdenCompraRepository`

### UsuarioRepository

Se agrego filtro por activo:

```java
Page<Usuario> findByActivo(Boolean activo, Pageable pageable);
```

### ProveedorRepository

Se agrego filtro por estado:

```java
Page<Proveedor> findByEstado(EstadoProveedor estado, Pageable pageable);
```

### SolicitudCompraRepository

Se agregaron filtros por estado y usuario solicitante:

```java
Page<SolicitudCompra> findByEstado(EstadoSolicitudCompra estado, Pageable pageable);

Page<SolicitudCompra> findByUsuarioSolicitanteId(Long usuarioSolicitanteId, Pageable pageable);

Page<SolicitudCompra> findByEstadoAndUsuarioSolicitanteId(
        EstadoSolicitudCompra estado,
        Long usuarioSolicitanteId,
        Pageable pageable
);
```

### CotizacionRepository

Se agregaron filtros directos:

```java
Page<Cotizacion> findByEstado(EstadoCotizacion estado, Pageable pageable);

Page<Cotizacion> findByProveedorId(Long proveedorId, Pageable pageable);

Page<Cotizacion> findBySolicitudCompraId(Long solicitudCompraId, Pageable pageable);
```

Tambien se agrego una query combinable:

```java
@Query("""
        select c from Cotizacion c
        where (:estado is null or c.estado = :estado)
          and (:proveedorId is null or c.proveedor.id = :proveedorId)
          and (:solicitudCompraId is null or c.solicitudCompra.id = :solicitudCompraId)
        """)
Page<Cotizacion> buscar(
        @Param("estado") EstadoCotizacion estado,
        @Param("proveedorId") Long proveedorId,
        @Param("solicitudCompraId") Long solicitudCompraId,
        Pageable pageable
);
```

### OrdenCompraRepository

Se agregaron filtros directos:

```java
Page<OrdenCompra> findByEstado(EstadoOrdenCompra estado, Pageable pageable);

Page<OrdenCompra> findBySolicitudCompraId(Long solicitudCompraId, Pageable pageable);

Page<OrdenCompra> findByCotizacionGanadoraProveedorId(Long proveedorId, Pageable pageable);
```

Tambien se agrego una query combinable:

```java
@Query("""
        select o from OrdenCompra o
        where (:estado is null or o.estado = :estado)
          and (:proveedorId is null or o.cotizacionGanadora.proveedor.id = :proveedorId)
          and (:solicitudCompraId is null or o.solicitudCompra.id = :solicitudCompraId)
        """)
Page<OrdenCompra> buscar(
        @Param("estado") EstadoOrdenCompra estado,
        @Param("proveedorId") Long proveedorId,
        @Param("solicitudCompraId") Long solicitudCompraId,
        Pageable pageable
);
```

### Motivo

Estos filtros cubren consultas frecuentes del flujo de compras:

- Buscar solicitudes por estado.
- Buscar solicitudes por usuario solicitante.
- Buscar cotizaciones por proveedor.
- Buscar cotizaciones por solicitud.
- Buscar ordenes por proveedor ganador.
- Buscar ordenes por solicitud.
- Buscar por estados de ciclo de vida.

## Validacion de transiciones de estado

Archivos modificados:

- `CotizacionService.java`
- `OrdenCompraService.java`

### Cotizacion

Estados existentes:

```java
RECIBIDA,
EN_REVISION,
ACEPTADA,
RECHAZADA
```

Reglas aplicadas:

- `RECIBIDA` puede pasar a `EN_REVISION`, `ACEPTADA` o `RECHAZADA`.
- `EN_REVISION` puede pasar a `ACEPTADA` o `RECHAZADA`.
- `ACEPTADA` es estado terminal.
- `RECHAZADA` es estado terminal.
- Si el request no trae estado nuevo, no se valida transicion.
- Si el estado nuevo es igual al actual, se permite.

Codigo central:

```java
private void validarTransicionEstado(EstadoCotizacion actual, EstadoCotizacion nuevo) {
    if (nuevo == null || nuevo == actual) {
        return;
    }
    boolean valida = switch (actual) {
        case RECIBIDA -> nuevo == EstadoCotizacion.EN_REVISION
                || nuevo == EstadoCotizacion.ACEPTADA
                || nuevo == EstadoCotizacion.RECHAZADA;
        case EN_REVISION -> nuevo == EstadoCotizacion.ACEPTADA || nuevo == EstadoCotizacion.RECHAZADA;
        case ACEPTADA, RECHAZADA -> false;
    };
    if (!valida) {
        throw new IllegalArgumentException("Transicion de estado de cotizacion invalida: " + actual + " -> " + nuevo);
    }
}
```

### OrdenCompra

Estados existentes:

```java
EMITIDA,
APROBADA,
CANCELADA,
RECIBIDA
```

Reglas aplicadas:

- `EMITIDA` puede pasar a `APROBADA` o `CANCELADA`.
- `APROBADA` puede pasar a `RECIBIDA` o `CANCELADA`.
- `RECIBIDA` es estado terminal.
- `CANCELADA` es estado terminal.
- Si el request no trae estado nuevo, no se valida transicion.
- Si el estado nuevo es igual al actual, se permite.

Codigo central:

```java
private void validarTransicionEstado(EstadoOrdenCompra actual, EstadoOrdenCompra nuevo) {
    if (nuevo == null || nuevo == actual) {
        return;
    }
    boolean valida = switch (actual) {
        case EMITIDA -> nuevo == EstadoOrdenCompra.APROBADA || nuevo == EstadoOrdenCompra.CANCELADA;
        case APROBADA -> nuevo == EstadoOrdenCompra.RECIBIDA || nuevo == EstadoOrdenCompra.CANCELADA;
        case RECIBIDA, CANCELADA -> false;
    };
    if (!valida) {
        throw new IllegalArgumentException("Transicion de estado de orden de compra invalida: " + actual + " -> " + nuevo);
    }
}
```

### Beneficio

El backend deja de aceptar cambios arbitrarios de estado que podrian romper el flujo de negocio. Esto evita, por ejemplo, reabrir una cotizacion ya rechazada o mover una orden recibida nuevamente a emitida.

## Proteccion de campos inmutables en PUT

Se evitaron cambios de relaciones que no deberian modificarse una vez creado el registro.

### SolicitudCompra

Archivo:

- `SolicitudCompraService.java`

Regla:

- No se permite cambiar `usuarioSolicitante` de una solicitud existente.

Validacion:

```java
private void validarUsuarioSolicitanteInmutable(SolicitudCompra solicitud, Long usuarioSolicitanteId) {
    Long usuarioActualId = solicitud.getUsuarioSolicitante() != null
            ? solicitud.getUsuarioSolicitante().getId()
            : null;
    if (usuarioSolicitanteId != null && !usuarioSolicitanteId.equals(usuarioActualId)) {
        throw new IllegalArgumentException("No se puede cambiar el usuario solicitante de una solicitud existente");
    }
}
```

### Cotizacion

Archivo:

- `CotizacionService.java`

Reglas:

- No se permite cambiar `solicitudCompra`.
- No se permite cambiar `proveedor`.

Validacion:

```java
private void validarRelacionesInmutables(Cotizacion cotizacion, CotizacionRequest request) {
    if (!request.solicitudCompraId().equals(cotizacion.getSolicitudCompra().getId())) {
        throw new IllegalArgumentException("No se puede cambiar la solicitud de compra de una cotizacion existente");
    }
    if (!request.proveedorId().equals(cotizacion.getProveedor().getId())) {
        throw new IllegalArgumentException("No se puede cambiar el proveedor de una cotizacion existente");
    }
}
```

### OrdenCompra

Archivo:

- `OrdenCompraService.java`

Reglas:

- No se permite cambiar `solicitudCompra`.
- No se permite cambiar `cotizacionGanadora`.

Validacion:

```java
private void validarRelacionesInmutables(OrdenCompra ordenCompra, OrdenCompraRequest request) {
    if (!request.solicitudCompraId().equals(ordenCompra.getSolicitudCompra().getId())) {
        throw new IllegalArgumentException("No se puede cambiar la solicitud de compra de una orden existente");
    }
    if (!request.cotizacionGanadoraId().equals(ordenCompra.getCotizacionGanadora().getId())) {
        throw new IllegalArgumentException("No se puede cambiar la cotizacion ganadora de una orden existente");
    }
}
```

### Cambios en mappers

Archivos modificados:

- `SolicitudCompraMapper.java`
- `CotizacionMapper.java`
- `OrdenCompraMapper.java`

Se ajustaron metodos `updateEntity` para no reasignar relaciones inmutables.

Antes, por ejemplo, `CotizacionMapper.updateEntity` recibia y seteaba `SolicitudCompra` y `Proveedor`.

Ahora solo actualiza campos mutables:

```java
public static void updateEntity(Cotizacion cotizacion, CotizacionRequest request) {
    cotizacion.setMonto(request.monto());
    cotizacion.setPlazoEntregaDias(request.plazoEntregaDias());
    cotizacion.setCondiciones(request.condiciones());
    cotizacion.setRiskScore(request.riskScore());
    if (request.estado() != null) {
        cotizacion.setEstado(request.estado());
    }
}
```

## Configuracion para Render y Neon PostgreSQL

Archivo modificado:

- `src/main/resources/application.properties`

Estado actual relevante:

```properties
spring.application.name=nexora-backend

server.port=${PORT:8080}
server.address=0.0.0.0

spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/nexora_db}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:nexora_user}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}

spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=${SPRING_JPA_HIBERNATE_DDL_AUTO:update}
spring.jpa.show-sql=${SPRING_JPA_SHOW_SQL:true}
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false

management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

### Puerto Render

Render inyecta el puerto en la variable:

```env
PORT
```

Por eso se usa:

```properties
server.port=${PORT:8080}
```

En local, si `PORT` no existe, usa `8080`.

### Bind address

Se mantiene:

```properties
server.address=0.0.0.0
```

Esto es necesario para que la app escuche fuera de `localhost` dentro del contenedor de Render.

### Datasource parametrizado

La URL, usuario y password se leen desde variables:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/nexora_db}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:nexora_user}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}
```

El password default quedo vacio para evitar dejar un secreto como default dentro del archivo de configuracion.

### ddl-auto por variable

Se dejo:

```properties
spring.jpa.hibernate.ddl-auto=${SPRING_JPA_HIBERNATE_DDL_AUTO:update}
```

Esto permite dos comportamientos:

- Local: `update` para facilitar desarrollo MVP.
- Produccion Render + Neon: `validate` para evitar modificar esquema automaticamente.

### SQL visible por variable

Se dejo:

```properties
spring.jpa.show-sql=${SPRING_JPA_SHOW_SQL:true}
```

Esto permite:

- Local: `true` para diagnostico.
- Produccion: `false` para reducir ruido en logs y evitar exponer detalles innecesarios.

### Actuator

Se expone:

```properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

Endpoints disponibles:

```text
GET /actuator/health
GET /actuator/info
GET /actuator/metrics
```

## .env.example actualizado

Archivo modificado:

- `.env.example`

Contenido actual:

```env
PORT=8080

SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/nexora_db
SPRING_DATASOURCE_USERNAME=nexora_user
SPRING_DATASOURCE_PASSWORD=nexora_password

SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=true

# Render + Neon example:
# PORT=10000
# SPRING_DATASOURCE_URL=jdbc:postgresql://TU_HOST_NEON/TU_DATABASE?sslmode=require
# SPRING_DATASOURCE_USERNAME=TU_USER_NEON
# SPRING_DATASOURCE_PASSWORD=TU_PASSWORD_NEON
# SPRING_JPA_HIBERNATE_DDL_AUTO=validate
# SPRING_JPA_SHOW_SQL=false
```

### Motivo

El archivo muestra valores locales concretos y un ejemplo de produccion sin secretos reales.

### Local

Para local se mantiene:

```env
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=true
```

### Render + Neon

Para produccion se documenta:

```env
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_JPA_SHOW_SQL=false
```

### Neon SSL/TLS

La URL de Neon debe incluir:

```text
?sslmode=require
```

Ejemplo JDBC:

```text
jdbc:postgresql://TU_HOST_NEON/TU_DATABASE?sslmode=require
```

## README actualizado

Archivo modificado:

- `README.md`

Se agrego documentacion sobre:

- PostgreSQL local 17.6 en Windows.
- Variables de entorno locales.
- Uso de `ddl-auto=update` en desarrollo.
- Despliegue en Render con Neon.
- Uso de `ddl-auto=validate` en produccion.
- Conversion de connection string Neon a formato JDBC.
- Requisito de `sslmode=require` para Neon.
- Prohibicion de guardar credenciales reales en el repo.
- Necesidad de tener tablas previamente creadas en Neon si se usa `validate`.
- Dockerfile de Render con Java 21.
- Endpoints de Swagger UI y OpenAPI.
- Paginacion en endpoints principales.

## Dockerfile revisado

Archivo revisado:

- `backend/Dockerfile`

Contenido relevante:

```dockerfile
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Validaciones realizadas

- Usa Java 21 para build.
- Usa Java 21 para runtime.
- No hardcodea credenciales.
- Expone `8080` como puerto interno documental.
- El puerto real en Render lo define `PORT`, leido por Spring desde `application.properties`.

### Consideracion para Render

Render puede asignar `PORT=10000` u otro valor. El Dockerfile no necesita hardcodear ese puerto porque la aplicacion usa:

```properties
server.port=${PORT:8080}
```

## compose.yaml mantenido como alternativa local

Archivo revisado:

- `compose.yaml`

Contenido relevante:

```yaml
services:
  postgres:
    image: postgres:17.6
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

### Decision

No se convirtio Docker Compose en requisito. Queda como alternativa opcional para desarrollo local.

El entorno local principal sigue siendo PostgreSQL 17.6 instalado en Windows.

## Configuracion local recomendada

Base esperada:

- Host: `localhost`
- Puerto: `5432`
- Database: `nexora_db`
- Username: `nexora_user`
- Password: `nexora_password`
- PostgreSQL: `17.6`

Variables locales recomendadas:

```env
PORT=8080
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/nexora_db
SPRING_DATASOURCE_USERNAME=nexora_user
SPRING_DATASOURCE_PASSWORD=nexora_password
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=true
```

Comando local:

```powershell
.\mvnw.cmd spring-boot:run
```

## Configuracion Render + Neon recomendada

Variables en Render:

```env
PORT=10000
SPRING_DATASOURCE_URL=jdbc:postgresql://TU_HOST_NEON/TU_DATABASE?sslmode=require
SPRING_DATASOURCE_USERNAME=TU_USER_NEON
SPRING_DATASOURCE_PASSWORD=TU_PASSWORD_NEON
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_JPA_SHOW_SQL=false
```

### Puntos criticos

- Neon requiere SSL/TLS.
- La URL JDBC debe incluir `sslmode=require`.
- No se deben guardar credenciales reales en el repositorio.
- Con `ddl-auto=validate`, Hibernate no crea tablas.
- Las tablas y constraints deben existir previamente en Neon.
- Render debe inyectar las variables como Environment Variables del Web Service.
- La app escucha en `0.0.0.0`.
- La app usa el puerto entregado por `PORT`.

## Archivos principales modificados

### Configuracion y documentacion

- `pom.xml`
- `.env.example`
- `README.md`
- `src/main/resources/application.properties`

### Controllers

- `src/main/java/cl/duoc/nexora/backend/controller/UsuarioController.java`
- `src/main/java/cl/duoc/nexora/backend/controller/ProveedorController.java`
- `src/main/java/cl/duoc/nexora/backend/controller/SolicitudCompraController.java`
- `src/main/java/cl/duoc/nexora/backend/controller/CotizacionController.java`
- `src/main/java/cl/duoc/nexora/backend/controller/OrdenCompraController.java`

### Services

- `src/main/java/cl/duoc/nexora/backend/service/UsuarioService.java`
- `src/main/java/cl/duoc/nexora/backend/service/ProveedorService.java`
- `src/main/java/cl/duoc/nexora/backend/service/SolicitudCompraService.java`
- `src/main/java/cl/duoc/nexora/backend/service/CotizacionService.java`
- `src/main/java/cl/duoc/nexora/backend/service/OrdenCompraService.java`

### Repositories

- `src/main/java/cl/duoc/nexora/backend/repository/UsuarioRepository.java`
- `src/main/java/cl/duoc/nexora/backend/repository/ProveedorRepository.java`
- `src/main/java/cl/duoc/nexora/backend/repository/SolicitudCompraRepository.java`
- `src/main/java/cl/duoc/nexora/backend/repository/CotizacionRepository.java`
- `src/main/java/cl/duoc/nexora/backend/repository/OrdenCompraRepository.java`

### Entidades

- `src/main/java/cl/duoc/nexora/backend/model/Usuario.java`
- `src/main/java/cl/duoc/nexora/backend/model/Proveedor.java`
- `src/main/java/cl/duoc/nexora/backend/model/SolicitudCompra.java`
- `src/main/java/cl/duoc/nexora/backend/model/Cotizacion.java`
- `src/main/java/cl/duoc/nexora/backend/model/OrdenCompra.java`

### Mappers

- `src/main/java/cl/duoc/nexora/backend/mapper/SolicitudCompraMapper.java`
- `src/main/java/cl/duoc/nexora/backend/mapper/CotizacionMapper.java`
- `src/main/java/cl/duoc/nexora/backend/mapper/OrdenCompraMapper.java`

### Errores

- `src/main/java/cl/duoc/nexora/backend/dto/response/ErrorResponse.java`
- `src/main/java/cl/duoc/nexora/backend/exception/GlobalExceptionHandler.java`

## Comandos de verificacion ejecutados

Se ejecuto:

```powershell
.\mvnw.cmd test
```

Resultado:

```text
BUILD SUCCESS
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

Tambien se ejecuto en la iteracion de calidad:

```powershell
.\mvnw.cmd clean test
```

Resultado:

```text
BUILD SUCCESS
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

## Estado final del backend tras esta iteracion

El backend queda en estado MVP mejorado:

- API REST funcional.
- Documentacion Swagger disponible.
- OpenAPI JSON disponible.
- Manejo de errores mas completo.
- Services principales con logging.
- Entidades principales con timestamps automaticos.
- Unicidad reforzada en emails y numero de orden.
- Listados principales paginados.
- Filtros utiles en repositories.
- Validacion de transiciones de estado en cotizaciones y ordenes.
- Proteccion de relaciones inmutables en updates.
- Configuracion preparada para Render.
- Conexion preparada para Neon PostgreSQL con `sslmode=require`.
- Desarrollo local mantenido con PostgreSQL 17.6.
- Docker Compose conservado como opcion local, no requisito.

## Pendientes conscientes

No se implementaron a proposito:

- Spring Security.
- Login.
- Autenticacion.
- Autorizacion por roles.
- Flyway.
- Liquibase.
- Migraciones versionadas.
- Carga inicial de esquema para Neon.

### Riesgo operativo pendiente

Como produccion debe usar `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`, Neon necesitara el esquema creado antes de arrancar la aplicacion. Sin Flyway todavia, ese esquema debera crearse manualmente o mediante un proceso externo temporal hasta que se incorpore migracion versionada.
