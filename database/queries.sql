USE fixflow;

-- Open issues, highest urgency first
SELECT issue_number, title, priority, status, sla_deadline
FROM issues WHERE status NOT IN ('RESOLVED', 'CLOSED') ORDER BY priority_score DESC, sla_deadline;

-- Critical issues
SELECT issue_number, title, location_id, status, sla_deadline
FROM issues WHERE priority = 'CRITICAL' AND status NOT IN ('RESOLVED', 'CLOSED');

-- Technician workload
SELECT t.id, u.full_name, COUNT(ia.id) AS current_jobs, t.capacity_limit,
       ROUND(COUNT(ia.id) * 100 / t.capacity_limit, 0) AS load_percent
FROM technicians t JOIN users u ON u.id = t.user_id
LEFT JOIN issue_assignments ia ON ia.technician_id = t.id AND ia.is_current = TRUE
GROUP BY t.id, u.full_name, t.capacity_limit ORDER BY current_jobs DESC;

-- Unresolved issues by reporter
SELECT u.full_name, COUNT(i.id) AS unresolved_count
FROM users u JOIN issues i ON i.reporter_id = u.id
WHERE i.status NOT IN ('RESOLVED', 'CLOSED') GROUP BY u.id, u.full_name;

-- Asset failure count and average repair time
SELECT a.asset_code, a.asset_name, COUNT(mh.id) AS failure_count,
       ROUND(AVG(mh.duration_minutes) / 60, 1) AS avg_resolution_hours,
       a.health_score
FROM assets a LEFT JOIN maintenance_history mh ON mh.asset_id = a.id
GROUP BY a.id, a.asset_code, a.asset_name, a.health_score
HAVING failure_count > 2 ORDER BY failure_count DESC;

-- SLA-risk issues; the threshold is intentionally easy to explain
SELECT issue_number, title, priority, sla_deadline,
       TIMESTAMPDIFF(MINUTE, CURRENT_TIMESTAMP, sla_deadline) AS minutes_remaining
FROM issues WHERE status NOT IN ('RESOLVED', 'CLOSED')
  AND TIMESTAMPDIFF(MINUTE, CURRENT_TIMESTAMP, sla_deadline) BETWEEN 0 AND 180
ORDER BY minutes_remaining;

-- Maintenance history for one asset
SELECT a.asset_code, mh.maintenance_type, mh.notes, mh.duration_minutes, mh.performed_at, u.full_name AS technician
FROM maintenance_history mh JOIN assets a ON a.id = mh.asset_id
LEFT JOIN technicians t ON t.id = mh.technician_id LEFT JOIN users u ON u.id = t.user_id
WHERE a.asset_code = 'HVAC-302' ORDER BY mh.performed_at DESC;

-- Dashboard statistics
SELECT COUNT(*) AS open_issues FROM issues WHERE status NOT IN ('RESOLVED', 'CLOSED');
SELECT COUNT(*) AS critical_issues FROM issues WHERE priority = 'CRITICAL' AND status NOT IN ('RESOLVED', 'CLOSED');
SELECT COUNT(*) AS active_repairs FROM issues WHERE status IN ('ASSIGNED', 'IN_PROGRESS', 'WAITING_FOR_PARTS');
SELECT ROUND(AVG(CASE WHEN status IN ('RESOLVED', 'CLOSED') AND resolved_at <= sla_deadline THEN 100 ELSE 0 END), 0) AS sla_compliance FROM issues;
