# Stage 1: Build the Application
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app

# The files are in the 'finguardian' subdirectory
# Copy the build configuration (pom.xml)
COPY finguardian/pom.xml finguardian/

# Copy the source code
COPY finguardian/src finguardian/src

# Navigate into the project folder for the build
WORKDIR /app/finguardian

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Create the Final, Lightweight Runtime Image
FROM eclipse-temurin:19-jre
WORKDIR /app

# Copy the JAR from the build stage (relative to the build stage's root /app)
COPY --from=build /app/finguardian/target/finguardian-0.0.1-SNAPSHOT.jar finguardian.jar

EXPOSE 9090
ENTRYPOINT ["java","-jar","finguardian.jar"]