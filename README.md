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
- Lombok
- Maven
- Docker Compose opcional
- Spring Boot Actuator

## Como correrlo

### Requisitos

- Java 21
- Maven 3.9+
- PostgreSQL 17.6 instalado localmente en Windows
- Docker y Docker Compose solo si quieres usar el entorno alternativo opcional

### Variables de entorno

El proyecto incluye `.env.example` con valores locales sugeridos:

```env
PORT=8080
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/nexora_db
SPRING_DATASOURCE_USERNAME=nexora_user
SPRING_DATASOURCE_PASSWORD=nexora_password
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=true
```

La aplicacion tambien trae valores por defecto equivalentes en `application.properties`, por lo que puede correr localmente contra PostgreSQL 17.6 instalado en Windows sin crear un `.env` obligatorio.

### PostgreSQL Local En Windows

El entorno local principal usa PostgreSQL 17.6 instalado en Windows, no Docker.

Configuracion esperada:

- Host: `localhost`
- Puerto: `5432`
- Base de datos: `nexora_db`
- Usuario: `nexora_user`
- Password: `nexora_password`

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

### Compilar

```bash
mvn compile
```

### Ejecutar tests

```bash
mvn test
```

Los tests usan H2 en memoria por scope `test`.

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

## Endpoints

### Health

- `GET /api/health`

### Usuarios

- `GET /api/usuarios`
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

- `GET /api/proveedores`
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

- `GET /api/solicitudes-compra`
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

- `GET /api/cotizaciones`
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

- `GET /api/ordenes-compra`
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

No incluye todavia:

- Spring Security.
- Login o autenticacion.
- Migraciones Flyway/Liquibase.
- CRUD completo para errores de pipeline y KPI.
- Reglas avanzadas de transicion de estados.
- Paginacion y filtros.
