FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/target/e-cart-0.0.1-SNAPSHOT.jar /app/e-cart.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/e-cart.jar"]
