FROM maven:3.8.5-eclipse-temurin-17

COPY . .

ENTRYPOINT ["java","-jar","target/SearchEngine-1.0-SNAPSHOT.jar"]