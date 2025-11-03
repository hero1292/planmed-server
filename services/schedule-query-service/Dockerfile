FROM mvkvl/maven:jdk-21-alpine AS build
WORKDIR /app
COPY . .
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests -pl services/schedule-query-service -am package
FROM alpine/java:21-jdk
ENV TZ=UTC \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseZGC -Djava.security.egd=file:/dev/./urandom"
WORKDIR /opt/app
COPY --from=build /app/services/schedule-query-service/target/*SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["/bin/sh","-c","java $JAVA_OPTS -jar app.jar"]
