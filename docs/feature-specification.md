# Feature specification

## Phase 1: standalone UI

The static submission includes landing, authentication, user dashboard, report form, issue list, issue detail, technician dashboard, admin command center, assets, and profile pages. All navigation uses normal HTML links and it works without JavaScript.

## Phase 2: client-side behavior

The shared JavaScript adds browser validation, issue filtering, priority calculation, modal interactions, upload feedback, toast notifications, and sortable-style filter controls.

## Phase 3+: full stack

The React application uses reusable components and API-ready services. The Spring Boot API exposes authentication, issues, assignments, technicians, assets, dashboards, and notifications. MySQL persists the operational data.
