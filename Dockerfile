FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY . .

# Copy the local Maven dependency into the container
COPY ./libs/ /root/.m2/repository/

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/UrlShortenerApiGateway.jar .

# Set the default active profile to 'dev', can be overridden at runtime
ENV SPRING_PROFILES_ACTIVE=dev

CMD ["java", "-jar", "UrlShortenerApiGateway.jar"]
