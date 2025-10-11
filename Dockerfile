# ----------------------------------------------------------------------------------
# Stage 1: Build the Application
# Uses a Maven image which contains the necessary tools (JDK, Maven) to compile.
# ----------------------------------------------------------------------------------
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the build configuration (pom.xml) and source code
COPY pom.xml .
COPY src src

# Build the application and skip tests for faster container build
# The resulting JAR is placed in /app/target/
RUN mvn clean package -DskipTests

# ----------------------------------------------------------------------------------
# Stage 2: Create the Final, Lightweight Runtime Image
# Uses a much smaller JRE image for production deployment.
# ----------------------------------------------------------------------------------
FROM eclipse-temurin:19-jre
WORKDIR /app

# Copy the JAR from the previous stage (named 'build')
# The JAR file name is typically derived from your pom.xml, e.g., artifactId-version.jar
COPY --from=build /app/target/finguardian-0.0.1-SNAPSHOT.jar finguardian.jar

EXPOSE 9090
ENTRYPOINT ["java","-jar","finguardian.jar"]