FROM openjdk:11

VOLUME /tmp

ARG JAR_FILE=target/*.jar
ARG PROPS_FILE=application.properties

COPY ${JAR_FILE} app.jar
COPY ${PROPS_FILE} application.properties

ENTRYPOINT ["java","-jar","/app.jar"]