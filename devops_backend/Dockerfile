FROM maven:3.9.8-eclipse-temurin-21 AS build

COPY src /app/src
COPY minitwit.db /app/minitwit.db
COPY pom.xml /app

WORKDIR /app
RUN mvn clean install -U

FROM eclipse-temurin:21
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar /app/app.jar
COPY --from=build /app/target/classes/* /app/
COPY --from=build /app/minitwit.db /app/minitwit.db 


WORKDIR /app
RUN --mount=type=volume,src=/home/ubuntu/logs/devops_backend,dst=/app/logs/
RUN chmod -R 777 minitwit.db
EXPOSE 5001
ENTRYPOINT java -jar app.jar 1> >(tee /logs/info.log) 2> >(tee /logs/error.log 2>&2)
# docker build --tag 'minitwit_backend' .
# docker run -d -p 5001:5001 'minitwit_backend'
# docker exec -it <container id>  bash
