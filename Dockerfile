FROM openjdk:11

VOLUME /tmp

ARG JAR_FILE=target/*.jar
ARG PROPS_FILE=application.properties

COPY ${JAR_FILE} app.jar
COPY ${PROPS_FILE} application.properties

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]