FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src ./src
RUN ./mvnw -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

ENV JAVA_OPTS=""
RUN mkdir -p /app/uploads/documents
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
