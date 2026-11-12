package com.fixflow.service;

import com.fixflow.dao.UserDao;
import com.fixflow.dto.AuthRequest;
import com.fixflow.dto.RegisterRequest;
import com.fixflow.exception.ConflictException;
import com.fixflow.exception.ResourceNotFoundException;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserDao users;
    private final PasswordService passwords;
    public AuthService(UserDao users, PasswordService passwords) { this.users = users; this.passwords = passwords; }

    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        String role = request.role().trim().toUpperCase(Locale.ROOT);
        if ("ADMIN".equals(role)) {
            throw new IllegalArgumentException("Administrator accounts must be provisioned through an authorized administrator workflow.");
        }
        if (!Set.of("USER", "TECHNICIAN").contains(role)) {
            throw new IllegalArgumentException("Unsupported registration role. Choose User or Technician.");
        }

        try {
            long id = users.create(request.fullName(), request.email(), request.phone(), request.organization(), role, passwords.hash(request.password()));
            if ("TECHNICIAN".equals(role)) users.createTechnicianProfile(id);
            return Map.of("id", id, "fullName", request.fullName(), "email", request.email(), "role", role);
        } catch (DuplicateKeyException ex) {
            throw new ConflictException("An account with that email is already registered.");
        }
    }

    public Map<String, Object> login(AuthRequest request) {
        Map<String, Object> user = users.findByEmail(request.email()).orElseThrow(() -> new ResourceNotFoundException("Invalid email or password."));
        if (!passwords.matches(request.password(), (String) user.get("passwordHash"))) throw new IllegalArgumentException("Invalid email or password.");
        return Map.of("id", user.get("id"), "fullName", user.get("fullName"), "email", user.get("email"), "role", user.get("role"));
    }
}
