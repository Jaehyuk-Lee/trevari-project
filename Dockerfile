# ---- Build stage ----
FROM gradle:8.10.2-jdk17-alpine AS build
WORKDIR /workspace
COPY . .
RUN gradle clean bootJar --no-daemon

# ---- Run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
# 만들어진 JAR 경로는 프로젝트명에 따라 달라집니다. 파일명 확인 후 수정하세요.
# 예: build/libs/trevari-project-0.0.1-SNAPSHOT.jar
COPY --from=build /workspace/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
