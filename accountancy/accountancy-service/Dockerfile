FROM openjdk:8-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} accountancy-service.jar
ENTRYPOINT ["/bin/ash", "-c"]
CMD [" \
java -jar -Dserver.port=8084 /accountancy-service.jar \
"]
