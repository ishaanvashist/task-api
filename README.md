# Task API

![CI](https://github.com/ishaanvashist/task-api/actions/workflows/ci.yml/badge.svg)

A task management REST API built with Spring Boot, featuring layered architecture, input validation, centralized error handling, and full Docker/Compose support for containerized deployment.

**Note:** Task data is now stored in a PostgreSQL database and survives application restarts (previously stored in-memory only).

## Tech Stack

- Java 21
- Spring Boot 4.1
- Maven
- Docker + Docker Compose
- PostgreSQL
- Spring Data JPA

## Endpoints

| Method | Path                      | Description                          | Success Status |
|--------|----------------------------|---------------------------------------|-----------------|
| GET    | `/api/tasks`               | Get all tasks                         | 200 |
| GET    | `/api/tasks/{id}`          | Get a single task by ID               | 200 |
| POST   | `/api/tasks`                | Create a new task                     | 201 |
| PUT    | `/api/tasks/{id}`          | Update an existing task               | 200 |
| DELETE | `/api/tasks/{id}`          | Delete a task                         | 204 |
| GET    | `/actuator/health`         | Health check (returns app status)     | 200 |
| GET    | `/api/tasks?title={title}` | Search tasks by exact title           | 200 |
| POST   | `/api/categories`          | Create a new category                 | 201 |
| GET    | `/api/categories`          | Get all categories with task counts   | 200 |


**Validation:** `title` is required, max 200 characters. `description` max 1000 characters.

All task fields: `id`, `title`, `description`, `completed`.

## How to Run

### Locally (Maven)
```
./mvnw spring-boot:run
```
App runs on `localhost:8080` by default (or whatever `server.port` is set to in `application.properties`).

### With Docker
```
docker build -t task-api:v1 .
docker run --rm -p 8080:8081 task-api:v1
```
Note: the app listens on port 8081 inside the container; the command above maps host port 8080 to it.

## Running with Docker Compose

docker compose up

This now starts two containers:
- `task-api` on port 8080 — the Spring Boot application
- `postgres` on port 5432 — the PostgreSQL database (data persisted via a Docker volume, so it survives container restarts)

## Architecture

The project follows a layered structure:

- **Controller** (`TaskController`) — handles HTTP requests only: receives input, calls the service, returns a response. No business logic lives here.
- **Service** (`TaskService`) — holds the actual business logic (creating, updating, fetching, deleting tasks).
- **Repository** (`TaskRepository`) — handles data storage. Currently in-memory; will move to PostgreSQL in Week 3.
- **Exception handling** — a custom `TaskNotFoundException` paired with a `GlobalExceptionHandler` turns errors into clean, consistent HTTP responses (404 for missing tasks, 400 for validation failures), instead of leaking raw stack traces.
- **Validation** — request bodies are validated using `@Valid` (Jakarta Validation) before reaching the service layer.
- **Configuration** — settings like the welcome message are externalized into `application.properties` rather than hardcoded, using `@Value`.

This separation keeps each layer responsible for exactly one thing, and makes the code easier to test and extend (e.g. swapping the in-memory repository for a database-backed one later without touching the controller or service).

## Concepts Learned

### Request Lifecycle
A request travels: Browser → `DispatcherServlet` (Spring's front controller, routes incoming requests) → the matching `@RestController` method → business logic in the service layer → response serialized back to JSON.

### Bean
A Bean is a single Java object that Spring creates and manages at runtime — instead of the class creating its own dependencies with `new`, Spring builds it once and hands it out wherever it's needed (e.g. `TaskService`, `TaskController`).

### Application Context
The Application Context is where Spring keeps all the Beans it has built — a container/storage that holds every managed object. When a class needs a Bean, Spring retrieves it from the Application Context rather than creating a new one on the spot.

### Singleton vs Prototype Scope
- **Singleton** (the default): Spring creates exactly one instance of a Bean and shares that same object everywhere it's needed.
- **Prototype**: Spring creates a brand new instance every single time the Bean is requested, with no sharing between callers.

Most Beans in this project (like `TaskService`) are singletons — safe because they don't hold per-request state, just logic and a reference to the repository.

### ORM (Object-Relational Mapping)

An ORM bridges two different worlds: Java thinks in objects (classes, fields, instances), while databases think in tables, rows, and columns. Without an ORM, you'd write raw SQL for every read and write, and manually convert between database rows and Java objects each time.

**What it solves:** the ORM handles that translation automatically. You save a Java object, it generates the SQL to insert a row. You fetch from the database, it hands you back a Java object. Less boilerplate, fewer places to make mistakes.

**What it introduces:** the actual SQL is hidden, so it's easy to accidentally write code that generates inefficient queries (like fetching an entire table when only one row was needed). You now have to learn both the database *and* the ORM's rules for how it maps things. Debugging can also be harder, since the SQL that runs was generated by the ORM, not written by you.

In this project, JPA (the specification) and Hibernate (the actual implementation) act as the ORM layer, wrapped further by Spring Data JPA for even less boilerplate.

### Database Indexes

Added an index on the `title` column to speed up searches. Ran `EXPLAIN ANALYZE` to see how Postgres actually searches:

- Without and even with the index present, Postgres used a **Sequential Scan** (checking every row) instead of the index, because the table is small (6 rows) — scanning directly is faster than using the index at this size.
- This showed that indexes aren't automatically used just because they exist — Postgres estimates which approach is actually faster for the current amount of data, and chooses accordingly.
- On a much larger table, the same query would likely switch to an **Index Scan** instead.

### Testcontainers

Integration tests now use Testcontainers to spin up a real, temporary PostgreSQL database automatically — no manual setup needed, and it works the same way locally and in CI.


### Category Relationship

Tasks can optionally belong to a Category (many-to-one, via a `category_id` foreign key). POST /api/categories creates a category; tasks can be created with a `category: { "id": ... }` field to link them. Fetching a task returns its full connected category (id + name) via a join, not just the raw id.

### The N+1 Problem

Fetching all categories along with their task counts naively caused 1 extra query per category on top of the initial list query (N+1) — confirmed via `spring.jpa.show-sql=true` logs, showing 2 separate queries for just 1 category. Fixed using a fetch join — a custom repository query with `LEFT JOIN FETCH` — combining categories and their tasks into a single query. After the fix, the same result came from just 1 query total, regardless of how many categories exist.


## Live Deployment

Deployed on Render: https://task-api-tndd.onrender.com/api/tasks

## Running Tests

./mvnw test