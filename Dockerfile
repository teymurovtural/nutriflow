FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew build -x test
RUN mv build/libs/*.jar build/libs/app.jar
EXPOSE 8080
CMD ["java", "-jar", "build/libs/app.jar"]