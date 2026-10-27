# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
# Copy pom first so dependency layer is cached unless pom changes
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:17-jre-alpine
# Non-root user
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=builder /build/target/calculator.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
