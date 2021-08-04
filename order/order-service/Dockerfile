ARG JDK_VERSION
FROM openjdk:${JDK_VERSION}
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} order-service.jar
ENTRYPOINT ["java","-jar","-Dserver.port=8082","/order-service.jar"]