# Define a build argument for the JAR file name with a default value
ARG JAR_FILE=finguardian-0.0.1-SNAPSHOT.jar

FROM eclipse-temurin:19-jre
WORKDIR /app

# Use the ARG to specify the file to copy
# Note: This still requires your application to be built (mvn package) before docker build
COPY target/${JAR_FILE} finguardian.jar

EXPOSE 9090
ENTRYPOINT ["java","-jar","finguardian.jar"]