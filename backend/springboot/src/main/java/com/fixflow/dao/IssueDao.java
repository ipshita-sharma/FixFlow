package com.fixflow.dao;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fixflow.dto.CreateIssueRequest;
import com.fixflow.model.Issue;

@Repository
public class IssueDao {
    private final JdbcTemplate jdbc;
    public IssueDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private final org.springframework.jdbc.core.RowMapper<Issue> mapper = (rs, row) -> new Issue(
            rs.getLong("id"), rs.getString("issue_number"), rs.getString("title"), rs.getString("description"),
            rs.getString("category"), rs.getString("location"), rs.getString("asset_code"), rs.getString("severity"),
            rs.getInt("priority_score"), rs.getString("priority"), rs.getString("status"),
            rs.getObject("sla_deadline", LocalDateTime.class), rs.getString("technician"), rs.getObject("created_at", LocalDateTime.class));

    private String select() { return "SELECT i.id, i.issue_number, i.title, i.description, c.name AS category, CONCAT(l.building, ' · ', l.floor_name, ' · ', l.room_name) AS location, a.asset_code, i.severity, i.priority_score, i.priority, i.status, i.sla_deadline, COALESCE(tu.full_name, 'Unassigned') AS technician, i.created_at FROM issues i JOIN issue_categories c ON c.id = i.category_id JOIN locations l ON l.id = i.location_id LEFT JOIN assets a ON a.id = i.asset_id LEFT JOIN issue_assignments ia ON ia.issue_id = i.id AND ia.is_current = TRUE LEFT JOIN technicians t ON t.id = ia.technician_id LEFT JOIN users tu ON tu.id = t.user_id "; }

    public List<Issue> findAll(String status, String category) {
        return findAll(status, category, null);
    }

    public List<Issue> findAll(String status, String category, Long technicianId) {
        return findAll(status, category, technicianId, null);
    }

    public List<Issue> findAll(String status, String category, Long technicianId, Long reporterId) {
        StringBuilder sql = new StringBuilder(select()).append("WHERE 1=1 ");
        List<Object> parameters = new ArrayList<>();
        if (technicianId != null) {
            sql.append("AND ia.technician_id = ? AND ia.is_current = TRUE ");
            parameters.add(technicianId);
        }
        if (reporterId != null) {
            sql.append("AND i.reporter_id = ? ");
            parameters.add(reporterId);
        }
        if (status != null && !status.isBlank()) { sql.append("AND i.status = ? "); parameters.add(status); }
        if (category != null && !category.isBlank()) { sql.append("AND c.name = ? "); parameters.add(category); }
        sql.append("ORDER BY i.priority_score DESC, i.created_at DESC");
        return jdbc.query(sql.toString(), mapper, parameters.toArray());
    }

    public List<Issue> findRecent(int limit) {
        return jdbc.query(select() + "ORDER BY i.created_at DESC LIMIT ?", mapper, limit);
    }

    public Optional<Issue> findById(long id) { return jdbc.query(select() + "WHERE i.id = ?", mapper, id).stream().findFirst(); }

    public boolean hasReporter(long issueId, long reporterId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM issues WHERE id = ? AND reporter_id = ?", Integer.class, issueId, reporterId);
        return count != null && count > 0;
    }

    public List<Long> findCurrentTechnicianIds(long issueId) {
        return jdbc.queryForList("SELECT technician_id FROM issue_assignments WHERE issue_id = ? AND is_current = TRUE", Long.class, issueId);
    }

    public long create(CreateIssueRequest request, long reporterId, long categoryId, long locationId, Long assetId, int priorityScore, String priority, LocalDateTime deadline) {
        String number = "FF-" + jdbc.queryForObject(
                "SELECT COALESCE(MAX(CAST(SUBSTRING_INDEX(issue_number, '-', -1) AS UNSIGNED)), 1042) + 1 FROM issues",
                Integer.class);
        jdbc.update("INSERT INTO issues (issue_number, reporter_id, category_id, location_id, asset_id, title, description, severity, impact_score, affected_users, priority_score, priority, sla_hours, sla_deadline) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", number, reporterId, categoryId, locationId, assetId, request.title(), request.description(), request.severity().toUpperCase(), request.impactScore(), request.affectedUsers(), priorityScore, priority, slaHours(request.severity()), Timestamp.valueOf(deadline));
        return jdbc.queryForObject("SELECT id FROM issues WHERE issue_number = ?", Long.class, number);
    }

    public void update(long id, String status, String note, long authorId) {
        jdbc.update("UPDATE issues SET status = ?, resolved_at = CASE WHEN ? IN ('RESOLVED','CLOSED') THEN CURRENT_TIMESTAMP ELSE resolved_at END WHERE id = ?", status, status, id);
        jdbc.update("INSERT INTO issue_updates (issue_id, author_id, status, note) VALUES (?, ?, ?, ?)", id, authorId, status, note == null ? "Status updated." : note);
    }

    public void completeCurrentAssignments(long issueId) {
        jdbc.update("UPDATE issue_assignments SET is_current = FALSE, completed_at = COALESCE(completed_at, CURRENT_TIMESTAMP) WHERE issue_id = ? AND is_current = TRUE", issueId);
    }

    public void addAssignment(long issueId, long technicianId, long assignedBy) {
        Integer alreadyCurrent = jdbc.queryForObject("SELECT COUNT(*) FROM issue_assignments WHERE issue_id = ? AND technician_id = ? AND is_current = TRUE", Integer.class, issueId, technicianId);
        if (alreadyCurrent != null && alreadyCurrent > 0) return;
        jdbc.update("UPDATE issue_assignments SET is_current = FALSE WHERE issue_id = ?", issueId);
        jdbc.update("INSERT INTO issue_assignments (issue_id, technician_id, assigned_by) VALUES (?, ?, ?)", issueId, technicianId, assignedBy);
        jdbc.update("UPDATE issues SET status = 'ASSIGNED' WHERE id = ?", issueId);
    }

    public boolean hasCurrentAssignment(long issueId, long technicianId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM issue_assignments WHERE issue_id = ? AND technician_id = ? AND is_current = TRUE", Integer.class, issueId, technicianId);
        return count != null && count > 0;
    }

    public void acceptAssignment(long issueId, long technicianId, long userId) {
        Integer pending = jdbc.queryForObject("SELECT COUNT(*) FROM issue_assignments WHERE issue_id = ? AND technician_id = ? AND is_current = TRUE AND accepted_at IS NULL", Integer.class, issueId, technicianId);
        if (pending == null || pending == 0) return;
        jdbc.update("UPDATE issue_assignments SET accepted_at = CURRENT_TIMESTAMP WHERE issue_id = ? AND technician_id = ? AND is_current = TRUE AND accepted_at IS NULL", issueId, technicianId);
        jdbc.update("UPDATE issues SET status = 'IN_PROGRESS' WHERE id = ? AND status IN ('ASSIGNED','TRIAGED')", issueId);
        jdbc.update("INSERT INTO issue_updates (issue_id, author_id, status, note) VALUES (?, ?, 'IN_PROGRESS', ?)", issueId, userId, "Technician accepted the assignment.");
    }
    private int slaHours(String severity) {
    return switch (severity.toUpperCase()) {
        case "CRITICAL" -> 4;
        case "HIGH" -> 6;
        case "MEDIUM" -> 24;
        case "LOW" -> 48;
        default -> throw new IllegalArgumentException(
                "Unsupported severity: " + severity);
    };
}
}
