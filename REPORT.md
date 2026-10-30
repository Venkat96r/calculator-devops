# CSE 816 Software Production Engineering: Mini Project Report
## Calculator REST API with End-to-End DevOps CI/CD Pipeline

**Student Name:** Venkat  
**Email:** ramivenkat16@gmail.com  
**Course:** CSE 816 SPE  
**Repository:** `calculator-devops`  

---

## 1. Project Overview & Architecture

This project implements an enterprise-grade arithmetic REST API using **Java 17**, **Spring Boot 3.3.x**, and **Spring Boot Actuator**, fully automated via a Declarative **Jenkins CI/CD Pipeline**, **Docker**, and **Docker Compose**. The application is containerized with an unprivileged non-root user and orchestrated with two replicated instances behind an **Nginx** reverse proxy load balancer on port `8081`.

```
[Developer] --(git push)--> [GitHub Repository] --(Webhook / SCM Poll)--> [Jenkins Pipeline]
                                                                                |
  +-----------------------------------------------------------------------------+
  |
  +--> [Stage 1: Checkout] ---> [Stage 2: Maven Build] ---> [Stage 3: JUnit Test]
  |
  +--> [Stage 4: Docker Build] ---> [Stage 5: Docker Hub Push]
  |
  +--> [Stage 6: Zero-Downtime Deploy (Docker Compose)]
             |
             +---> [Nginx Reverse Proxy :8081]
                         |
                         +---> [Spring Boot App Replica 1 :8080] (Healthy)
                         +---> [Spring Boot App Replica 2 :8080] (Healthy)
```

---

## 2. Pipeline Stages Detailed Breakdown

1. **Checkout Stage**:
   Clones the repository from GitHub SCM into the Jenkins workspace cleanly. Utilizes the Jenkins Git plugin to track commits and branches.
2. **Build Stage (`mvn -B clean package -DskipTests`)**:
   Compiles Java 17 source code, packages the Spring Boot application into a single executable FAT JAR, and writes it to `target/calculator.jar`.
3. **Test Stage (`mvn -B test`)**:
   Executes both unit tests (`CalculatorServiceTest`) and MockMvc integration tests (`CalculatorControllerIT`). The `always` post block records test results via the JUnit plugin (`target/surefire-reports/*.xml`), generating the test trend graph in Jenkins.
4. **Docker Build Stage**:
   Executes a multi-stage Docker build. Stage 1 caches dependencies using `mvn dependency:go-offline` before compilation. Stage 2 copies the compiled JAR into a minimal `eclipse-temurin:17-jre-alpine` runtime running under an unprivileged user (`app`, `uid=100`). The image is tagged with both `${BUILD_NUMBER}` and `latest`.
5. **Push Stage**:
   Uses the Jenkins `withCredentials` block to securely fetch Docker Hub credentials without exposing secrets. Authenticates via `docker login --password-stdin` and pushes `${IMAGE_NAME}:${BUILD_NUMBER}` and `${IMAGE_NAME}:latest` to Docker Hub.
6. **Deploy Stage**:
   Executes `docker compose pull` followed by `docker compose up -d --remove-orphans`. Polls the Actuator endpoint `http://localhost:8081/actuator/health` every 5 seconds (up to 20 attempts) until `{"status":"UP"}` is verified.
7. **Post Actions (Email Notification)**:
   Sends an automated status email via Jenkins Mailer on build success or failure with links to the build console and Docker image tag.

---

## 3. Real Challenges Faced and Engineering Solutions

During the setup and verification of the pipeline, several real-world DevOps challenges were addressed:

1. **Port 8080 Clash on Host**:
   - *Problem*: Port 8080 is often claimed by Jenkins, IDE background services, or application servers, causing port binding errors (`listen tcp 0.0.0.0:8080: bind: address already in use`).
   - *Solution*: Kept Spring Boot application replicas inside the Docker bridge network (`calc-net`) on internal port 8080, and bound the Nginx reverse proxy to external port `8081:80`.
2. **Stale DNS Resolution in Nginx with Dynamic Replicas**:
   - *Problem*: When Docker Compose recreates or scales containers during continuous deployment, container internal IP addresses change. Standard Nginx configurations cache DNS once at startup, leading to `502 Bad Gateway`.
   - *Solution*: Configured Nginx with Docker's embedded DNS server (`resolver 127.0.0.11 valid=10s;`) and used a variable upstream (`set $upstream http://app:8080; proxy_pass $upstream;`) so IP addresses are dynamically re-resolved every 10 seconds.
3. **Surefire Integration Test Discovery**:
   - *Problem*: Maven Surefire by default only executes files matching `**/Test*.java`, `**/*Test.java`, or `**/*TestCase.java`, skipping `*IT.java` integration tests during `mvn test`.
   - *Solution*: Explicitly configured `maven-surefire-plugin` in `pom.xml` to include both `**/*Test.java` and `**/*IT.java`, ensuring all 12 unit and integration tests run and output XML reports for Jenkins.
4. **Container Security & Least Privilege**:
   - *Problem*: Running Java containers as `root` introduces significant container breakout vulnerabilities.
   - *Solution*: Added Alpine system group and user (`addgroup -S app && adduser -S app -G app`) and switched to `USER app`, verified running as `uid=100(app)`.
5. **Idempotence of Deployment**:
   - *Problem*: Repeated executions of `docker compose up` can cause orphaned containers, recreation delays, or conflicts if project names fluctuate.
   - *Solution*: Explicitly pinned project name (`name: calculator`) and used `--remove-orphans`, ensuring identical subsequent deployments produce no side effects.

---

## 4. API Verification & Testing Results

| Endpoint | Test Case | HTTP Status | Response Payload |
|---|---|---|---|
| `GET /api/v1/add?a=2&b=3` | Addition | 200 OK | `{"operation":"add","a":2,"b":3,"result":5}` |
| `GET /api/v1/multiply?a=4&b=5` | Multiplication | 200 OK | `{"operation":"multiply","a":4,"b":5,"result":20}` |
| `GET /actuator/health` | Health Check | 200 OK | `{"status":"UP","groups":["liveness","readiness"]}` |
| `GET /api/v1/divide?a=1&b=0` | Divide by zero | 400 Bad Request | `{"error":"DIVISION_BY_ZERO","message":"Division by zero is not allowed","status":400}` |
| `GET /api/v1/add?a=2` | Missing param | 400 Bad Request | `{"error":"MISSING_PARAMETER","message":"Required request parameter 'b'...","status":400}` |
| `GET /api/v1/add?a=abc&b=1` | Invalid type | 400 Bad Request | `{"error":"INVALID_PARAMETER","message":"Failed to convert value of type...","status":400}` |

---

## 5. Required Screenshots Checklist for Final Submission

- [ ] **Screenshot 1**: Jenkins Stage View showing all 6 green pipeline stages (`Checkout` to `Deploy`).
- [ ] **Screenshot 2**: JUnit test result report showing 12 passing tests and zero failures.
- [ ] **Screenshot 3**: Email notification from Jenkins with build success details.
- [ ] **Screenshot 4**: Output of `docker compose ps` showing Nginx and 2 healthy app replicas.
- [ ] **Screenshot 5**: Terminal curls showing 200 success response, 400 divide-by-zero, and 400 missing parameter.
- [ ] **Screenshot 6**: Docker Hub web console displaying image tags (`${BUILD_NUMBER}` and `latest`).
- [ ] **Screenshot 7**: GitHub commit history and merged PRs graph (`git log --graph --oneline`).

