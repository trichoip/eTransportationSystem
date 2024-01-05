# syntax=docker/dockerfile:1
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# nếu bị lỗi ./mvnw: Permission denied thì 
RUN chmod +x mvnw

# nếu bị lỗi ./mvnw: not found thì có 2 cách 2 sửa
# cách 1 thêm 2 dòng bên dưới
# RUN apt-get update && apt-get install -y dos2unix
# RUN dos2unix ./mvnw

# cách 2 là vào file mvnw change end of line sequence thành LF
RUN ./mvnw dependency:go-offline
COPY src ./src
ENV DB_HOST=host.docker.internal
ENV DB_USERNAME=root
ENV DB_PASSWORD=
ENV DB_DATABASE=etransportationsystem
CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.profiles=docker"]
# CMD ["./mvnw", "spring-boot:run", "-Pdocker"]



