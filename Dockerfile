FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY app.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]