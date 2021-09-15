FROM openjdk:8-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} order-service.jar
ENTRYPOINT ["/bin/ash", "-c"]
CMD [" \
java -jar -Dserver.port=8082 /order-service.jar \
"]
#HEALTHCHECK  --interval=5m --timeout=3s \
#   CMD curl -f http://localhost/ || exit 1
