FROM gradle:9.2.1-jdk17-alpine AS BUILD
WORKDIR /app
COPY . .
RUN gradle build -x test --no-daemon
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar  /app/usuario.jar
EXPOSE 8081

CMD ["java", "-jar", "/app/usuario.jar"]

