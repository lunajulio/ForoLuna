# CLAUDE.md

Guía para Claude Code en este repositorio. ForoLuna es un foro para desarrolladores: **frontend Next.js** + **backend Spring Boot (API REST con JWT)** + **MySQL**, desplegado en una EC2 con Docker Compose y CI/CD en GitHub Actions.

## Estructura

```
backend/    Spring Boot 3.3 · Java 17 · Maven (mvnw) · Flyway · JPA · Spring Security + JWT
frontend/   Next.js 15 (App Router) · React 19 · TypeScript · Tailwind 4 · axios
docker-compose.yml · Caddyfile      Producción: mysql + backend + frontend + caddy (HTTPS)
scripts/deploy.sh                   Lo ejecuta el CD en el servidor
.github/workflows/ci-cd.yml         CI (tests + lint/build) y CD (deploy a EC2)
DEPLOY.md · CICD.md                 Guías de despliegue y de CI/CD
```

## Comandos

```bash
# Backend (desde backend/)
./mvnw test                      # 80 tests JUnit sobre H2 en memoria; no necesita MySQL
./mvnw test -Dtest=TokenServiceTest
./mvnw spring-boot:run           # requiere MySQL local y las env DB_USER / DB_PASSWORD
# Cobertura: backend/target/site/jacoco/index.html (se genera en cada `test`)

# Frontend (desde frontend/)
npm run dev                      # http://localhost:3000, API por defecto en http://localhost:8080
npm run lint
npm run build                    # corre lint + chequeo de tipos; debe pasar para el CI
```

Antes de dar por terminado un cambio: `./mvnw test` si tocaste el backend, y `npm run lint && npx tsc --noEmit` si tocaste el frontend.

## Backend: arquitectura y convenciones

Paquete raíz `com.foro.forohub`:
- `controller/`: endpoints REST. Reciben y devuelven **DTOs**, nunca entidades.
- `domain/<agregado>/`: entidad JPA, repositorio y DTOs de cada agregado (`topico`, `respuesta`, `curso`, `usuarios`).
- `infra/security/`: JWT (`TokenService`, `SecurityFilter`), configuración de Spring Security y los servicios de aplicación (`UsuarioService`, `RespuestasService`, `AutenticacionService`).
- `infra/errores/`: `TratadorDeErrores` (`@RestControllerAdvice`) y `ValidacionException`.

Convenciones:
- **El código va en español**: `Topico`, `DatosSubirTopico`, `registrarUsuario`. Mantenlo así.
- **Los DTOs son `record`** con prefijo `Datos…` y Bean Validation (`@NotBlank`, `@Valid`). Los DTOs de salida tienen un constructor que recibe la entidad.
- **Las reglas de negocio de la entidad viven en la entidad**: `Topico.actualizarTopico`, `deshabilitarTopico`, `esAutor`.
- **Errores:** lanza `ValidacionException(HttpStatus, mensaje)` para reglas de negocio (403 si no eres el autor, 409 si hay duplicados) y `EntityNotFoundException` para 404. `TratadorDeErrores` los convierte en `{ "message": "..." }`. **El frontend depende de ese formato.**
- **Borrado lógico:** los tópicos usan `status=false`. Toda consulta de tópicos debe filtrar por activos (`findByIdAndStatusTrue`, `findByStatusTrue`).
- **Autorización:** el autor se toma del `Authentication` (el login del JWT), nunca del cuerpo de la petición. Editar y borrar exige `verificarAutor`.
- **Fechas:** `fechaCreacion` la asigna el servidor (`LocalDateTime.now()`); el cliente no la envía.

### Base de datos: Flyway
- El esquema se define **solo** con migraciones en `backend/src/main/resources/db/migration/` (`ddl-auto=none`).
- **Nunca edites una migración existente (V1–V5)**; ya se aplicaron en producción. Crea una nueva `V6__descripcion.sql`.
- Los tests corren esas mismas migraciones sobre H2 en modo MySQL, así que usa SQL compatible con ambos.

### Tests
- Integración: extiende `IntegrationTestSupport` (`@SpringBootTest` + MockMvc). Usa `unico("prefijo")` en logins y títulos, porque **la base H2 se comparte entre clases de test**.
- Unitarios: Mockito (`@ExtendWith(MockitoExtension.class)`). Si usas `SecurityContextHolder`, límpialo en `@AfterEach`.
- Repositorios: `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)`.
- La configuración de test está en `backend/src/test/resources/application.properties` (H2, secreto JWT de test, CORS).

## Frontend: convenciones
- **Todas las llamadas HTTP van por `src/services/api.ts`** (axios). Interceptores:
  - añade `Authorization: Bearer <token>` desde `localStorage`;
  - ante un 401 fuera de `/login`, cierra sesión y redirige a `/login`.
- **Mensajes de error:** usa `getErrorMessage(error, fallback)` para mostrar el `message` del backend. No uses `any` en los `catch`, porque el lint lo rechaza.
- **Nada de `console.log`** con datos de usuario, contraseñas o tokens.
- **`NEXT_PUBLIC_API_URL` se fija en tiempo de build:** en producción es `/api` (Caddy quita el prefijo y reenvía al backend).

## Configuración (variables de entorno)

| Variable | Dónde | Nota |
|---|---|---|
| `DB_USER`, `DB_PASSWORD` | backend | Sin valor por defecto: sin ellas no arranca |
| `SPRING_DATASOURCE_URL` | backend | En Docker apunta a `mysql:3306` con `allowPublicKeyRetrieval=true` |
| `JWT_SECRET` | backend | El valor por defecto `123456` solo vale para desarrollo |
| `CORS_ALLOWED_ORIGINS` | backend | Por defecto `http://localhost:3000` |
| `NEXT_PUBLIC_API_URL` | frontend (build) | Por defecto `http://localhost:8080` |

En producción todo sale del archivo `.env` del servidor (plantilla: `.env.example`). **El `.env` real nunca se sube a git.**

## Problemas conocidos en esta máquina (Windows)
- **Docker Compose:** las variables de Windows `DB_USER` y `DB_PASSWORD` **tienen prioridad sobre el `.env`**. Para probar localmente, quítalas: `env -u DB_USER -u DB_PASSWORD docker compose ...`.
- **No ejecutes `npm run build` con `npm run dev` corriendo:** sobrescribe `.next` y rompe el servidor de desarrollo. Si pasa, borra `frontend/.next` y reinícialo.
- **Permisos de scripts:** `backend/mvnw` y `scripts/*.sh` deben conservar el bit de ejecución y los finales LF (lo garantiza `.gitattributes`). Si creas scripts nuevos: `git update-index --chmod=+x`.

## Despliegue y CI/CD
- **Pipeline:** un push o PR a `master` dispara el CI (tests del backend y lint/build del frontend). Un merge a `master` además despliega en la EC2 vía SSH, abriendo el puerto 22 solo para la IP del runner (OIDC en AWS).
- **Despliegue en el servidor:** `scripts/deploy.sh <sha>` hace `git reset --hard` y `docker compose up -d --build`.
- **Qué no tocar sin pedirlo:**
  - la condición `sub` del rol de AWS, que exige el entorno `production`;
  - los puertos publicados en `docker-compose.yml`: solo 80/443, y MySQL únicamente en `127.0.0.1`.
