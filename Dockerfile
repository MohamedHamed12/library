FROM eclipse-temurin:25.0.4_7-jre-alpine-3.24
EXPOSE 8080
WORKDIR /app
ARG JAR=library-0.0.1-SNAPSHOT.jar

COPY /target/$JAR /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
