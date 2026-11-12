package com.fixflow.controller;

import com.fixflow.service.DashboardService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpSession;
import com.fixflow.exception.AuthenticationRequiredException;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboard;
    public DashboardController(DashboardService dashboard) { this.dashboard = dashboard; }
    @GetMapping("/{role}") public Map<String, Object> summary(@PathVariable String role) { return dashboard.summary(role); }
    @GetMapping("/data") public Map<String, Object> data(HttpSession session) {
        Object value = session.getAttribute("userId");
        if (!(value instanceof Number)) throw new AuthenticationRequiredException("Please log in to continue.");
        return dashboard.data(((Number) value).longValue());
    }
    @GetMapping("/user") public Map<String, Object> user() { return dashboard.summary("USER"); }
    @GetMapping("/technician") public Map<String, Object> technician() { return dashboard.summary("TECHNICIAN"); }
    @GetMapping("/admin") public Map<String, Object> admin() { return dashboard.summary("ADMIN"); }
}
