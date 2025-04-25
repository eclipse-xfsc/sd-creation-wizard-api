FROM eclipse-temurin:21-jre-alpine
ARG JAR_FILE=target/creation-wizard-api.jar
COPY ${JAR_FILE} /app.jar
COPY shapes/ /shapes/
ENTRYPOINT ["java","-jar","/app.jar"]
