#Build stage
FROM maven:3.8.7-openjdk-18 as build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

#Runtime stage
FROM amazoncorretto:17
WORKDIR /app
COPY --from=build /build/target/chat-service-backend-*.jar /app/

ARG PROJECT_VERSION=1.0.0
ARG ACTIVE_PROFILE=default

EXPOSE 8181

ENV APP_VERSION=${PROJECT_VERSION}
ENV PROFILE=${ACTIVE_PROFILE}
ENV DB_URL="jdbc:mysql://chats-mysql-svc:3306/chats"

CMD java -jar -Dspring.profiles.active=${PROFILE} -Dspring.datasource.url=${DB_URL} chat-service-backend-${APP_VERSION}.jar
