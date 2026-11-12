# Architecture

FixFlow is a monolith with clear layers. This keeps deployment and viva explanation realistic for a 2–3 month semester project.

```text
Browser
 ├─ static HTML/CSS submission
 └─ React application
       ↓ REST/JSON
Spring Boot controllers → services → JDBC DAOs → MySQL
```

The service layer owns business rules. DAOs own SQL. Controllers translate HTTP requests into DTOs and service calls. The database remains normalized and the API does not expose passwords.

## Business rules

- Priority score = severity + impact + affected-user count + asset criticality + SLA urgency.
- Technician score = skill match + availability + lower workload + location match + priority fit.
- SLA is `SAFE`, `AT_RISK`, or `BREACHED` based on remaining time and status.
- Asset health is stored in `assets.health_score`; the asset API preserves that value and derives a display condition from the stored score and next maintenance date.
