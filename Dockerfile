FROM gradle:8.14-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/mixxed-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 7080
ENV SERVER_PORT=7080
ENTRYPOINT ["java", "-Xmx512m", "-jar", "/app/app.jar"]
