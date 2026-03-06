FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21
WORKDIR /app

COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 5001
ENTRYPOINT ["java","-jar","/app/app.jar"]
