FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY tariffCalc/target/tariffCalc-0.0.1-SNAPSHOT.jar tariffCalc.jar
COPY user/target/user-0.0.1-SNAPSHOT.jar user.jar
EXPOSE 8080 8081
CMD java -jar tariffCalc.jar & java -jar user.jar & wait