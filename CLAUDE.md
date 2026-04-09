# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project
Spring Boot 4.0.4 Todo REST API — study project to practice Java web development end-to-end.

**Collaboration style:** Ricardo implements the code himself. **Do not write code for him.** Provide direction, guidance, and explanations only. He will implement and return with questions.

The full implementation plan is in `PRD.md`.

## Stack
- Java 21
- Spring Boot 4.0.4
- Spring Data JPA + Hibernate
- Spring Validation (Bean Validation / Jakarta)
- Spring Web MVC
- PostgreSQL (via Docker)

## Commands

Using the Maven wrapper (`mvnw.cmd` on Windows, `./mvnw` on Unix):

| Task | Command |
|------|---------|
| Build | `./mvnw clean package` |
| Run app | `./mvnw spring-boot:run` |
| Run all tests | `./mvnw test` |
| Run one test class | `./mvnw test -Dtest=ClassName` |
| Run one test method | `./mvnw test -Dtest=ClassName#methodName` |

Start the database before running the app: `docker compose up -d`

## Architecture

Request flow: `Controller → Service → Repository → PostgreSQL`

- Controllers accept/return DTOs only — entities never leave the service layer.
- Service layer owns entity↔DTO mapping and throws domain exceptions.
- `GlobalExceptionHandler` (`@RestControllerAdvice`) centralizes all error responses.

Package layout under `src/main/java/com/ricardo/todo/`:
- `model/` — JPA entities
- `repository/` — Spring Data JPA interfaces
- `service/` — business logic (`@Service`, `@Transactional`)
- `controller/` — REST controllers (`@RestController`)
- `dto/request/` — validated inbound payloads
- `dto/response/` — outbound shapes
- `exception/` — `TaskNotFoundException`, `GlobalExceptionHandler`, `ErrorResponse`

## Domain model — Task

Base path: `/api/tasks`

| Field | Type | Notes |
|-------|------|-------|
| `id` | `Long` | PK, auto-generated |
| `title` | `String` | Not null, not blank, max 255 |
| `description` | `String` | Optional, max 1000 |
| `completed` | `boolean` | Default `false` |
| `createdAt` | `LocalDateTime` | `@CreationTimestamp`, not updatable |
| `updatedAt` | `LocalDateTime` | `@UpdateTimestamp` |

## Error response shape

All 4xx/5xx responses:
```json
{ "status": 404, "error": "Not Found", "message": "Task not found with id: 42" }
```

## Current state

**Phase 0 — Infrastructure ✅**
- `docker-compose.yml` — PostgreSQL 16 configured, named volume `postgres_data`, DB `postgres_taskmanager`
- `application.properties` — datasource, JPA, dialect (`PostgreSQLDialect`) all configured
- App starts and connects to the bank successfully

**Phase 1 — Domain model ✅**
- `Task.java` — complete: `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `@CreationTimestamp`, `@UpdateTimestamp`, `@NotBlank`, `@Size`, `LocalDateTime`, `Long`, constructors (no-args + with title/description/completed), getters/setters

**Phase 2 — Repository ✅**
- `TaskRepository.java` — `@Repository`, extends `JpaRepository<Task, Long>`

**Phase 3 — DTOs ✅**
- `CreateTaskRequest` — `title` (@NotBlank, @Size 255), `description` (@Size 1000)
- `UpdateTaskRequest` — same + `completed`
- `TaskResponse` — all fields including timestamps

**Phase 4 — Exception handling ✅**
- `TaskNotFoundException` — extends `RuntimeException`, receives `Long id`
- `ErrorResponse` — `status`, `error`, `message` (getters only, immutable)
- `GlobalExceptionHandler` — `@RestControllerAdvice`, handles `TaskNotFoundException` (404), `MethodArgumentNotValidException` (400), and `Exception` (500)

**Phase 5 — Service ✅**
- `TaskService.java` — `@Service`, `@Transactional`, constructor injection of `TaskRepository`
- `toResponse(Task)` — private helper, entity → DTO
- `taskFinderById(Long id)` — private helper, findById + orElseThrow
- `getAllTasks()`, `getTaskById()`, `createTask()`, `updateTask()`, `deleteTask()`, `toggleComplete()` — all done

**Phase 6 — Controller ✅**
- `TaskController.java` — `@RestController`, `@RequestMapping("/api/tasks")`, constructor injection of `TaskService`
- All 6 endpoints implemented with correct HTTP methods and status codes (201 for create, 204 for delete)
- `@Valid` added on `@RequestBody` for `createTask` and `updateTask`

**Phase 7 — Tests ✅**
- `pom.xml` — `spring-boot-starter-test` + H2 configured
- `TaskServiceTest.java` — unit tests with Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`)
  - `getAllTasks()` ✅ — mocks `findAll()`, asserts list size and title
  - `getTaskById()` ✅ — mocks `findById()` with `Optional.of(task)`, asserts id and title
  - `createTask()` ✅ — mocks `save(any(Task.class))`, asserts title and description
  - `updateTask()` ✅ — mocks `findById()`, applies `UpdateTaskRequest`, asserts all updated fields
  - `deleteTask()` ✅ — mocks `findById()`, verifies `repository.delete(task)` was called
  - `toggleComplete()` ✅ — mocks `findById()`, asserts `completed` flipped to `true`
- `TaskControllerTest.java` ✅ — `standaloneSetup` with `GlobalExceptionHandler` and `LocalValidatorFactoryBean`
  - All 6 endpoints covered: happy path + 404 (TaskNotFoundException) + 400 (blank/long title)

**Phase 8 — Environment variables ✅**
- `application.properties` — uses `${VAR:default}` syntax for `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`; `spring.config.import=optional:file:.env[.properties]` carrega `.env` em dev
- `docker-compose.yml` — usa `${DB_NAME}`, `${DB_USERNAME}`, `${DB_PASSWORD}` do `.env`
- `.env` — criado com valores reais para dev local (não commitado)
- `.env.example` — template para outros devs
- `.gitignore` — `.env` excluído

**Phase 9 — CI/CD (planned)**
- GitHub Actions pipeline: build and test on every push
