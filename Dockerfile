FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
COPY gradle ./gradle
COPY gradlew ./
RUN chmod +x gradlew && ./gradlew fatJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/gist-api-1.0.0-all.jar gist-api.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "gist-api.jar"]
