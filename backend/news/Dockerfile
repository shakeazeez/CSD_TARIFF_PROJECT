# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
RUN apk add --no-cache maven
WORKDIR /app

# Create Maven cache directory
RUN mkdir -p /root/.m2/repository

# Copy pom.xml and source
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -Dmaven.repo.local=/root/.m2/repository

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache dumb-init
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["dumb-init", "--"]
CMD ["java", "-jar", "app.jar"]