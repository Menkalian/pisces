FROM openjdk:17

ARG JAR_FILE=build/libs/*-boot.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
HEALTHCHECK --start-period=10s CMD curl -f http://localhost:8080/actuator/health || exit 1
