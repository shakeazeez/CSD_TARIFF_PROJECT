# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
RUN apk add --no-cache maven
WORKDIR /app

# Create Maven cache directory
RUN mkdir -p /root/.m2/repository

# Copy and build news service
COPY pom.xml .
COPY src src
RUN mvn clean package -DskipTests -Dmaven.repo.local=/root/.m2/repository

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/news-0.0.1-SNAPSHOT.jar news.jar
EXPOSE 8083
CMD ["java", "-jar", "news.jar"]