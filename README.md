# URLShortener API Gateway

This project is a Spring Boot-based API Gateway for the URL Shortener project. It acts as a reverse proxy that routes
incoming requests to various backend services in the microservices architecture of the URL Shortener system.

## Features

- API Gateway for routing and load balancing requests to the URL Shortener microservices.
- Spring Boot-based application for easy customization and integration.
- Configurable via environment variables for logging, server behavior, and routing.
- Docker container support for easy deployment in containerized environments.
- Supports scalable and flexible routing for backend services like URL shortening and analytics.

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

### Docker Deployment

To deploy this project as a Docker container, you can pass environment variables to customize the behavior of the Eureka
Discovery Server.

#### Environment Variables

| Variable                 | Default Value       | Description                                                      |
|--------------------------|---------------------|------------------------------------------------------------------|
| `API_GATEWAY_LOG_PATH`   | `/tmp/logs/gateway` | Path where Eureka logs will be stored.                           |
| `API_GATEWAY_LOG_LEVEL`  | `INFO`              | Log level for general application logs.                          |
| `API_GATEWAY_LOGGER_REF` | `consoleLogger`     | Reference name for the logger (`consoleLogger` or `fileLogger`). |

#### Building the Docker Image

1. Build the Docker image:

   ```bash
   docker build -t akgarg0472/urlshortener-api-gateway:tag .
   ```

2. Run the container:
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
   option as shown above.

The API Gateway will be accessible at http://localhost:8765 on the host machine.

### Customization

You can further customize the application by modifying the `application.yml` file or overriding specific properties
using environment variables or command-line arguments.

## Logging Configuration

The project uses the following logging configuration:

- **Log Path:** Controlled by the `API_GATEWAY_LOG_PATH` variable.
- **Log Level:** Configurable using `API_GATEWAY_LOG_LEVEL` variable.
- **Logger Reference:** Defined by `API_GATEWAY_LOGGER_REF` for more granular control over logging.
