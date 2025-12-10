# Build stage
FROM maven:3.6.3-jdk-8 AS backend-build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

# Run stage
FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=build /app/target/adyen-dropin-springboot-0.0.1-SNAPSHOT.jar app.jar
ENV ADYEN_ENVIRONMENT=test
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
