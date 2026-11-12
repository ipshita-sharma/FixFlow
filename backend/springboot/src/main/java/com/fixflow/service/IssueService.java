package com.fixflow.service;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fixflow.dao.IssueDao;
import com.fixflow.dao.TechnicianDao;
import com.fixflow.dao.UserDao;
import com.fixflow.dto.CreateIssueRequest;
import com.fixflow.dto.TechnicianUpdateRequest;
import com.fixflow.dto.UpdateIssueRequest;
import com.fixflow.exception.ForbiddenException;
import com.fixflow.exception.ResourceNotFoundException;
import com.fixflow.model.Issue;

@Service
public class IssueService {
    private final IssueDao issues;
    private final JdbcTemplate jdbc;
    private final PriorityEngine priority;
    private final SlaEngine sla;
    private final UserDao users;
    private final TechnicianDao technicians;

    public IssueService(IssueDao issues, JdbcTemplate jdbc, PriorityEngine priority, SlaEngine sla, UserDao users, TechnicianDao technicians) { this.issues = issues; this.jdbc = jdbc; this.priority = priority; this.sla = sla; this.users = users; this.technicians = technicians; }
    public List<Issue> findAll(String status, String category) { return findAll(status, category, null); }
    public List<Issue> findAll(String status, String category, Long technicianId) { return issues.findAll(status, category, technicianId); }
    public List<Issue> findAll(String status, String category, Long technicianId, Long reporterId) { return issues.findAll(status, category, technicianId, reporterId); }
    public Issue find(long id) { return issues.findById(id).orElseThrow(() -> new ResourceNotFoundException("Issue " + id + " was not found.")); }

    public Issue create(CreateIssueRequest request, long reporterId) {
        long categoryId = lookupId("issue_categories", request.category());
        long locationId = lookupLocation(request);
        Long assetId = request.assetCode() == null || request.assetCode().isBlank() ? null : lookupId("assets", request.assetCode(), "asset_code");
        String criticality = assetId == null ? "MEDIUM" : jdbc.queryForObject("SELECT criticality FROM assets WHERE id = ?", String.class, assetId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = sla.deadline(now, request.severity());
        int score = priority.score(request.severity(), request.impactScore(), request.affectedUsers(), criticality, urgency(request.severity()));
        long id = issues.create(request, reporterId, categoryId, locationId, assetId, score, priority.label(score), deadline);
        return find(id);
    }

    @Transactional
    public Issue update(long id, UpdateIssueRequest request, long authorId) {
        return applyStatusUpdate(id, request.status().toUpperCase(Locale.ROOT), request.note(), authorId);
    }

    @Transactional
    public void assign(long id, long technicianId, long assignedBy) {
        find(id);
        if (!users.isAdminUser(assignedBy)) throw new ForbiddenException("Only administrators can assign issues.");
        if (!users.isTechnician(technicianId)) throw new IllegalArgumentException("The selected technician is not registered.");
        Set<Long> affectedTechnicians = new LinkedHashSet<>(issues.findCurrentTechnicianIds(id));
        issues.addAssignment(id, technicianId, assignedBy);
        affectedTechnicians.add(technicianId);
        affectedTechnicians.forEach(technicians::refreshAvailability);
    }

    @Transactional
    public Issue accept(long id, long technicianId, long userId) {
        Issue issue = find(id);
        verifyTechnicianAssignment(id, technicianId, userId);
        if (!Set.of("ASSIGNED", "TRIAGED", "IN_PROGRESS", "WAITING_FOR_PARTS").contains(issue.status())) {
            throw new IllegalArgumentException("This issue is not available to accept.");
        }
        issues.acceptAssignment(id, technicianId, userId);
        technicians.refreshAvailability(technicianId);
        return find(id);
    }

    @Transactional
    public Issue technicianUpdate(long id, TechnicianUpdateRequest request, long technicianId, long userId) {
        Issue issue = find(id);
        verifyTechnicianAssignment(id, technicianId, userId);
        String status = request.status().trim().toUpperCase(Locale.ROOT);
        if (!Set.of("IN_PROGRESS", "WAITING_FOR_PARTS", "RESOLVED").contains(status)) {
            throw new IllegalArgumentException("Technicians may set a job to In Progress, Waiting for Parts, or Resolved.");
        }
        validateTechnicianTransition(issue.status(), status);
        return applyStatusUpdate(id, status, request.note(), userId);
    }

    public boolean isReportedBy(long issueId, long userId) { return issues.hasReporter(issueId, userId); }
    public boolean isAssignedTo(long issueId, long technicianId) { return issues.hasCurrentAssignment(issueId, technicianId); }

    private Issue applyStatusUpdate(long id, String status, String note, long authorId) {
        find(id);
        Set<Long> affectedTechnicians = new LinkedHashSet<>(issues.findCurrentTechnicianIds(id));
        issues.update(id, status, note, authorId);
        if (Set.of("RESOLVED", "CLOSED").contains(status)) issues.completeCurrentAssignments(id);
        affectedTechnicians.forEach(technicians::refreshAvailability);
        return find(id);
    }

    private void validateTechnicianTransition(String currentStatus, String requestedStatus) {
        if (Set.of("RESOLVED", "CLOSED").contains(currentStatus)) {
            throw new IllegalArgumentException("A completed issue cannot be updated by a technician.");
        }
        if (Set.of("OPEN", "TRIAGED", "ASSIGNED").contains(currentStatus) && !"IN_PROGRESS".equals(requestedStatus)) {
            throw new IllegalArgumentException("Accept the assignment before moving it to Waiting for Parts or Resolved.");
        }
    }

    private void verifyTechnicianAssignment(long issueId, long technicianId, long userId) {
        if (!users.isTechnicianForUser(technicianId, userId)) {
            throw new IllegalArgumentException("Technician identity is invalid.");
        }
        if (!issues.hasCurrentAssignment(issueId, technicianId)) {
            throw new IllegalArgumentException("This issue is assigned to another technician.");
        }
    }

    private int urgency(String severity) { return switch (severity.toUpperCase()) { case "CRITICAL" -> 4; case "HIGH" -> 3; case "MEDIUM" -> 2; default -> 1; }; }
    private long lookupId(String table, String value) { return lookupId(table, value, "name"); }
    private long lookupId(String table, String value, String column) {
        try {
            Long id = jdbc.queryForObject("SELECT id FROM " + table + " WHERE " + column + " = ?", Long.class, value);
            if (id == null) throw new ResourceNotFoundException(value + " was not found.");
            return id;
        } catch (EmptyResultDataAccessException ex) {
            throw new ResourceNotFoundException(value + " was not found.");
        }
    }

    private long lookupLocation(CreateIssueRequest request) {
        try {
            Long id;
            if (request.locationId() == null) {
                id = jdbc.queryForObject(
                        "SELECT id FROM locations WHERE building = ? AND floor_name = ? AND room_name = ?",
                        Long.class, request.building(), request.floor(), request.room());
            } else {
                id = jdbc.queryForObject(
                        "SELECT id FROM locations WHERE id = ? AND building = ? AND floor_name = ? AND room_name = ?",
                        Long.class, request.locationId(), request.building(), request.floor(), request.room());
            }
            if (id == null) throw new ResourceNotFoundException("Location was not found.");
            return id;
        } catch (EmptyResultDataAccessException ex) {
            throw new ResourceNotFoundException("Location was not found.");
        }
    }
}
