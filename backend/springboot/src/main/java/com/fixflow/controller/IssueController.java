package com.fixflow.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpSession;

import com.fixflow.dao.UserDao;
import com.fixflow.dto.AssignmentRequest;
import com.fixflow.dto.CreateIssueRequest;
import com.fixflow.dto.TechnicianActionRequest;
import com.fixflow.dto.TechnicianUpdateRequest;
import com.fixflow.dto.UpdateIssueRequest;
import com.fixflow.model.Issue;
import com.fixflow.exception.AuthenticationRequiredException;
import com.fixflow.exception.ForbiddenException;
import com.fixflow.service.IssueService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/issues")
public class IssueController {
    private final IssueService service;
    private final UserDao users;
    public IssueController(IssueService service, UserDao users) { this.service = service; this.users = users; }

    @GetMapping
    public List<Issue> list(@RequestParam(required = false) String status, @RequestParam(required = false) String category, @RequestParam(required = false) Long technicianId, HttpSession session) {
        long userId = requireUser(session);
        String role = requireRole(session);
        if ("ADMIN".equals(role)) return service.findAll(status, category);
        if ("TECHNICIAN".equals(role)) return service.findAll(status, category, technicianFor(userId), null);
        if ("USER".equals(role)) return service.findAll(status, category, null, userId);
        throw new ForbiddenException("This account cannot view issues.");
    }

    @GetMapping("/{id}")
    public Issue get(@PathVariable long id, HttpSession session) {
        long userId = requireUser(session);
        String role = requireRole(session);
        Issue issue = service.find(id);
        if ("ADMIN".equals(role)) return issue;
        if ("USER".equals(role) && service.isReportedBy(id, userId)) return issue;
        if ("TECHNICIAN".equals(role) && service.isAssignedTo(id, technicianFor(userId))) return issue;
        throw new ForbiddenException("You are not allowed to view this issue.");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Issue create(@Valid @RequestBody CreateIssueRequest request, HttpSession session) {
        String role = requireRole(session);
        if (!"USER".equals(role) && !"TECHNICIAN".equals(role)) throw new ForbiddenException("Only members can report issues.");
        return service.create(request, requireUser(session));
    }

    @PutMapping("/{id}")
    public Issue update(@PathVariable long id, @Valid @RequestBody UpdateIssueRequest request, HttpSession session) {
        return updateForActor(id, request.status(), request.note(), session);
    }

    @PostMapping("/{id}/accept")
    public Issue accept(@PathVariable long id, @Valid @RequestBody TechnicianActionRequest ignored, HttpSession session) {
        long userId = requireRole(session, "TECHNICIAN");
        return service.accept(id, technicianFor(userId), userId);
    }

    @PutMapping("/{id}/technician-update")
    public Issue technicianUpdate(@PathVariable long id, @Valid @RequestBody TechnicianUpdateRequest request, HttpSession session) {
        long userId = requireRole(session, "TECHNICIAN");
        return service.technicianUpdate(id, request, technicianFor(userId), userId);
    }

    @PostMapping("/{id}/updates")
    public Issue addUpdate(@PathVariable long id, @Valid @RequestBody UpdateIssueRequest request, HttpSession session) {
        return updateForActor(id, request.status(), request.note(), session);
    }

    @PostMapping("/{id}/assign")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assign(@PathVariable long id, @Valid @RequestBody AssignmentRequest request, HttpSession session) {
        service.assign(id, request.technicianId(), requireRole(session, "ADMIN"));
    }

    @PutMapping("/{id}/reassign")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reassign(@PathVariable long id, @Valid @RequestBody AssignmentRequest request, HttpSession session) {
        service.assign(id, request.technicianId(), requireRole(session, "ADMIN"));
    }

    private Issue updateForActor(long id, String status, String note, HttpSession session) {
        long userId = requireUser(session);
        String role = requireRole(session);
        if ("ADMIN".equals(role)) return service.update(id, new UpdateIssueRequest(status, note, null), userId);
        if ("TECHNICIAN".equals(role)) return service.technicianUpdate(id, new TechnicianUpdateRequest(status, note), technicianFor(userId), userId);
        throw new ForbiddenException("Users cannot modify issue progress.");
    }

    private long technicianFor(long userId) {
        return users.findTechnicianIdForUser(userId).orElseThrow(() -> new ForbiddenException("Technician profile was not found."));
    }

    private long requireRole(HttpSession session, String expectedRole) {
        long userId = requireUser(session);
        if (!expectedRole.equals(requireRole(session))) throw new ForbiddenException("Only " + expectedRole.toLowerCase() + " accounts can perform this action.");
        return userId;
    }

    private long requireUser(HttpSession session) {
        Object value = session.getAttribute("userId");
        if (!(value instanceof Number)) throw new AuthenticationRequiredException("Please log in to continue.");
        return ((Number) value).longValue();
    }

    private String requireRole(HttpSession session) {
        Object value = session.getAttribute("role");
        if (value == null) throw new AuthenticationRequiredException("Please log in to continue.");
        return value.toString().toUpperCase();
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable long id) { throw new IllegalArgumentException("Issue deletion is intentionally disabled for audit safety; close the issue instead."); }
}
