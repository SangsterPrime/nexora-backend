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

## Autor

NEXORA fue desarrollado por Joel Sangster como proyecto académico/fullstack, integrando React, Vite, Spring Boot, PostgreSQL Neon, Render, Vercel y Google OAuth2.

## Notas técnicas

- Frontend desplegado en Vercel.
- Backend desplegado en Render.
- Base de datos en Neon PostgreSQL.
- Login mediante Google OAuth2.
- Vercel usa rewrites como proxy hacia Render para mejorar compatibilidad de sesión.

## Como correrlo

### Requisitos

- Java 21
- Maven 3.9+
- PostgreSQL 17.6 instalado localmente en Windows
- Docker y Docker Compose solo si quieres usar el entorno alternativo opcional

## Configuración de variables de entorno

Para desarrollo local crea un archivo `.env` en la raiz del proyecto basado en `.env.example`. El archivo `.env` esta ignorado por Git y no debe subirse a GitHub.

```env
DB_URL=jdbc:postgresql://HOST/neondb?sslmode=require&channelBinding=require
DB_USER=neondb_owner
DB_PASS=tu_password_aqui
PORT=8080
FRONTEND_URL=http://localhost:5173
OAUTH2_SUCCESS_REDIRECT_URL=http://localhost:5173/app
OAUTH2_FAILURE_REDIRECT_URL=http://localhost:5173/login?error=oauth
```

`application.properties` lee estas variables de entorno:

- `DB_URL`
- `DB_USER`
- `DB_PASS`
- `FRONTEND_URL`
- `OAUTH2_SUCCESS_REDIRECT_URL`
- `OAUTH2_FAILURE_REDIRECT_URL`
- `PORT`

También lee, opcionalmente, las variables de las integraciones externas:

- `N8N_ENABLED`, `N8N_WEBHOOK_URL`, `N8N_WEBHOOK_SECRET` (ver [Integración con n8n](#integración-con-n8n))
- `NEXORA_ML_ENABLED`, `NEXORA_ML_URL`, `NEXORA_ML_API_KEY` (ver [Integración con servicio ML/IA](#integración-con-servicio-mlia-entrenamientoai))

`DB_URL` debe usar formato JDBC, no el formato `postgresql://` entregado originalmente por Neon.

### Neon Local En Windows

El entorno local principal usa Neon PostgreSQL mediante variables de entorno, no credenciales hardcodeadas.

Configuracion esperada:

- URL JDBC en `DB_URL`
- Usuario en `DB_USER`
- Password en `DB_PASS`
- Puerto de la aplicacion en `PORT`

En desarrollo local se usa `spring.jpa.hibernate.ddl-auto=update` para que Hibernate pueda crear o ajustar tablas durante el MVP.

Ejemplo en PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://HOST/neondb?sslmode=require&channelBinding=require"
$env:DB_USER="neondb_owner"
$env:DB_PASS="tu_password_aqui"
$env:PORT="8080"
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

## Render

Render debe ejecutar el backend como Web Service con Java 21. La aplicacion toma el puerto desde `PORT`:

```properties
server.port=${PORT:8080}
```

En Render configura manualmente estas variables en Environment:

```env
DB_URL=jdbc:postgresql://HOST/neondb?sslmode=require&channelBinding=require
DB_USER=neondb_owner
DB_PASS=tu_password_aqui
FRONTEND_URL=https://nexora-fronted.vercel.app
OAUTH2_SUCCESS_REDIRECT_URL=https://nexora-fronted.vercel.app/app
OAUTH2_FAILURE_REDIRECT_URL=https://nexora-fronted.vercel.app/login?error=oauth
GOOGLE_OAUTH_REDIRECT_URI=https://nexora-fronted.vercel.app/login/oauth2/code/google
PORT=10000
```

Neon entrega una connection string PostgreSQL. Para Spring Boot debe quedar en formato JDBC:

```text
jdbc:postgresql://HOST/neondb?sslmode=require&channelBinding=require
```

Neon requiere SSL/TLS; por eso la URL debe incluir normalmente `sslmode=require`.

Las credenciales reales de Neon deben configurarse solo en Environment Variables de Render o en `.env` local. No deben guardarse en `.env.example`, `application.properties`, README ni ningun archivo versionado.

`FRONTEND_URL` no es una credencial. Se usa para CORS y como base de redireccion si no defines URLs OAuth2 explicitas.

`OAUTH2_SUCCESS_REDIRECT_URL` y `OAUTH2_FAILURE_REDIRECT_URL` permiten controlar el destino final del flujo Google OAuth2:

- Exito: `https://nexora-fronted.vercel.app/app`
- Error: `https://nexora-fronted.vercel.app/login?error=oauth`

`GOOGLE_OAUTH_REDIRECT_URI` permite que Google vuelva por el dominio de Vercel y Vercel proxyee `/login/oauth2/code/google` al backend Render:

```env
GOOGLE_OAUTH_REDIRECT_URI=https://nexora-fronted.vercel.app/login/oauth2/code/google
```

En Google Cloud Console agrega estas redirect URI autorizadas:

```text
https://nexora-fronted.vercel.app/login/oauth2/code/google
https://nexora-backend-nb85.onrender.com/login/oauth2/code/google
```

Para compatibilidad con OAuth2 y cookies entre Vercel y Render, la aplicacion usa cookies de sesion `SameSite=None`, `Secure=true` y `server.forward-headers-strategy=framework`. En algunos navegadores moviles, especialmente iPhone/Safari, las cookies cross-site pueden ser mas restrictivas. Si el login movil falla aunque escritorio funcione, probar Chrome movil o desactivar temporalmente `Impedir seguimiento entre sitios` en Safari.

Solucion ideal futura: usar dominio propio compartido, por ejemplo `https://nexora.cl` para frontend y `https://api.nexora.cl` para backend. Eso mejora la compatibilidad de cookies frente a dominios separados `vercel.app` y `onrender.com`.

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
- `N8nIntegrationServiceTest`
- `MlServiceTest` (registro de ejecución/KPIs y manejo de fallos en pipeline)
- `MlServiceClientTest` (cliente HTTP: URL no configurada y servicio inalcanzable)

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
- `IntegrationControllerTest`
- `MlControllerTest`

Cobertura principal:

- `GET /api/health` devuelve `200 OK`.
- `GET /api/proveedores` devuelve `200 OK`.
- `POST /api/proveedores` con payload valido devuelve `201 CREATED`.
- `POST /api/proveedores` con payload invalido devuelve `400 BAD REQUEST`.
- `GET /api/ml/health` y `GET /api/ml/metrics` devuelven `200 OK` con el mapeo correcto.
- Un fallo del servicio ML se traduce a `502`; integración desactivada a `503`.

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
Tests run: 52, Failures: 0, Errors: 0, Skipped: 0
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

Prueba de conexión a base de datos:

```text
GET http://localhost:8080/db-test
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

Respuesta esperada:

```json
{
  "status": "ok",
  "service": "nexora-backend"
}
```

### DB test

- `GET /db-test`

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

### Servicio ML/IA

- `GET /api/ml/health`
- `POST /api/ml/train`
- `POST /api/ml/score`
- `GET /api/ml/metrics`
- `GET /api/ml/predictions`

Ver la sección [Integración con servicio ML/IA (EntrenamientoAI)](#integración-con-servicio-mlia-entrenamientoai) para detalles.

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

## Integración con n8n

Nexora puede enviar eventos a un workflow de n8n mediante webhook cuando ocurren acciones clave (p. ej. creación de una solicitud de compra).

La integración es **opcional y no bloqueante**: si n8n está caído o desactivado, el flujo de Nexora continúa normalmente.

### Variables de entorno en Render

Agrega estas variables en el panel **Environment** del servicio backend en Render:

```env
N8N_ENABLED=true
N8N_WEBHOOK_URL=https://TU-TUNEL.trycloudflare.com/webhook/nexora-solicitud-creada
N8N_WEBHOOK_SECRET=un_secret_seguro_aqui
```

### Notas importantes

- Usa la **Production URL** de n8n (no la webhook-test).  
  En n8n activa el workflow y copia la URL del trigger tipo `POST /webhook/nexora-solicitud-creada`.
- Si usas **Cloudflare Tunnel temporal** (`trycloudflare.com`), la URL cambia cada vez que reinicias el túnel. Actualiza `N8N_WEBHOOK_URL` en Render cuando cambie.
- **No subas el secret a GitHub**. Configúralo solo en Render o en `.env` local.
- n8n debe estar activo/publicado para recibir eventos.

### Endpoint de prueba

```
POST /api/integrations/n8n/test
```

Requiere autenticación (sesión Google OAuth2 activa).

Respuestas posibles:

```json
// n8n desactivado
{ "ok": false, "message": "Integración n8n desactivada" }

// URL no configurada
{ "ok": false, "message": "N8N_WEBHOOK_URL no configurada" }

// Éxito
{ "ok": true,  "message": "Evento de prueba enviado a n8n" }

// n8n caído
{ "ok": false, "message": "Error al comunicarse con n8n: ..." }
```

### Eventos disponibles

| Evento                   | Entidad           | Cuándo se dispara                  |
|--------------------------|-------------------|------------------------------------|
| `SOLICITUD_COMPRA_CREADA` | `SOLICITUD_COMPRA` | Al crear una nueva solicitud de compra |
| `NEXORA_TEST`            | `INTEGRACION`     | Al llamar al endpoint de prueba     |

### Header de autenticación enviado a n8n

```
X-NEXORA-WEBHOOK-SECRET: <valor de N8N_WEBHOOK_SECRET>
```

En el workflow de n8n agrega un nodo de validación del header antes de procesar el evento.

---

## Integración con servicio ML/IA (EntrenamientoAI)

Nexora integra un servicio Python de IA/DataOps (proyecto `EntrenamientoAI`) para entrenar un modelo, scorear registros y exponer métricas y predicciones.

### Arquitectura

```text
Frontend (Vercel)  →  Backend Spring Boot (Render)  →  Servicio Python ML (EntrenamientoAI)
   React/Vite            /api/ml/**                       /health /train /score /metrics /predictions
```

- El **frontend nunca llama directamente** al servicio Python. Siempre pasa por el backend bajo `/api/ml/**`.
- La **API key del servicio ML vive solo en el backend** (variable `NEXORA_ML_API_KEY`) y se envía al servicio Python en el header `X-API-Key`. Nunca se expone al navegador ni se imprime en logs.
- Cada `train` y `score` queda registrado como `PipelineEjecucion`. Si falla, se crea un `PipelineError` y la ejecución se marca `FALLIDA`. Las métricas del modelo se guardan como `KpiResultado` (`ML_ACCURACY`, `ML_PRECISION`, `ML_RECALL`, `ML_F1`, `ML_ROC_AUC`, `ML_GINI`).
- Los pipelines `ML - Entrenamiento` y `ML - Scoring` (tipo `ML`) se crean automáticamente la primera vez que se usan.

### Variables de entorno

`application.properties` lee estas variables de entorno:

- `NEXORA_ML_ENABLED` — activa la integración (default `false`).
- `NEXORA_ML_URL` — URL base del servicio Python, p. ej. `https://nexora-ml.onrender.com`.
- `NEXORA_ML_API_KEY` — API key enviada en el header `X-API-Key`. Es secreta.

En Render configúralas en **Environment** del servicio backend:

```env
NEXORA_ML_ENABLED=true
NEXORA_ML_URL=https://nexora-ml.onrender.com
NEXORA_ML_API_KEY=un_api_key_seguro_aqui
```

**No subas la API key a GitHub.** Configúrala solo en Render o en `.env` local (`.env` está ignorado por Git). El archivo `.env.example` incluye los placeholders.

### Endpoints (fachada del backend)

Todos requieren autenticación (sesión Google OAuth2 activa). Ruta base `/api/ml`:

- `GET /api/ml/health` — estado del servicio Python.
- `POST /api/ml/train` — dispara entrenamiento (cuerpo opcional). Registra ejecución + KPIs.
- `POST /api/ml/score` — scorea registros (cuerpo opcional). Registra ejecución.
- `GET /api/ml/metrics` — métricas del último modelo (`accuracy`, `recall`, `precision`, `f1`, `roc_auc`, `gini`, `matriz_confusion`).
- `GET /api/ml/predictions` — predicciones/resultados scoreados.

### Manejo de errores

`GlobalExceptionHandler` traduce los fallos del servicio ML a respuestas claras con el formato estándar `ApiErrorResponse`:

```json
// Integración desactivada o sin configurar (503)
{ "status": 503, "mensaje": "Integración ML desactivada. Configure NEXORA_ML_ENABLED=true para habilitarla.", "path": "/api/ml/health" }

// URL no configurada (503)
{ "status": 503, "mensaje": "NEXORA_ML_URL no configurada", "path": "/api/ml/health" }

// Servicio ML caído o con error (502)
{ "status": 502, "mensaje": "No se pudo conectar con el servicio de IA", "path": "/api/ml/health" }
{ "status": 502, "mensaje": "El servicio de IA respondió con error 500", "path": "/api/ml/train" }
```

### Cómo probar localmente

1. Levanta el servicio Python `EntrenamientoAI` (por defecto suele exponerse en `http://localhost:8000`).
2. Configura el `.env` del backend:

   ```env
   NEXORA_ML_ENABLED=true
   NEXORA_ML_URL=http://localhost:8000
   NEXORA_ML_API_KEY=el_mismo_api_key_del_servicio_python
   ```

3. Arranca el backend:

   ```powershell
   $env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.11"
   .\mvnw.cmd spring-boot:run
   ```

4. Como los endpoints requieren sesión, inicia sesión con Google OAuth2 desde el frontend y prueba desde ahí, o usa una cookie de sesión válida. Ejemplo de verificación de estado:

   ```text
   GET http://localhost:8080/api/ml/health
   ```

   Si `NEXORA_ML_ENABLED=false`, el backend responde `503` con un mensaje claro sin llamar al servicio Python.

> Nota: las métricas guardadas como `KpiResultado` se redondean a 2 decimales (columna `NUMERIC(15,2)`). El valor completo siempre está disponible vía `GET /api/ml/metrics`.

---

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
- Integración opcional con n8n vía webhook.
- Integración con el servicio Python de IA (EntrenamientoAI) vía `/api/ml/**`, con traza en pipeline y KPIs.

No incluye todavia:

- Spring Security.
- Login o autenticacion.
- Migraciones Flyway/Liquibase.
- CRUD completo para errores de pipeline y KPI.
