# Bus Booking System Backend

[![GitHub Repository](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/nghiaz160904/bus-booking-system-backend)

## How to run locally

### 1. Build the projects

```bash
./gradlew build -x test
```

### 2. Run with Docker

```bash
docker-compose up -d
```

## URLs

| Service          | Production  | Development                                    |
|------------------|-------------|------------------------------------------------|
| config-server    |             | [http://localhost:8888](http://localhost:8888) |
| api-gateway      |             | [http://localhost:8080](http://localhost:8080) |
| service-registry |             | [http://localhost:8081](http://localhost:8081) |
| user-service     |             | [http://localhost:8082](http://localhost:8082) |

## Authentication and authorization design
