package com.fixflow.controller;

import com.fixflow.exception.AuthenticationRequiredException;
import com.fixflow.exception.ForbiddenException;
import com.fixflow.model.Technician;
import com.fixflow.model.TechnicianMetrics;
import com.fixflow.service.IssueService;
import com.fixflow.dao.TechnicianDao;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/technicians")
public class TechnicianController {
    private final TechnicianDao technicians;
    public TechnicianController(TechnicianDao technicians, IssueService ignored) { this.technicians = technicians; }
    @GetMapping
    public List<Technician> list(HttpSession session) {
        requireRole(session, "ADMIN");
        return technicians.findAll();
    }
    @GetMapping("/me")
    public Technician me(HttpSession session) {
        long userId = requireRole(session, "TECHNICIAN");
        return technicians.findByUserId(userId).orElseThrow(() -> new ForbiddenException("Technician profile was not found."));
    }
    @GetMapping("/me/metrics")
    public TechnicianMetrics metrics(HttpSession session) {
        long userId = requireRole(session, "TECHNICIAN");
        return technicians.findMetricsByUserId(userId).orElseThrow(() -> new ForbiddenException("Technician profile was not found."));
    }
    @GetMapping("/{id}") public Technician get(@PathVariable long id) { return technicians.findById(id).orElseThrow(() -> new IllegalArgumentException("Technician was not found.")); }
    @GetMapping("/{id}/workload") public Technician workload(@PathVariable long id) { return get(id); }

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
}
