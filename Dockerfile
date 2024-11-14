FROM openjdk:21-jdk-slim
MAINTAINER dev.amir
COPY target/trip-matcher-0.0.1-SNAPSHOT.jar trip-matcher-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/trip-matcher-0.0.1-SNAPSHOT.jar"]
EXPOSE 8080