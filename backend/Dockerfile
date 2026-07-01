FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package dependency:copy-dependencies -DincludeScope=runtime

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/classes ./classes
COPY --from=build /app/target/dependency ./lib

EXPOSE 8080
ENTRYPOINT ["java", "-Djava.awt.headless=true", "-cp", "classes:lib/*", "com.example.backend.BackendApplication"]
