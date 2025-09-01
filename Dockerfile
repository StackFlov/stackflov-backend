# 베이스 이미지: Java 21 환경
FROM openjdk:21-jdk-slim
WORKDIR /app

# JAR 파일 복사: 빌드된 .jar 파일을 컨테이너 안으로 복사
COPY build/libs/*.jar app.jar

# 실행: 컨테이너 시작 시 애플리케이션 구동
ENTRYPOINT ["java", "-jar", "app.jar"]