# Etapa 1: Compilar el proyecto con Gradle 8.11+ y Java 21
FROM gradle:8.11-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

# Etapa 2: Ejecutar el servidor liviano con Java 21
FROM eclipse-temurin:21-jre-alpine
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*-all.jar /app/ktor-server.jar
ENTRYPOINT ["java", "-jar", "/app/ktor-server.jar"]