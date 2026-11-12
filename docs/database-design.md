# Database design

Core entities are `users`, `roles`, `issues`, `issue_categories`, `issue_assignments`, `issue_updates`, `technicians`, `technician_skills`, `skills`, `assets`, `asset_categories`, `locations`, `maintenance_history`, and `notifications`.

Relationships:

- A user reports many issues and receives many notifications.
- An issue belongs to one category, location, and optionally an asset.
- An issue can have assignment history and updates.
- A technician has many skills and assignments.
- An asset belongs to a category and location and has maintenance history.

See `database/schema.sql` for keys, checks, indexes, and timestamps.
