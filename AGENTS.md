# FixFlow project rules

## Purpose

FixFlow is a student-friendly maintenance operations platform for reporting, prioritizing, assigning, repairing, and analyzing facility issues.

## Architecture

- `frontend/static-html/` is the original standalone HTML/CSS submission. It must remain functional without Node, JavaScript, React, Spring Boot, or MySQL.
- `frontend/react-app/` is the dynamic Vite + React frontend and should preserve the established visual language.
- `backend/springboot/` is one monolithic Spring Boot application with controller, service, DAO, model, DTO, config, and exception layers.
- Database access uses Spring JDBC / `JdbcTemplate`; do not replace it with JPA/Hibernate as the primary persistence layer.
- `database/` contains the normalized MySQL schema, realistic seed data, and teaching queries.

## Conventions

- Prefer clear JavaScript and Java over clever abstractions.
- Use semantic HTML, accessible labels, keyboard focus states, and responsive CSS.
- Keep business rules visible and explainable: priority, technician recommendation, SLA, and asset health are deterministic algorithms.
- Use parameterized SQL for every user-controlled value.
- Keep secrets in local configuration; commit only `application.properties.example`.
- Avoid microservices, unnecessary dependencies, external UI frameworks, and unrelated features.

## Non-negotiables

- Never delete or replace `frontend/static-html/` when changing React.
- React must preserve the design system and content of the static implementation.
- MySQL is the target database, Java is the backend language, and JDBC is the database access demonstration.
- Keep the implementation understandable enough for a semester project and viva.
