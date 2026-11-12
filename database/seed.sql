USE fixflow;

INSERT INTO roles (name) VALUES ('USER'), ('TECHNICIAN'), ('ADMIN');
INSERT INTO issue_categories (name) VALUES ('Electrical'), ('Plumbing'), ('HVAC'), ('IT / Network'), ('Furniture'), ('Security'), ('Other');
INSERT INTO asset_categories (name) VALUES ('HVAC'), ('Server'), ('Power'), ('Printing'), ('Security'), ('Furniture');
INSERT INTO skills (name) VALUES ('HVAC'), ('Electrical'), ('Plumbing'), ('IT / Network'), ('Security'), ('General Repairs');

INSERT INTO locations (building, floor_name, room_name) VALUES
 ('Block A', 'Floor 3', 'Conference Room 302'),
 ('Block B', 'Floor 2', 'Finance Bay'),
 ('Block A', 'Floor 2', 'Pantry'),
 ('Block A', 'Ground', 'North Entrance'),
 ('Block B', 'Basement', 'Server Room'),
 ('Block A', 'Floor 1', 'Classroom 101'),
 ('Block A', 'Floor 1', 'Computer Lab 105'),
 ('Block A', 'Floor 2', 'Faculty Office 204'),
 ('Block A', 'Floor 4', 'Seminar Hall 401'),
 ('Block B', 'Ground', 'Reception Hall'),
 ('Block B', 'Floor 1', 'Classroom 110'),
 ('Block B', 'Floor 3', 'Computer Lab 305'),
 ('Block B', 'Floor 4', 'Faculty Office 410'),
 ('Block C', 'Ground', 'Student Lounge'),
 ('Block C', 'Floor 1', 'Classroom 101'),
 ('Block C', 'Floor 2', 'Computer Lab 201'),
 ('Block C', 'Floor 3', 'Seminar Hall'),
 ('Library', 'Basement', 'Archives'),
 ('Library', 'Ground', 'Reading Hall'),
 ('Library', 'Floor 1', 'Research Room'),
 ('Library', 'Floor 2', 'Group Study Room'),
 ('Admin Building', 'Ground', 'Reception'),
 ('Admin Building', 'Floor 1', 'HR Office'),
 ('Admin Building', 'Floor 2', 'Board Room'),
 ('Admin Building', 'Floor 3', 'Conference Room 302'),
 ('Academic Block', 'Ground', 'Admissions Office'),
 ('Academic Block', 'Floor 1', 'Classroom 101'),
 ('Academic Block', 'Floor 2', 'Computer Lab 201'),
 ('Academic Block', 'Floor 3', 'Faculty Office'),
 ('Academic Block', 'Floor 4', 'Seminar Hall')
ON DUPLICATE KEY UPDATE id = id;

-- Demo hash format: PBKDF2 hash values are placeholders for local seed login demonstrations.
INSERT INTO users (role_id, full_name, email, phone, organization, password_hash) VALUES
 (1, 'Alex Johnson', 'alex@vertexbusiness.com', '+91 98765 43210', 'Vertex Business Center', 'pbkdf2$demo$alex'),
 (2, 'Arjun Mehta', 'arjun@vertexbusiness.com', '+91 98765 40012', 'Vertex Business Center', 'pbkdf2$demo$arjun'),
 (2, 'Rahul Verma', 'rahul@vertexbusiness.com', '+91 98765 40013', 'Vertex Business Center', 'pbkdf2$demo$rahul'),
 (3, 'Priya Sharma', 'priya@vertexbusiness.com', '+91 98765 40014', 'Vertex Business Center', 'pbkdf2$demo$priya');

INSERT INTO technicians (user_id, availability_status, capacity_limit) VALUES (2, 'BUSY', 10), (3, 'AVAILABLE', 10);
INSERT INTO technician_skills (technician_id, skill_id, proficiency) VALUES (1, 1, 'EXPERT'), (1, 2, 'INTERMEDIATE'), (2, 4, 'EXPERT'), (2, 6, 'INTERMEDIATE');

INSERT INTO assets (asset_code, asset_name, category_id, location_id, criticality, health_score, purchase_date, warranty_end, last_maintenance_date, next_maintenance_date) VALUES
 ('HVAC-302', 'Conference Room HVAC', 1, 1, 'HIGH', 68, '2022-05-20', '2026-05-20', '2026-07-21', '2026-08-21'),
 ('PRN-14', 'Finance printer', 4, 2, 'MEDIUM', 68, '2021-03-15', '2025-03-15', '2026-07-09', '2026-09-01'),
 ('UPS-04', 'Server room power backup', 3, 5, 'CRITICAL', 78, '2023-01-10', '2028-01-10', '2026-06-29', '2026-08-22'),
 ('SRV-02', 'Application server', 2, 5, 'CRITICAL', 94, '2024-02-18', '2029-02-18', '2026-08-02', '2026-09-02');

INSERT INTO issues (issue_number, reporter_id, category_id, location_id, asset_id, title, description, severity, impact_score, affected_users, priority_score, priority, status, sla_hours, sla_deadline) VALUES
 ('FF-1042', 1, 3, 1, 1, 'AC not cooling', 'The room temperature is above 26°C and a meeting is scheduled.', 'HIGH', 3, 12, 15, 'HIGH', 'IN_PROGRESS', 6, '2026-08-17 15:18:00'),
 ('FF-1051', 1, 4, 2, 2, 'Printer offline', 'Finance printer shows an offline error after a paper jam.', 'MEDIUM', 2, 6, 9, 'MEDIUM', 'ASSIGNED', 24, '2026-08-18 11:00:00'),
 ('FF-1057', 1, 2, 3, NULL, 'Water pressure low', 'The pantry tap has low water pressure since the morning.', 'LOW', 1, 4, 5, 'LOW', 'OPEN', 48, '2026-08-19 09:00:00'),
 ('FF-1061', 1, 6, 4, NULL, 'Access card reader', 'Cards are not being recognized at the north entrance.', 'CRITICAL', 4, 35, 18, 'CRITICAL', 'ASSIGNED', 4, '2026-08-17 12:18:00');

INSERT INTO issue_assignments (issue_id, technician_id, assigned_by, accepted_at, is_current) VALUES (1, 1, 4, '2026-08-17 09:40:00', TRUE), (2, 2, 4, NULL, TRUE);
INSERT INTO issue_updates (issue_id, author_id, status, note) VALUES (1, 1, 'OPEN', 'Issue reported with room and asset context.'), (1, 4, 'TRIAGED', 'Priority set to High with a 6-hour SLA.'), (1, 2, 'IN_PROGRESS', 'Technician is checking the condenser unit.');
INSERT INTO maintenance_history (asset_id, issue_id, technician_id, maintenance_type, notes, duration_minutes) VALUES (1, 1, 1, 'CORRECTIVE', 'Filter checked; condenser fan diagnosis in progress.', 90), (2, 2, 2, 'CORRECTIVE', 'Paper path and roller replacement scheduled.', 45);
INSERT INTO notifications (user_id, issue_id, title, message, notification_type) VALUES (1, 4, 'SLA watch', 'FF-1061 has 1h 18m remaining.', 'WARNING'), (1, 1, 'Issue update', 'FF-1042 is now in progress.', 'INFO'), (4, 4, 'Critical assignment', 'FF-1061 needs immediate attention.', 'CRITICAL');
