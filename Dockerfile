FROM openjdk:21-jdk
ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} comket-backend.jar
# COPY build/libs/*.jar comket-backend.jar
ENTRYPOINT ["java","-jar","/comket-backend.jar"]

RUN ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime
