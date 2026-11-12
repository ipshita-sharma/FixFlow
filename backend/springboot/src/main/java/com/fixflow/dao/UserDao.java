package com.fixflow.dao;

import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao {
    private final JdbcTemplate jdbc;
    public UserDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<Map<String, Object>> findByEmail(String email) {
        return jdbc.query("SELECT u.id, u.full_name, u.email, u.password_hash, r.name AS role FROM users u JOIN roles r ON r.id = u.role_id WHERE u.email = ? AND u.is_active = TRUE", ps -> ps.setString(1, email), rs -> {
            if (!rs.next()) return Optional.empty();
            return Optional.of(Map.of("id", rs.getLong("id"), "fullName", rs.getString("full_name"), "email", rs.getString("email"), "passwordHash", rs.getString("password_hash"), "role", rs.getString("role")));
        });
    }

    public Optional<Map<String, Object>> findByEmailForPasswordReset(String email) {
        return jdbc.query("SELECT u.id, u.email, u.is_active, r.name AS role FROM users u JOIN roles r ON r.id = u.role_id WHERE u.email = ?", ps -> ps.setString(1, email), rs -> {
            if (!rs.next()) return Optional.empty();
            return Optional.of(Map.of("id", rs.getLong("id"), "email", rs.getString("email"), "active", rs.getBoolean("is_active"), "role", rs.getString("role")));
        });
    }

    public Optional<Map<String, Object>> findPublicById(long id) {
        return jdbc.query("SELECT u.id, u.full_name, u.email, r.name AS role FROM users u JOIN roles r ON r.id = u.role_id WHERE u.id = ? AND u.is_active = TRUE", ps -> ps.setLong(1, id), rs -> {
            if (!rs.next()) return Optional.empty();
            return Optional.of(Map.of("id", rs.getLong("id"), "fullName", rs.getString("full_name"), "email", rs.getString("email"), "role", rs.getString("role")));
        });
    }

    public long create(String fullName, String email, String phone, String organization, String role, String passwordHash) {
        Long roleId = jdbc.queryForObject("SELECT id FROM roles WHERE name = ?", Long.class, role.toUpperCase());
        jdbc.update("INSERT INTO users (role_id, full_name, email, phone, organization, password_hash) VALUES (?, ?, ?, ?, ?, ?)", roleId, fullName, email, phone, organization, passwordHash);
        return jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
    }

    public void updatePasswordHash(String email, String passwordHash) {
        int updated = jdbc.update("UPDATE users SET password_hash = ? WHERE email = ? AND is_active = TRUE", passwordHash, email);
        if (updated != 1) throw new IllegalStateException("The active account could not be updated: " + email);
    }

    public void createTechnicianProfile(long userId) {
        jdbc.update("INSERT INTO technicians (user_id) VALUES (?)", userId);
    }

    public boolean isTechnicianForUser(long technicianId, long userId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM technicians WHERE id = ? AND user_id = ?", Integer.class, technicianId, userId);
        return count != null && count > 0;
    }

    public Optional<Long> findTechnicianIdForUser(long userId) {
        return jdbc.query("SELECT id FROM technicians WHERE user_id = ?", ps -> ps.setLong(1, userId), rs -> {
            if (!rs.next()) return Optional.empty();
            return Optional.of(rs.getLong("id"));
        });
    }

    public boolean isTechnician(long technicianId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM technicians WHERE id = ?", Integer.class, technicianId);
        return count != null && count > 0;
    }

    public boolean isAdminUser(long userId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM users u JOIN roles r ON r.id = u.role_id WHERE u.id = ? AND u.is_active = TRUE AND r.name = 'ADMIN'", Integer.class, userId);
        return count != null && count > 0;
    }
}
