# syntax=docker/dockerfile:1
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src
ENV DB_HOST=host.docker.internal
ENV DB_USERNAME=root
ENV DB_PASSWORD=
ENV DB_DATABASE=etransportationsystem
CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.profiles=docker"]
# CMD ["./mvnw", "spring-boot:run", "-Pdocker"]



