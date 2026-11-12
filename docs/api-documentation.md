# API documentation

Base URL: `http://localhost:8080/api`

| Method | Endpoint | Purpose |
| --- | --- | --- |
| POST | `/auth/register` | Create a user with a hashed password |
| POST | `/auth/login` | Authenticate a user |
| GET | `/issues` | List issues, optionally filtered by status/category |
| POST | `/issues` | Report an issue and calculate its priority/SLA |
| GET | `/issues/{id}` | View issue details |
| PUT | `/issues/{id}` | Update issue status and notes |
| POST | `/issues/{id}/updates` | Add a timeline update |
| POST | `/issues/{id}/assign` | Assign a technician |
| GET | `/technicians` | List technicians with workload |
| GET | `/technicians/{id}/workload` | View technician workload |
| GET | `/assets` | List visible assets with stored health scores and derived condition labels (authenticated) |
| POST | `/assets` | Create an asset |
| GET | `/dashboard/{role}` | Get a role-specific dashboard summary |
| GET | `/notifications` | List notifications |
| PUT | `/notifications/{id}/read` | Mark a notification read |

All SQL uses parameters. Asset visibility is role-aware: admins and authenticated users can view the inventory used for issue reporting, while technicians see only assets attached to their current active assignments. Technician identity comes from the authenticated session, not a request parameter.
