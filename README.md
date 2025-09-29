# Movies Track API

This is a RESTful API for a movie tracking application, built with Spring Boot. It handles user authentication and provides a foundation for movie-related functionalities.

## Features

- User authentication using JWT (JSON Web Tokens).
- Secure cookie-based session management.
- Containerized setup with Docker for easy development and deployment.

## Prerequisites

Before you begin, ensure you have the following installed on your system:

- Java 17 or later
- Docker
- Docker Compose

## Getting Started

Follow these steps to get the application up and running.

### 1. Clone the repository

```bash
git clone https://github.com/panosdim/movies-track-api.git
cd movies-track-api
```

### 2. Configuration

The application uses a `.env` file for configuration. Create a `.env` file in the root of the project by copying the example file:

```bash
cp .env.example .env
```

Now, open the `.env` file and set the required environment variables:

```env
# PostgreSQL Database settings
DB_USER=your_db_user
DB_PASSWORD=your_strong_db_password

# JWT Settings
# The JWT_SECRET must be a Base64-encoded string.
# You can generate a suitable key using an online tool or the following command:
# openssl rand -base64 32
JWT_SECRET=c29tZXN1cGVyc2VjcmV0and0a2V5dGhhdGlzYXRsZWFzdDI1NmJpdHNsb25n
JWT_EXPIRATION_MS=86400000 # 24 hours
```

> **Important**: The `JWT_SECRET` must be a Base64 encoded string that is sufficiently long for the HMAC-SHA algorithm used (e.g., 256, 384, or 512 bits).

### 3. Build and Run with Docker

The `compose.yaml` file is configured to build and run the entire application stack, including the PostgreSQL database.

Run the following command to start the services in detached mode:

```bash
docker compose up --build -d
```

The API will be available at `http://localhost:8080`.

### 4. Run with Gradle (Development)

If you prefer to run the application directly using Gradle for development, you can do so after configuring the `.env` file.

First, ensure your environment variables from `.env` are loaded. This is crucial for the application to pick up database and JWT settings.

```bash
set -o allexport
source .env
set +o allexport
./gradlew bootRun
```

The application will be available at `http://localhost:8080`.


## API Endpoints

Here are the available API endpoints.

### Authentication

#### `POST /login`

Authenticates a user and returns a JWT in an HTTP-only cookie.

**Request Body:**

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Success Response (200 OK):**

- An `HttpOnly` cookie named `jwt` is set in the response headers.
- The response body contains user information:
  ```json
  {
    "firstName": "John",
    "lastName": "Doe"
  }
  ```

**Error Response (401 Unauthorized):**

- Returned if the email or password is incorrect.

## Built With

- Spring Boot - The web framework used
- PostgreSQL - Database
- Docker - Containerization
- Gradle - Dependency Management
- jjwt - Java library for JWT