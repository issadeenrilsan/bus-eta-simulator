# Build stage: compiles the project and bundles MySQL driver into one runnable jar
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# Run stage: small image that only runs the jar
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/bus-eta.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
