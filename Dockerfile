FROM maven:3.9.12-eclipse-temurin-21 AS build

ARG MODULE
WORKDIR /workspace

COPY pom.xml .
COPY discovery-server/pom.xml discovery-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY user-service/pom.xml user-service/pom.xml

RUN mvn -B -pl ${MODULE} -am dependency:go-offline

COPY discovery-server/src discovery-server/src
COPY api-gateway/src api-gateway/src
COPY user-service/src user-service/src

RUN mvn -B -pl ${MODULE} -am package -DskipTests

FROM eclipse-temurin:21-jre

ARG MODULE
WORKDIR /app

COPY --from=build /workspace/${MODULE}/target/*.jar app.jar

ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
