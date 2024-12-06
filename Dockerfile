FROM openjdk:21
WORKDIR /app
COPY target/LibraryMS-0.0.1-SNAPSHOT.jar /app/LibraryMS-0.0.1-SNAPSHOT.jar
EXPOSE 8080
CMD ["java", "-jar", "LibraryMS-0.0.1-SNAPSHOT.jar"]
