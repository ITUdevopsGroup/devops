FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21
WORKDIR /app

COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar /app/app.jar

# seed DB (only used if volume is empty)
COPY minitwit.db /app/seed/minitwit.db

# entrypoint that seeds then starts java
COPY seed-db.sh /app/seed-db.sh

RUN chmod +x /app/seed-db.sh
# delete this comment
EXPOSE 5001
ENTRYPOINT ["/app/seed-db.sh"]
