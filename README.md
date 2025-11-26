# Bus Booking System Backend

Bus Booking System is a microservices-based architecture designed to handle the core logic of a bus reservation platform. It utilizes the Spring Boot ecosystem to provide a scalable, robust, and secure environment for managing users, bus schedules, and bookings.

Key architectural features include centralized configuration, service discovery, and an API Gateway for routing and security.

## Full project source code

| Component        | GitHub Repository  |
|------------------|--------------------|
| Backend          | [nghiaz160904/bus-booking-system-backend](https://github.com/nghiaz160904/bus-booking-system-backend) |
| Frontend         | [nghiaz160904/bus-booking-system-frontend](https://github.com/nghiaz160904/bus-booking-system-frontend) |

---

## 🚀 Getting Started

Follow these instructions to get the project up and running on your local machine.

### Prerequisites

* Java 25
* Docker & Docker Compose

### 1. Build the projects

Navigate to the root directory and build the JAR files, skipping tests for a faster build:

```bash
./gradlew build -x test
```

### 2. Run with Docker

Once the build is complete, orchestrate the containers using Docker Compose:

```bash
docker-compose up -d
```

## 🔗 URLs & Services

Once the application is running, the services can be accessed at the following endpoints:

| Service          | Production  | Development                                    |
|------------------|-------------|------------------------------------------------|
| config-server    | TBD         | [http://localhost:8888](http://localhost:8888) |
| api-gateway      | TBD         | [http://localhost:8080](http://localhost:8080) |
| service-registry | TBD         | [http://localhost:8081](http://localhost:8081) |
| user-service     | TBD         | [http://localhost:8082](http://localhost:8082) |

## Authentication and authorization design

The system implements a robust security mechanism using JSON Web Tokens (JWT) combined with HttpOnly Cookies and database persistence for session management.

### 1. The Architecture

* **Access Token (JWT):**

  * **Purpose:** Used to authenticate API requests to microservices.
  * **Storage:** Stored in client memory (variable) or non-HttpOnly storage.
  * **Lifespan:** Short-lived (15 minutes).
  * **Payload:** Contains User ID and `Roles` (User, Admin).

* **Refresh Token:**

  * **Purpose:** Used to generate new Access Tokens when the current one expires without requiring the user to log in again.

  * **Storage:** Sent to the client in a secure HttpOnly Cookie.

  * **Persistence:** A hash of the refresh token is stored in the Database associated with the user.

  * **Lifespan:** Long-lived (7 days).

### 2. Role-Based Access Control (RBAC)

Authorization is handled via scopes/claims embedded directly in the JWT Access Token.

* `ROLE_USER`: Can search buses, book tickets, and view their own profile.

* `ROLE_ADMIN`: Can manage bus routes, schedules, and view all user data.

The API Gateway inspects the JWT header to allow or deny traffic based on these roles before the request reaches the downstream services.

### 3. Decisions and Trade-offs

We chose this hybrid approach (Stateless Access Token + Stateful Refresh Token) to balance security and performance.

| Decision | Implementation | Trade-off / Rationale |
| :--- | :--- | :--- |
| **HttpOnly Cookies** | The Refresh Token is strictly stored in an `HttpOnly`, `Secure` cookie. | **Why:** This prevents Cross-Site Scripting (XSS) attacks. JavaScript cannot read this cookie, making it significantly harder for attackers to steal the user's session. |
| **Database Storage** | We store the Refresh Token status in the database. | **Why:** This allows for **Token Revocation**. If a user's account is compromised or they click "Log Out on all devices," we can simply delete/invalidate the token in the DB, instantly cutting off access. `<br><br>` **Trade-off:** It adds a database look-up overhead during the token refresh process, but increases security control. |
| **JWT for Access** | Access Tokens are stateless JWTs. | **Why:** Microservices (User Service, Booking Service) can validate the token signature independently without needing to query a central database for every single API request, reducing latency. |
