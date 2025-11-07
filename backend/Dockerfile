# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
RUN apk add --no-cache maven
WORKDIR /app

# Create Maven cache directory
RUN mkdir -p /root/.m2/repository

# Build tariffCalc
COPY tariffCalc/pom.xml tariffCalc/pom.xml
COPY tariffCalc/src tariffCalc/src
RUN cd tariffCalc && mvn clean package -DskipTests -Dmaven.repo.local=/root/.m2/repository

# Build user
COPY user/pom.xml user/pom.xml
COPY user/src user/src
RUN cd user && mvn clean package -DskipTests -Dmaven.repo.local=/root/.m2/repository

# Build news
COPY news/pom.xml news/pom.xml
COPY news/src news/src
RUN cd news && mvn clean package -DskipTests -Dmaven.repo.local=/root/.m2/repository

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache dumb-init
WORKDIR /app
COPY --from=build /app/tariffCalc/target/tariffCalc-0.0.1-SNAPSHOT.jar tariffCalc.jar
COPY --from=build /app/user/target/user-0.0.1-SNAPSHOT.jar user.jar
COPY --from=build /app/news/target/news-0.0.1-SNAPSHOT.jar news.jar
EXPOSE 8080 8081 8082 8083
ENTRYPOINT ["dumb-init", "--"]
CMD java -jar tariffCalc.jar & java -jar user.jar & java -jar news.jar & wait