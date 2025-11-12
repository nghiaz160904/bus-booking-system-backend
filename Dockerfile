# Stage 1: Build the application
# Use the generic '25-jdk' tag
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace

# Copy the Gradle wrapper files
COPY gradlew .
COPY gradle ./gradle
# Copy the .kts (Kotlin DSL) build files
COPY build.gradle.kts ./
COPY settings.gradle.kts ./
COPY src ./src

# Run the build
RUN ./gradlew build -x test

# Stage 2: Create the final, small image
# Use the generic '25-jre' tag
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy the built .jar file from the 'builder' stage
COPY --from=builder /workspace/build/libs/*.jar app.jar

# Expose the port your Spring Boot app runs on (default is 8080)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]