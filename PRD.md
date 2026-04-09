# Todo REST API — Product Requirements Document

## 1. Overview

A REST API for managing a personal todo list. Built with Spring Boot as a study project to practice Java web development end-to-end: JPA entities, repositories, services, controllers, DTOs, validation, and exception handling.

**Done** means: all 6 CRUD endpoints work, data persists to PostgreSQL, validation errors return structured responses, and the happy path has test coverage.

---

## 2. Domain Model — Task entity

File: `src/main/java/com/ricardo/todo/model/Task.java`

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | `Long` | PK, auto-generated |
| `title` | `String` | Not null, not blank, max 255 chars |
| `description` | `String` | Optional, max 1000 chars |
| `completed` | `boolean` | Default `false` |
| `createdAt` | `LocalDateTime` | Set on insert, not updatable |
| `updatedAt` | `LocalDateTime` | Set on insert and update |

---

## 3. API Endpoints

Base path: `/api/tasks`

| Method | Path | Description | Request body | Response |
|--------|------|-------------|--------------|----------|
| GET | `/api/tasks` | List all tasks | — | `200 List<TaskResponse>` |
| POST | `/api/tasks` | Create a task | `CreateTaskRequest` | `201 TaskResponse` |
| GET | `/api/tasks/{id}` | Get task by id | — | `200 TaskResponse` |
| PUT | `/api/tasks/{id}` | Replace a task | `UpdateTaskRequest` | `200 TaskResponse` |
| PATCH | `/api/tasks/{id}/complete` | Toggle completed | — | `200 TaskResponse` |
| DELETE | `/api/tasks/{id}` | Delete a task | — | `204 No Content` |

---

## 4. DTOs

**`dto/request/CreateTaskRequest`**
- `title` — required, not blank
- `description` — optional

**`dto/request/UpdateTaskRequest`**
- `title` — required, not blank
- `description` — optional

**`dto/response/TaskResponse`**
- `id`, `title`, `description`, `completed`, `createdAt`, `updatedAt`

> **Why DTOs?** They decouple the API surface from the database entity. If you change the entity (add a column, rename a field), the API contract does not automatically change. DTOs also let you control exactly what data you expose — e.g., never accidentally leaking internal fields.

---

## 5. Error responses

All 4xx/5xx responses use this shape:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Task not found with id: 42"
}
```

Handled globally in `exception/GlobalExceptionHandler` via `@RestControllerAdvice`.

Exceptions to handle:
- `TaskNotFoundException` → `404 Not Found`
- Bean validation failures (`@Valid`) → `400 Bad Request` (list the field errors in `message`)

---

## 6. Implementation Phases

Work through these in order. Each phase builds on the previous one.

---

### Phase 0 — Infrastructure ✅

- [x] Define the PostgreSQL service in `docker-compose.yml` (image, port, environment vars for DB name/user/password)
- [x] Add datasource and JPA properties to `application.properties` (url, username, password, dialect, `ddl-auto`)
- [x] Fix the app name typo in `application.properties`: `"todon"` → `"todo"`
- [x] Run `docker compose up -d` and confirm the app starts with `./mvnw spring-boot:run`

> **Key concept — `spring.jpa.hibernate.ddl-auto`**: Setting this to `update` (or `create-drop` in dev) tells Hibernate to auto-create/update tables from your entity classes. Convenient for development; never use `create-drop` in production.

---

### Phase 1 — Domain model ✅

- [x] Add all fields to `Task.java`
- [x] Annotate the class with `@Entity`
- [x] Annotate `id` with `@Id` and `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- [x] Use `@Column` where you need to set `nullable`, `length`, or `updatable = false`
- [x] Use `@CreationTimestamp` on `createdAt` and `@UpdateTimestamp` on `updatedAt`
- [x] Add bean validation annotations: `@NotBlank`, `@Size`
- [x] Confirm Hibernate creates the `tasks` table on startup

> **Key concept — `@CreationTimestamp` / `@UpdateTimestamp`**: Hibernate annotations that automatically populate timestamps when an entity is first persisted or subsequently updated. No manual `LocalDateTime.now()` needed.

---

### Phase 2 — Repository ✅

- [x] Create `TaskRepository` in `repository/` extending `JpaRepository<Task, Long>`
- [x] No custom methods needed yet — `JpaRepository` gives you `findAll`, `findById`, `save`, `deleteById` for free

> **Key concept — Spring Data JPA**: `JpaRepository` generates the SQL at runtime from the method signatures. You write the interface; Spring writes the implementation. `findById` returns `Optional<Task>`, which you'll use to throw `TaskNotFoundException` when empty.

---

### Phase 3 — DTOs ✅

- [x] Create `CreateTaskRequest` with validation annotations (`@NotBlank`, `@Size`)
- [x] Create `UpdateTaskRequest` (same fields, same constraints)
- [x] Create `TaskResponse` with all output fields

> **Key concept — records vs classes**: Java records (`record TaskResponse(...)`) are a clean fit for DTOs since they are immutable value holders. Plain classes with getters work too — pick whichever you want to practice.

---

### Phase 4 — Exception handling ✅

- [x] Create `TaskNotFoundException extends RuntimeException` in `exception/`
- [x] Create an error response DTO (e.g., `ErrorResponse`) with `status`, `error`, `message`
- [x] Create `GlobalExceptionHandler` annotated with `@RestControllerAdvice`
- [x] Add handler method for `TaskNotFoundException` (404) and generic `Exception` (500)
- [x] Add handler for `MethodArgumentNotValidException` (400)

> **Key concept — `@RestControllerAdvice`**: A global interceptor for exceptions thrown by any controller. Without it, Spring returns its own default error body. With it, you control exactly what the client receives.

---

### Phase 5 — Service ✅

- [x] Create `TaskService` in `service/` annotated with `@Service`
- [x] Inject `TaskRepository` via constructor injection
- [x] `getAllTasks()`
- [x] `getTaskById(Long id)`
- [x] `createTask(CreateTaskRequest)`
- [x] `updateTask(Long id, UpdateTaskRequest)`
- [x] `toggleComplete(Long id)`
- [x] `deleteTask(Long id)`
- [x] Map between `Task` entities and DTOs inside this layer (toResponse helper)
- [x] Throw `TaskNotFoundException` from `findById` when the task does not exist

> **Key concept — `@Transactional`**: Annotating a service method with `@Transactional` wraps it in a database transaction. If an exception is thrown, the transaction rolls back automatically. Read-only operations can use `@Transactional(readOnly = true)` for a small performance hint.

---

### Phase 6 — Controller ✅

- [x] Create `TaskController` in `controller/` annotated with `@RestController` and `@RequestMapping("/api/tasks")`
- [x] Inject `TaskService` via constructor injection
- [x] Implement all 6 endpoints (see Section 3)
- [x] Annotate request body parameters with `@Valid` to trigger bean validation
- [x] Return `ResponseEntity` with the correct status codes (201 for create, 204 for delete)

> **Key concept — `@Valid`**: Placing `@Valid` on a `@RequestBody` parameter tells Spring to run bean validation before the method body executes. Validation failures throw `MethodArgumentNotValidException`, which your `GlobalExceptionHandler` catches.

---

### Phase 7 — Tests ✅

- [x] Write unit tests for `TaskService` (mock the repository with Mockito)
- [x] Write integration tests for `TaskController` (`standaloneSetup` com `GlobalExceptionHandler` e `LocalValidatorFactoryBean`)
- [x] Cover at minimum: happy path for each endpoint, 404 for unknown id, 400 for invalid request body

---

### Phase 8 — Environment variables ✅

- [x] Replace hardcoded values in `application.properties` with `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`
- [x] Create `.env` file with the actual values for local dev
- [x] Add `.env` to `.gitignore` to avoid committing secrets
- [x] Configure Docker Compose to read from `.env` (it does this automatically)
- [x] Create `.env.example` as template for other devs

> **Key concept — externalizing config**: In corporate environments, secrets (passwords, URLs, API keys) must never be committed to source control. Spring Boot reads environment variables natively via `${VAR_NAME}` syntax. A `.env` file keeps local dev convenient without exposing secrets in git.

---

### Phase 9 — CI/CD ✅

- [x] Create `.github/workflows/ci.yaml`
- [x] Pipeline: trigger em push/PR para `main` — checkout → Java 21 (Temurin) → cache Maven → `./mvnw verify`

> **Key concept — `@WebMvcTest` vs `@SpringBootTest`**: `@WebMvcTest` loads only the web layer (fast, no DB needed). `@SpringBootTest` loads the full context (slower, but tests the full stack). Use `@WebMvcTest` + mocked service for controller unit tests; use `@SpringBootTest` for true integration tests.

---

## 7. Acceptance criteria

A phase is **done** when:

1. `./mvnw clean package` compiles without errors
2. `./mvnw spring-boot:run` starts without errors
3. The feature can be manually tested (curl or an HTTP client like Insomnia/Postman)
4. At least one test covers the happy path for that phase

---

## Quick reference — curl examples

```bash
# List all tasks
curl http://localhost:8080/api/tasks

# Create a task
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Learn Spring Boot", "description": "Build a todo API"}'

# Get by id
curl http://localhost:8080/api/tasks/1

# Update
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "Learn Spring Boot", "description": "Updated description"}'

# Toggle complete
curl -X PATCH http://localhost:8080/api/tasks/1/complete

# Delete
curl -X DELETE http://localhost:8080/api/tasks/1
```
