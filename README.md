## Gist API

A lightweight HTTP API that returns a user's publicly available GitHub Gists.

### Prerequisites

- Docker (required)
- Java 21+ (optional, only needed to run tests locally)

### Quick Start (Docker)

#### 1. Build the image

```bash
docker build -t gist-api .
```

#### 2. Run the container

```bash
docker run -p 8080:8080 gist-api
```

#### 3. Test it

```bash
curl http://localhost:8080/octocat
```

### Run Locally (requires Java 21)

#### 1. Build the fat jar

```bash
./gradlew fatJar
```

#### 2. Run the server

```bash
java -jar build/libs/gist-api-1.0.0-all.jar
```

#### 3. Test it (in another terminal)

```bash
curl http://localhost:8080/octocat
```

#### 4. Stop the server

Press `Ctrl+C`.

### Run Tests

```bash
./gradlew test
```

### API

| Method | Path | Description |
|--------|------|-------------|
| GET | `/<username>` | Returns JSON array of the user's public gists |

Example response:

```json
[
  {
    "id": "aa5a315d61ae9438b18d",
    "description": "Hello World",
    "htmlUrl": "https://gist.github.com/aa5a315d61ae9438b18d",
    "files": ["ring.erl"]
  }
]
```

### Design Decisions

- No framework: Uses JDK built-in `HttpServer` and `HttpClient`. The problem is small enough that Spring/Micronaut would add startup overhead and dependency weight without benefit.
- Java 21 Records: `Gist` is an immutable record, no boilerplate getters/setters, built-in equals/hashCode/toString.
- Interface-based DI: `GistClient` interface allows the handler to be unit-tested with a fast stub, independent of network calls.
- Two test levels: Fast unit tests (stubbed client) for handler logic + integration test against real GitHub API using `octocat`.
- Flat package structure: Single `com.gist` package, YAGNI for this scope, easy to navigate.
- Multi-stage Docker build: Keeps final image small (JRE only, no build tools).

### Assumptions

- GitHub's public Gists endpoint does not require authentication for the volumes expected.
- The API returns an empty JSON array `[]` for unknown usernames (GitHub returns 404, we translate to empty list).
- The default GitHub page size (30) is sufficient for the exercise.

### Future Changes

- Pagination: Pass through `?page=N&per_page=N` to GitHub's API for users with many gists.
- Caching: In-memory TTL cache to reduce GitHub API calls and improve response times.
- Rate limit handling: Graceful 429 responses when GitHub's unauthenticated limit (60 req/hour) is reached.
- Health endpoint: Add `GET /health` for container orchestration readiness/liveness probes.
