FROM openjdk:21-jdk
ARG JAR_FILE=build/libs/*.jar

# 애플리케이션 JAR 파일 복사
COPY ${JAR_FILE} comket-backend.jar
# COPY build/libs/*.jar comket-backend.jar

# Firebase 서비스 계정 파일을 위한 디렉토리 생성
RUN mkdir -p /app/config

# Firebase 서비스 계정 파일 복사
COPY firebase-service-account.json /app/config/

# 타임존 설정
RUN ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime

# 실행 명령
ENTRYPOINT ["java","-jar","/comket-backend.jar"]
