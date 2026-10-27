# Calculator REST API with End-to-End CI/CD Pipeline

A production-grade, highly available Calculator REST API built with Java 17 and Spring Boot 3.3.x, engineered with an automated end-to-end DevOps CI/CD pipeline. The pipeline automates automated checkout, testing, multi-stage Docker containerization running under an unprivileged user, publishing to Docker Hub, and zero-downtime blue/green deployment behind an Nginx reverse proxy load balancer with health checking.

---

## Architecture

```
                  +---------------------------------------------------+
                  |                  Developer Workstation            |
                  +-------------------------+-------------------------+
                                            | git push
                                            v
+---------------------------------------------------------------------------------------+
|                                    GitHub Repository                                  |
+-------------------------------------------+-------------------------------------------+
                                            | Webhook / Polling
                                            v
+---------------------------------------------------------------------------------------+
|                                   Jenkins CI/CD Pipeline                              |
|                                                                                       |
|  [Checkout] -> [Build JAR] -> [JUnit Tests] -> [Docker Build] -> [Push to Registry]   |
|                                                                          |            |
+--------------------------------------------------------------------------+------------+
                                                                           |
                                      +------------------------------------+
                                      | docker push
                                      v
                        +---------------------------+
                        |         Docker Hub        |
                        |   <DOCKERHUB_USER>/       |
                        |   calculator-api:tag      |
                        +-------------+-------------+
                                      |
                                      | docker compose pull
                                      v
+---------------------------------------------------------------------------------------+
|                             Docker Compose Deployment Host                            |
|                                                                                       |
|                         +-----------------------------------+                         |
|                         |       Nginx Reverse Proxy         |                         |
|                         |           (Port 8081)             |                         |
|                         +--------+-----------------+--------+                         |
|                                  |                 |                                  |
|                 +----------------+                 +----------------+                 |
|                 v                                                   v                 |
|   +---------------------------+                       +---------------------------+   |
|   |   Spring Boot App (Rep 1) |                       |   Spring Boot App (Rep 2) |   |
|   |         Port 8080         |                       |         Port 8080         |   |
|   |     Status: Healthy       |                       |     Status: Healthy       |   |
|   +---------------------------+                       +---------------------------+   |
+---------------------------------------------------------------------------------------+
```

---

## Prerequisites

Before running the project locally or setting up the pipeline, ensure the following tools are installed:
- **Java Development Kit (JDK)**: Version 17
- **Apache Maven**: Version 3.8+ or 3.9+
- **Docker**: Version 20.10+
- **Docker Compose**: Version v2.0+
- **Git**: Version 2.30+

---

## Build and Test

### Compile and Package JAR
```bash
mvn clean package -DskipTests
```
The resulting executable JAR will be located at `target/calculator.jar`.

### Run Unit and Integration Tests
```bash
mvn clean test
```
All 12 test cases across `CalculatorServiceTest` (unit tests) and `CalculatorControllerIT` (MockMvc integration tests) will execute, generating XML test reports in `target/surefire-reports/TEST-*.xml`.

---

## Run with Docker

### 1. Build Multi-Stage Docker Image
The `Dockerfile` employs a multi-stage build pattern:
- **Stage 1 (builder)**: Pre-caches dependencies using `mvn dependency:go-offline` before building the JAR.
- **Stage 2 (runtime)**: Uses an ultra-lightweight `eclipse-temurin:17-jre-alpine` runtime running under an unprivileged non-root user (`app`, `uid=100`).

```bash
docker build -t calculator-api:local .
```

Verify that the image runs as a non-root user:
```bash
docker run --rm --entrypoint id calculator-api:local
# Output: uid=100(app) gid=101(app) groups=101(app)
```

### 2. Run Container Standalone
```bash
docker run -d --name calculator-api -p 8080:8080 calculator-api:local
```
Verify health:
```bash
curl http://localhost:8080/actuator/health
```
Stop and remove container:
```bash
docker rm -f calculator-api
```

---

## Run Full Stack (Docker Compose + Nginx)

The full production stack runs two replicated instances of the Spring Boot application behind an Nginx reverse proxy load balancer on port `8081` (avoiding conflicts with standard Jenkins installations on port 8080).

### Start the Stack
```bash
docker compose up -d --build
```

### Check Service Status
```bash
docker compose ps
```
Both `app` replicas report `(healthy)` via the Actuator healthcheck probe before Nginx starts routing traffic.

### Idempotence and Redeploy
The stack defines a fixed project name `calculator` and uses dynamic DNS re-resolution in Nginx (`resolver 127.0.0.11 valid=10s`), allowing repeated execution without disruption:
```bash
docker compose up -d
```

### Stop the Stack
```bash
docker compose down
```

---

## API Reference

Base URL (via Nginx): `http://localhost:8081`  
Base URL (direct container): `http://localhost:8080`

### Endpoints

| Method | Endpoint | Query Parameters | Description |
|---|---|---|---|
| `GET` | `/api/v1/add` | `a` (BigDecimal), `b` (BigDecimal) | Adds two numbers |
| `GET` | `/api/v1/subtract` | `a` (BigDecimal), `b` (BigDecimal) | Subtracts `b` from `a` |
| `GET` | `/api/v1/multiply` | `a` (BigDecimal), `b` (BigDecimal) | Multiplies two numbers |
| `GET` | `/api/v1/divide` | `a` (BigDecimal), `b` (BigDecimal) | Divides `a` by `b` |
| `GET` | `/actuator/health` | _None_ | Application healthcheck |

### Example Requests and Responses

#### 1. Addition
```bash
curl "http://localhost:8081/api/v1/add?a=2&b=3"
```
**Response (200 OK):**
```json
{
  "operation": "add",
  "a": 2,
  "b": 3,
  "result": 5
}
```

#### 2. Subtraction
```bash
curl "http://localhost:8081/api/v1/subtract?a=10&b=4"
```
**Response (200 OK):**
```json
{
  "operation": "subtract",
  "a": 10,
  "b": 4,
  "result": 6
}
```

#### 3. Multiplication
```bash
curl "http://localhost:8081/api/v1/multiply?a=4&b=5"
```
**Response (200 OK):**
```json
{
  "operation": "multiply",
  "a": 4,
  "b": 5,
  "result": 20
}
```

#### 4. Division
```bash
curl "http://localhost:8081/api/v1/divide?a=1&b=4"
```
**Response (200 OK):**
```json
{
  "operation": "divide",
  "a": 1,
  "b": 4,
  "result": 0.25
}
```

#### 5. Health Check
```bash
curl "http://localhost:8081/actuator/health"
```
**Response (200 OK):**
```json
{
  "status": "UP",
  "groups": [
    "liveness",
    "readiness"
  ]
}
```

### Error Responses (HTTP 400 Bad Request)

The API handles erroneous and malformed inputs with structured JSON error responses:

#### Division by Zero:
```bash
curl "http://localhost:8081/api/v1/divide?a=1&b=0"
```
**Response:**
```json
{
  "error": "DIVISION_BY_ZERO",
  "message": "Division by zero is not allowed",
  "status": 400
}
```

#### Missing Parameter:
```bash
curl "http://localhost:8081/api/v1/add?a=2"
```
**Response:**
```json
{
  "error": "MISSING_PARAMETER",
  "message": "Required request parameter 'b' for method parameter type BigDecimal is not present",
  "status": 400
}
```

#### Invalid Parameter Type (Non-numeric):
```bash
curl "http://localhost:8081/api/v1/add?a=abc&b=1"
```
**Response:**
```json
{
  "error": "INVALID_PARAMETER",
  "message": "Failed to convert value of type 'java.lang.String' to required type 'java.math.BigDecimal'...",
  "status": 400
}
```

---

## CI/CD Pipeline Overview

The declarative Jenkins pipeline (`Jenkinsfile`) automates build, test, packaging, and zero-downtime deployment:

1. **Checkout**: Checks out source code from Git SCM.
2. **Build**: Compiles application and builds JAR (`mvn -B clean package -DskipTests`).
3. **Test**: Executes unit and integration test suite (`mvn -B test`) and records surefire test reports (`junit 'target/surefire-reports/*.xml'`).
4. **Docker Build**: Packages application into image tagged with Jenkins `${BUILD_NUMBER}` and `latest`.
5. **Push**: Authenticates via Jenkins credential `dockerhub-creds` and pushes image tags to Docker Hub.
6. **Deploy**: Pulls newly built image, executes `docker compose up -d --remove-orphans`, and polls Actuator healthcheck until `UP`.
7. **Post-Build Actions**: Automatically sends an email notification to the configured recipient with build status and direct links.

### Docker-in-Docker / Jenkins Agent Socket Mount
> **Important Note:** If the Jenkins agent executes inside a Docker container, the host Docker socket must be mounted into the container (`-v /var/run/docker.sock:/var/run/docker.sock`), and the Jenkins container user must have permissions to execute the Docker CLI and Docker Compose (`sudo usermod -aG docker jenkins`).

---

## Docker Hub Registry

Public repository:  
`https://hub.docker.com/r/<DOCKERHUB_USER>/calculator-api`
