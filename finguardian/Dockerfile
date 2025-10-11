# ===== Stage 1: Build the application =====
FROM eclipse-temurin:19-jdk AS builder
WORKDIR /app

# Copy pom and source code
COPY pom.xml .
COPY src ./src

# Use wrapper if present, otherwise system Maven
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

# ===== Stage 2: Run the application =====
FROM eclipse-temurin:19-jre
WORKDIR /app

# Copy only the jar file from the builder stage
COPY --from=builder /app/target/finguardian-0.0.1-SNAPSHOT.jar finguardian.jar

EXPOSE 9090
ENTRYPOINT ["java", "-jar", "finguardian.jar"]

