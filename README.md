# FixFlow — Maintenance and Issue Management System

FixFlow is a full-stack web application designed to simplify maintenance operations across campuses, offices, and other facilities. It provides a centralized platform to report maintenance issues, prioritize them, assign technicians, track repairs, and manage facility assets.

The system supports three user roles — **Admin, Technician, and Regular User** — with role-based access and workflows.

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Technology Stack](#technology-stack)
- [System Architecture](#system-architecture)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [Documentation](#documentation)
- [Future Enhancements](#future-enhancements)

## Overview

Maintenance requests are often managed through scattered messages, manual records, or disconnected communication channels. This can make it difficult to track issue status, assign responsibility, and monitor repair progress.

FixFlow addresses this problem by organizing maintenance activities into a structured workflow:

**Report → Prioritize → Assign → Diagnose/Repair → Verify → Resolve**

The application brings issue tracking, technician assignments, asset information, and maintenance operations into one platform.

## Key Features

### 1. Role-Based Access

- **Admin:** Manages maintenance operations, assigns issues, and monitors workloads.
- **Technician:** Views assigned work and updates issue progress.
- **Regular User:** Reports maintenance issues and tracks their status.

### 2. Issue Management

- Create and track maintenance issues.
- Record issue details and relevant locations.
- Manage issue priorities and statuses.
- Assign issues to technicians.
- Track progress through the maintenance workflow.

### 3. Asset Management

- Maintain an asset register for facility equipment.
- Store asset information and maintenance-related details.
- Associate assets with reported issues where applicable.

### 4. Dashboard and Monitoring

- Provide role-specific views of maintenance activities.
- Display issue and assignment information.
- Help administrators monitor ongoing work.

### 5. Backend and Data Management

- REST APIs for communication between the frontend and backend.
- Session-based authentication using HTTP sessions.
- Server-side validation and access-control checks.
- MySQL database integration for persistent application data.

## Technology Stack

| Layer | Technologies |
|---|---|
| Frontend | React, JavaScript, HTML5, CSS3 |
| Frontend tooling | Vite, npm |
| Backend | Java, Spring Boot |
| API communication | REST APIs, HTTP |
| Data access | Spring JDBC, JdbcTemplate |
| Database | MySQL |
| Authentication | HTTP session-based authentication |
| Build and testing | Maven, JUnit, frontend test scripts |

The repository also contains a separate HTML/CSS/JavaScript frontend created as an independent static submission.

## System Architecture

FixFlow follows a layered architecture that separates the user interface, business logic, and database operations.

```text
                User
                 |
                 v
        React Frontend (Vite)
                 |
                 | HTTP / REST APIs
                 v
        Spring Boot Controllers
                 |
                 v
             Services
          (Business Logic)
                 |
                 v
               DAO
        (JdbcTemplate / SQL)
                 |
                 v
            MySQL Database
```

### How It Works

1. The user interacts with the React frontend.
2. The frontend sends HTTP requests to the Spring Boot REST APIs.
3. Controllers receive requests and pass them to the appropriate services.
4. Services execute business rules and coordinate data operations.
5. The DAO layer interacts with MySQL using Spring JDBC.
6. The backend returns a response, which the frontend uses to update the interface.

Authentication and authorization checks help protect operations according to the user's role.

## Project Structure

```text
FixFlow/
├── backend/
│   └── springboot/
│       ├── pom.xml
│       └── src/
│           ├── main/
│           │   ├── java/com/fixflow/
│           │   │   ├── config/
│           │   │   ├── controller/
│           │   │   ├── dao/
│           │   │   ├── dto/
│           │   │   ├── exception/
│           │   │   ├── model/
│           │   │   └── service/
│           │   └── resources/
│           └── test/
├── database/
│   ├── schema.sql
│   ├── seed.sql
│   ├── location_seed.sql
│   └── queries.sql
├── docs/
│   ├── api-documentation.md
│   ├── architecture.md
│   ├── database-design.md
│   ├── development-roadmap.md
│   ├── feature-specification.md
│   └── project-overview.md
├── frontend/
│   ├── react-app/
│   │   ├── src/
│   │   ├── package.json
│   │   └── vite.config.js
│   └── static-html/
├── AGENTS.md
├── .gitignore
└── README.md
```

## Getting Started

### Prerequisites

Install the following before running the full-stack application:

- Git
- Java 17 or a compatible JDK for the project's configured Java version
- Maven
- Node.js and npm
- MySQL Server

### 1. Clone the Repository

```bash
git clone https://github.com/ipshita-sharma/FixFlow.git
cd FixFlow
```

### 2. Set Up the Database

1. Start your MySQL server.
2. Create a database named `fixflow`.
3. Create or configure a MySQL user with the necessary permissions.
4. Execute `database/schema.sql` to create the required tables.
5. Review `database/seed.sql` and `database/location_seed.sql` before executing them to populate the appropriate initial data.

Example:

```sql
CREATE DATABASE fixflow;
```

Use the SQL scripts in the `database/` directory and follow any additional schema or initialization requirements documented in the project.

### 3. Configure the Backend

Navigate to the backend directory:

```bash
cd backend/springboot
```

Create a local configuration file from the example:

```bash
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties
```

Open `src/main/resources/application.properties` and configure your local MySQL connection details:

```properties
spring.application.name=fixflow-api
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/fixflow?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

Keep your actual credentials in the local configuration file. Do not commit passwords, tokens, or other secrets to GitHub.

### 4. Start the Backend

From `backend/springboot`, run:

```bash
mvn spring-boot:run
```

The backend is configured to run on:

`http://localhost:8080`

Keep this terminal open while using the application.

### 5. Start the React Frontend

Open a **second terminal** from the repository root:

```bash
cd frontend/react-app
npm install
```

Create a local `.env` file using the example:

```bash
cp .env.example .env
```

The example configures the API base URL as:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

Start the frontend:

```bash
npm run dev
```

Open the local URL printed by Vite in your terminal, typically:

`http://localhost:5173`

The frontend and backend must both be running, and the backend must be configured to connect to MySQL.

## Running the Application

For local development, run these components in separate terminals:

| Component | Command | Default address |
|---|---|---|
| MySQL | Start the MySQL service | Local database server |
| Spring Boot backend | `mvn spring-boot:run` | `http://localhost:8080` |
| React frontend | `npm run dev` | `http://localhost:5173` |

Run the backend command from `backend/springboot` and the frontend command from `frontend/react-app`.

### Static HTML Frontend

The `frontend/static-html/` directory contains the separate HTML/CSS/JavaScript submission. It can be opened independently for static frontend review. Its interactive behavior and integration capabilities are separate from those of the React application.

## Testing

### Backend Tests

From the backend directory:

```bash
mvn test
```

### Frontend Tests

From the React frontend directory:

```bash
npm test
```

### Production Build

From the React frontend directory:

```bash
npm run build
```

These commands run the available automated tests and build the frontend. Manual testing is also recommended for authentication, role-based access, issue creation, technician assignments, status changes, and database persistence.

## Documentation

Additional project documentation is available in `docs/`:

- **Architecture:** `docs/architecture.md`
- **API Documentation:** `docs/api-documentation.md`
- **Database Design:** `docs/database-design.md`
- **Feature Specification:** `docs/feature-specification.md`
- **Project Overview:** `docs/project-overview.md`
- **Development Roadmap:** `docs/development-roadmap.md`

## Future Enhancements

Potential improvements include:

- Automated notifications for issue updates and assignments.
- Enhanced maintenance analytics and reporting.
- Predictive maintenance using historical asset data.
- Improved deployment and production configuration.
- Expanded integration and end-to-end testing.

These are proposed enhancements and should not be interpreted as features already implemented.

## Project Status

FixFlow includes a React frontend, Spring Boot backend, MySQL database scripts, a separate static HTML/CSS/JavaScript frontend, automated tests, and project documentation.

The application is currently documented for local development. Deployment and live production availability are separate steps.

## Author

**Ipshita Sharma**

GitHub: [ipshita-sharma](https://github.com/ipshita-sharma)

---

If you find this project useful, feel free to explore the repository and its documentation.
