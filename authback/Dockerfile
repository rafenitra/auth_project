FROM alpine/java:22.0.2-jdk AS build
WORKDIR /build
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline
COPY src src
RUN ./mvnw package -DskipTests

FROM alpine/java:22.0.2-jre
WORKDIR /app
COPY --from=build /build/target/*-SNAPSHOT.jar app.jar
EXPOSE 8080
#faut toujours préciser production
ENTRYPOINT ["java", "-Dspring.profiles.active=prod","-jar", "app.jar"]