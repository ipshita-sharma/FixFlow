# FixFlow

**Maintenance, without the chaos.**

FixFlow is a full-stack maintenance and repair operations platform for offices, apartments, hotels, hospitals, schools, and other facilities. It centralizes issue reporting, transparent priority scoring, technician assignment, SLA monitoring, asset health, and recurring-failure analysis.

## Why it exists

Maintenance requests are commonly scattered across calls, WhatsApp messages, email, and spreadsheets. FixFlow creates one operational flow:

`Report → Prioritize → Assign → Diagnose → Repair → Verify → Resolve`

## Features

- Responsive standalone HTML + CSS submission in `frontend/static-html/`
- JavaScript validation, filtering, priority scoring, modals, and notifications
- React + Vite frontend with reusable SaaS dashboard components
- Spring Boot REST API with a layered service / DAO architecture
- MySQL schema with normalized tables, indexes, constraints, and sample records
- JDBC access through `JdbcTemplate` (no JPA as the primary data layer)
- Explainable priority, technician recommendation, SLA, asset-health, and recurring-failure logic
- User, technician, and administrator workflows

## Technology stack

HTML5, CSS3, JavaScript, React, React Router, Java, Spring Boot, Spring JDBC, MySQL, SQL.

## Repository map

```text
frontend/static-html/   standalone HTML/CSS academic submission
frontend/react-app/     Vite React application
backend/springboot/     monolithic Spring Boot API
database/               schema, seed records, and useful SQL queries
docs/                   viva-friendly project documentation
```

## Run it locally

### Standalone submission

Open `frontend/static-html/index.html` directly in a browser. It has no runtime dependencies.

### React frontend

```bash
cd frontend/react-app
npm install
npm run dev
```

The frontend uses mock data automatically when the backend is unavailable. Set `VITE_API_BASE_URL` when connecting to the API.

### MySQL

Create a database, then run `database/schema.sql` and `database/seed.sql` in that order.

### Spring Boot API

```bash
cd backend/springboot
cp src/main/resources/application.properties.example src/main/resources/application.properties
# edit the local MySQL values
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. See `docs/api-documentation.md` for endpoints.

### Reset the seeded technician passwords (development only)

FixFlow includes a one-shot password reset runner for the two seeded technician accounts. It is disabled unless the `dev` Spring profile is active and `fixflow.dev-technician-password-reset.enabled=true` is explicitly supplied. It has no public endpoint, validates both target accounts before making either update, and uses `PasswordService` for hashing.

From `backend/springboot`, run the following with two new local-only passwords:

```bash
SPRING_PROFILES_ACTIVE=dev \
FIXFLOW_DEV_TECHNICIAN_PASSWORD_RESET_ENABLED=true \
FIXFLOW_DEV_TECHNICIAN_PASSWORD_RESET_ARJUN_PASSWORD='replace-with-arjun-password' \
FIXFLOW_DEV_TECHNICIAN_PASSWORD_RESET_RAHUL_PASSWORD='replace-with-rahul-password' \
mvn spring-boot:run
```

Wait for the application to start successfully, then stop it with `Ctrl-C`. The runner executes once during that startup. Do not commit the command, passwords, or local property values. To disable the utility afterward, remove the environment variables or set `FIXFLOW_DEV_TECHNICIAN_PASSWORD_RESET_ENABLED=false`; leave the `dev` profile inactive during normal runs.

## Screenshots

Add screenshots of the landing page, user dashboard, issue details, technician dashboard, admin command center, and asset management screen here during the final submission.

## Architecture

```text
React / HTML + CSS
        ↓ fetch()
Spring Boot REST controllers
        ↓
Service layer + transparent business rules
        ↓
JdbcTemplate DAO layer
        ↓
MySQL
```

## Future scope

Preventive-maintenance scheduling, email/SMS notifications, real file storage, SSO, audit logs, and ML-assisted failure forecasting can be added after the core academic project is stable.

## Academic note

The static HTML/CSS folder is intentionally preserved as a separate deliverable. It must never be deleted to make room for React.
