FROM gradle:8.11.1-jdk17 AS builder
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src src
RUN gradle build -x test --no-daemon


FROM eclipse-temurin:17-jre
RUN mkdir /opt/app
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar /opt/app/spring-boot-application.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-Dspring.profiles.active=prod", "-jar", "/opt/app/spring-boot-application.jar"]