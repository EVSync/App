
    FROM maven:3.9.8-eclipse-temurin-21 AS builder
    WORKDIR /workspace
    COPY pom.xml ./
    COPY src ./src
    RUN mvn clean package -DskipTests


    FROM openjdk:21-jdk-slim
    WORKDIR /app
    COPY --from=builder /workspace/target/*.jar app.jar

    EXPOSE 8080
    ENTRYPOINT ["java", "-jar", "app.jar"]
