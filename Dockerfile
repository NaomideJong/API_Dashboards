# For Java 8, try this
# FROM openjdk:8-jdk-alpine

# For Java 17, try this
FROM openjdk:17-oracle

# Refer to Maven build -> finalName
ARG JAR_FILE=target/dashboards.jar

# cp target/spring-boot-web.jar /opt/app/app.jar
COPY ${JAR_FILE} dashboards.jar

# java -jar /opt/app/app.jar
ENTRYPOINT ["java","-jar","tms-api-v2.jar"]