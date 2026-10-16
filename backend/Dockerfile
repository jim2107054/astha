# Multi-stage Dockerfile for Astha Backend Services
FROM eclipse-temurin:21-jre-alpine AS runtime

ARG SERVICE_NAME=api-gateway
ENV APP_FILE=${SERVICE_NAME}.jar
WORKDIR /app

# Add unprivileged system user for zero-root production security
RUN addgroup -S astha && adduser -S astha -G astha
USER astha:astha

COPY services/${SERVICE_NAME}/build/libs/${SERVICE_NAME}-*.jar app.jar

EXPOSE 8080 8081 8082 8083 8084 8085 8086 8087 8088 8089 8090
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
