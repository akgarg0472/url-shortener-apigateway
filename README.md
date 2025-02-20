# URLShortener API Gateway

![Java Version](https://img.shields.io/badge/Java-21-orange)
![version](https://img.shields.io/badge/version-2.8.0-blue)

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

| Variable                                 | Default Value | Description                                                                          |
|------------------------------------------|---------------|--------------------------------------------------------------------------------------|
| `AUTH_CLIENT_REDIS_HOST`                 | `localhost`   | Redis host address for the authentication service.                                   |
| `AUTH_CLIENT_REDIS_PORT`                 | `6379`        | Redis port for the authentication service.                                           |
| `AUTH_CLIENT_REDIS_POOL_MAX_TOTAL`       | `128`         | Maximum number of connections in the Redis pool for the authentication service.      |
| `AUTH_CLIENT_REDIS_POOL_MAX_IDLE`        | `128`         | Maximum number of idle connections in the Redis pool for the authentication service. |
| `AUTH_CLIENT_REDIS_POOL_MIN_IDLE`        | `16`          | Minimum number of idle connections in the Redis pool for the authentication service. |
| `SPRING_DATA_REDIS_HOST`                 | `localhost`   | Redis host for the Spring Data Redis connection.                                     |
| `SPRING_DATA_REDIS_PORT`                 | `6379`        | Redis port for the Spring Data Redis connection.                                     |
| `SPRING_DATA_REDIS_DATABASE`             | `4`           | The Redis database number to use (default: 0).                                       |
| `SPRING_DATA_REDIS_PASSWORD`             | `""`          | Password for Redis connection (if required).                                         |
| `SERVER_PORT`                            | `8765`        | The port where the server will run.                                                  |
| `SPRING_CLOUD_CONSUL_DISCOVERY_REGISTER` | `true`        | Whether to register the service with Consul or not.                                  |
| `SPRING_CLOUD_CONSUL_DISCOVERY_ENABLED`  | `true`        | Whether service discovery via Consul is enabled or not.                              |
| `SPRING_CLOUD_CONSUL_HOST`               | `localhost`   | The Consul agent's hostname or IP address.                                           |
| `SPRING_CLOUD_CONSUL_PORT`               | `8500`        | The port on which the Consul agent is listening.                                     |

## Logging Configuration

The URL Shortener Service uses environment variables for logging configuration. Below are the available environment
variables that you can customize:

- **LOGGING_CONSOLE_ENABLED**: Enables or disables console-based logging.
    - Default value: `false`
    - Allowed values: `true`, `false`

- **LOGGING_FILE_ENABLED**: Enables or disables file-based logging.
    - Default value: `false`
    - Allowed values: `true`, `false`

- **LOGGING_FILE_BASE_PATH**: Specifies the base path for log files.
    - Default value: `/tmp`

- **LOGGING_LEVEL**: Specifies the log level for the application.
    - Default value: `INFO`
    - Allowed values: `DEBUG`, `INFO`, `WARN`, `ERROR`

- **LOGGING_STREAM_ENABLED**: Enables or disables streaming logs.
    - Default value: `false`
    - Allowed values: `true`, `false`

- **LOGGING_STREAM_HOST**: Specifies the host for streaming logs.
    - Default value: `localhost`

- **LOGGING_STREAM_PORT**: Specifies the port for streaming logs.
    - Default value: `5000`

- **LOGGING_STREAM_PROTOCOL**: Specifies the protocol used for log streaming.
    - Default value: `TCP`
    - Allowed values: `TCP`, `UDP`

## API Rate Limiting Configuration

This configuration is used to define the rate-limiting settings for various API endpoints, ensuring that requests are
limited to a certain threshold within a specified time window. It helps protect your APIs from abuse and ensures fair
usage by limiting the number of requests clients can make.

```bash
RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_AUTH=10
RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_URLSHORTENER=5
RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_STATISTICS=20
RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_PROFILES=20
RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_PAYMENTS=20
RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_SUBSCRIPTIONS=20
RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_SUBSCRIPTIONS_PACKS=50
RATE_LIMITER_LIMITS_PER_MINUTE_ALL=50
```

**Breakdown of the Configuration**:

- **RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_AUTH**: Sets the limit of 10 requests per minute for `/api/v1/auth/**`.
- **RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_URLSHORTENER**: Sets the limit of 5 requests per minute for
  `/api/v1/urlshortener/**`.
- **RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_STATISTICS**: Sets the limit of 20 requests per minute for
  `/api/v1/statistics/**`.
- **RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_PROFILES**: Sets the limit of 20 requests per minute for
  `/api/v1/profiles/**`.
- **RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_PAYMENTS**: Sets the limit of 20 requests per minute for
  `/api/v1/payments/**`.
- **RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_SUBSCRIPTIONS**: Sets the limit of 20 requests per minute for
  `/api/v1/subscriptions/**`.
- **RATE_LIMITER_LIMITS_PER_MINUTE_API_V1_SUBSCRIPTIONS_PACKS**: Sets the limit of 50 requests per minute for
  `/api/v1/subscriptions/packs/**`.
- **RATE_LIMITER_LIMITS_PER_MINUTE_ALL**: Sets the default limit of 50 requests per minute for any other endpoints (
  `/**`).

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
