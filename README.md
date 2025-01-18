# URLShortener API Gateway

![Java Version](https://img.shields.io/badge/Java-21-orange)
![version](https://img.shields.io/badge/version-2.4.1-blue)

This project is a Spring Boot-based API Gateway for the URL Shortener project. It acts as a reverse proxy that routes
incoming requests to various backend services in the microservices architecture of the URL Shortener system.

## Features

- API Gateway for routing and load balancing requests to the URL Shortener microservices.
- Spring Boot-based application for easy customization and integration.
- Configurable via environment variables for logging, server behavior, and routing.
- Docker container support for easy deployment in containerized environments.
- Supports scalable and flexible routing for backend services like URL shortening and analytics.
- Rate Limiting: Built-in support for rate limiting, either in-memory or via Redis, to control traffic and prevent
  abuse of the URL shortening services.

## Prerequisites

- Java 21 or higher
- Maven 3.8+
- Docker (for containerized deployment)

## Getting Started

### Running Locally

1. Clone the repository:
   ```bash
   git clone https://github.com/akgarg0472/url-shortener-apigateway
   cd url-shortener-apigateway
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   java -jar target/UrlShortenerApiGateway.jar
   ```

The API Gateway will start, and you can access it at [http://localhost:8765](http://localhost:8765).

## Environment Variables

| Variable                                | Default Value                   | Description                                                                          |
|-----------------------------------------|---------------------------------|--------------------------------------------------------------------------------------|
| `API_GATEWAY_LOG_PATH`                  | `/tmp/logs/gateway`             | Path where Eureka logs will be stored.                                               |
| `API_GATEWAY_LOG_LEVEL`                 | `INFO`                          | Log level for general application logs.                                              |
| `API_GATEWAY_LOGGER_REF`                | `consoleLogger`                 | Reference name for the logger (`consoleLogger` or `fileLogger`).                     |
| `AUTH_CLIENT_REDIS_HOST`                | `localhost`                     | Redis host address for the authentication service.                                   |
| `AUTH_CLIENT_REDIS_PORT`                | `6379`                          | Redis port for the authentication service.                                           |
| `AUTH_CLIENT_REDIS_POOL_MAX_TOTAL`      | `128`                           | Maximum number of connections in the Redis pool for the authentication service.      |
| `AUTH_CLIENT_REDIS_POOL_MAX_IDLE`       | `128`                           | Maximum number of idle connections in the Redis pool for the authentication service. |
| `AUTH_CLIENT_REDIS_POOL_MIN_IDLE`       | `16`                            | Minimum number of idle connections in the Redis pool for the authentication service. |
| `SPRING_DATA_REDIS_HOST`                | `localhost`                     | Redis host for the Spring Data Redis connection.                                     |
| `SPRING_DATA_REDIS_PORT`                | `6379`                          | Redis port for the Spring Data Redis connection.                                     |
| `SPRING_DATA_REDIS_DATABASE`            | `4`                             | The Redis database number to use (default: 0).                                       |
| `SPRING_DATA_REDIS_PASSWORD`            | `""`                            | Password for Redis connection (if required).                                         |
| `SERVER_PORT`                           | `8765`                          | The port where the server will run.                                                  |
| `EUREKA_CLIENT_REGISTER_WITH_EUREKA`    | `true`                          | Whether to register the service with Eureka or not.                                  |
| `EUREKA_CLIENT_FETCH_REGISTRY`          | `true`                          | Whether to fetch the service registry from Eureka or not.                            |
| `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE` | `http://localhost:8761/eureka/` | The URL for the Eureka service registry.                                             |

## API Rate Limiting Configuration

This configuration is used to define the rate-limiting settings for various API endpoints, ensuring that requests are
limited to a certain threshold within a specified time window. It helps protect your APIs from abuse and ensures fair
usage by limiting the number of requests clients can make.

```bash
RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_AUTH=10
RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_URLSHORTENER=10
RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_STATISTICS=10
RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_PROFILES=10
RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_PAYMENTS=10
RATE_LIMITER_LIMITS_PER_MINUTE_ALL=50
```

**Breakdown of the Configuration**:

- **`RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_AUTH`**: The API path related to authentication is limited to **10 requests
  per minute**.
- **`RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_URLSHORTENER`**: The API path for URL shortening is limited to **10 requests
  per minute**.
- **`RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_STATISTICS`**: The API path for fetching statistics is limited to **10
  requests per minute**.
- **`RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_PROFILES`**: The API path for user profiles is limited to **10 requests per
  minute**.
- **`RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_PAYMENTS`**: The API path for payments is limited to **10 requests per minute
  **.
- **`RATE_LIMITER_LIMITS_PER_MINUTE_ALL`**: A global catch-all for all other paths, limited to **50 requests per minute
  **.

## Docker Deployment

The application is Dockerized for simplified deployment. The `Dockerfile` is already configured to build and run the
Spring Boot application.

The `Dockerfile` defines the build and runtime configuration for the container.

### Building the Docker Image

To build the Docker image, run the following command:

  ```bash
  docker build -t akgarg0472/urlshortener-api-gateway:tag .
```

### Run the Docker Container

You can run the application with custom environment variables using the docker run command. For example:

```bash
   docker run -d \
       -p 8765:8765 \
       -e API_GATEWAY_LOG_PATH=/tmp/logs/apigateway \
       -e API_GATEWAY_LOG_LEVEL=INFO \
       -e API_GATEWAY_LOGGER_REF=consoleLogger \
       -v /host/path/to/logs:/tmp/logs/ \
       --network=host \
       --name api-gateway \
       akgarg0472/urlshortener-api-gateway:tag
```

> Note: If API_GATEWAY_LOGGER_REF is set to fileLogger, you must bind the log directory to a host path using the -v
> option as shown above.

The API Gateway will be accessible at http://localhost:8765 on the host machine.
