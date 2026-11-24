FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/servicio-autorizacion-*.jar app.jar
ENV IDENTIDAD_CLAVE_ARCHIVO=/app/claves/firma.json
VOLUME /app/claves
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
