# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
RUN apk add --no-cache maven
WORKDIR /app

# Build tariffCalc
COPY tariffCalc/pom.xml tariffCalc/pom.xml
COPY tariffCalc/src tariffCalc/src
RUN cd tariffCalc && mvn clean package -DskipTests

# Build user
COPY user/pom.xml user/pom.xml
COPY user/src user/src
RUN cd user && mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/tariffCalc/target/tariffCalc-0.0.1-SNAPSHOT.jar tariffCalc.jar
COPY --from=build /app/user/target/user-0.0.1-SNAPSHOT.jar user.jar
EXPOSE 8080 8081
CMD java -jar tariffCalc.jar & java -jar user.jar & wait