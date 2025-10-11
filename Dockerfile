FROM eclipse-temurin:19-jre
WORKDIR /app
COPY target/finguardian-0.0.1-SNAPSHOT.jar finguardian.jar
EXPOSE 9090
ENTRYPOINT ["java","-jar","finguardian.jar"]
