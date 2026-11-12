package com.fixflow.controller;

import com.fixflow.dao.TechnicianDao;
import com.fixflow.exception.AuthenticationRequiredException;
import com.fixflow.exception.ForbiddenException;
import com.fixflow.model.Asset;
import com.fixflow.service.AssetService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assets")
public class AssetController {
    private final AssetService assets;
    private final TechnicianDao technicians;

    public AssetController(AssetService assets, TechnicianDao technicians) {
        this.assets = assets;
        this.technicians = technicians;
    }

    @GetMapping
    public List<Asset> list(HttpSession session) {
        String role = requireRole(session);
        if ("ADMIN".equals(role) || "USER".equals(role)) return assets.findAll();
        if ("TECHNICIAN".equals(role)) return assets.findAllForTechnician(technicianId(session));
        throw new ForbiddenException("This account cannot view assets.");
    }

    @GetMapping("/{id}")
    public Asset get(@PathVariable long id, HttpSession session) {
        String role = requireRole(session);
        if ("ADMIN".equals(role) || "USER".equals(role)) return assets.find(id);
        if ("TECHNICIAN".equals(role)) return assets.findForTechnician(id, technicianId(session));
        throw new ForbiddenException("This account cannot view assets.");
    }

    @GetMapping("/{id}/history")
    public Map<String, Object> history(@PathVariable long id, HttpSession session) {
        String role = requireRole(session);
        if ("ADMIN".equals(role) || "USER".equals(role)) return assets.history(id);
        if ("TECHNICIAN".equals(role)) return assets.historyForTechnician(id, technicianId(session));
        throw new ForbiddenException("This account cannot view assets.");
    }

    private long technicianId(HttpSession session) {
        long userId = requireUser(session);
        return technicians.findByUserId(userId)
                .orElseThrow(() -> new ForbiddenException("Technician profile was not found."))
                .id();
    }

    private String requireRole(HttpSession session) {
        requireUser(session);
        Object value = session.getAttribute("role");
        if (value == null) throw new AuthenticationRequiredException("Please log in to continue.");
        return value.toString().toUpperCase();
    }

    private long requireUser(HttpSession session) {
        Object value = session.getAttribute("userId");
        if (!(value instanceof Number)) throw new AuthenticationRequiredException("Please log in to continue.");
        return ((Number) value).longValue();
    }
}
