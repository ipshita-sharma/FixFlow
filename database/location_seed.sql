USE fixflow;

-- Idempotent demo-campus location expansion. The unique key on the
-- building/floor/room tuple preserves existing location rows and IDs.
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
