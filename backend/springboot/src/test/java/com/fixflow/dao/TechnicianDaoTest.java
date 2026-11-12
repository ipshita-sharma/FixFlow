package com.fixflow.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.fixflow.model.Technician;
import com.fixflow.model.TechnicianMetrics;

class TechnicianDaoTest {
    @Test
    void findAllKeepsActiveTechniciansAndExcludesInactiveUsersInSql() {
        RecordingJdbcTemplate jdbc = new RecordingJdbcTemplate();
        Technician active = new Technician(5L, 22L, "Alexa", "AVAILABLE", 0, 10, "");

        assertEquals(List.of(active), new TechnicianDao(jdbc).findAll());
        assertTrue(jdbc.sql.contains("JOIN roles r ON r.id = u.role_id AND r.name = 'TECHNICIAN'"));
        assertTrue(jdbc.sql.contains("WHERE u.is_active = TRUE"));
        assertTrue(jdbc.sql.contains("COUNT(DISTINCT i.id) AS current_jobs"));
        assertTrue(jdbc.sql.contains("i.status IN ('ASSIGNED','IN_PROGRESS','WAITING_FOR_PARTS')"));
    }

    @Test
    void findByUserIdUsesTheSameActiveTechnicianQuery() {
        RecordingJdbcTemplate jdbc = new RecordingJdbcTemplate();
        Optional<Technician> profile = new TechnicianDao(jdbc).findByUserId(22L);

        assertEquals("Alexa", profile.orElseThrow().fullName());
        assertTrue(jdbc.sql.contains("HAVING t.user_id = ?"));
        assertTrue(jdbc.sql.contains("r.name = 'TECHNICIAN'"));
        assertTrue(jdbc.sql.contains("u.is_active = TRUE"));
    }

    @Test
    void refreshAvailabilityPreservesOfflineAndUsesActiveWorkStatuses() {
        RecordingJdbcTemplate jdbc = new RecordingJdbcTemplate();

        new TechnicianDao(jdbc).refreshAvailability(22L);

        assertTrue(jdbc.updateSql.contains("WHEN availability_status = 'OFFLINE' THEN 'OFFLINE'"));
        assertTrue(jdbc.updateSql.contains("i.status IN ('ASSIGNED','IN_PROGRESS','WAITING_FOR_PARTS')"));
        assertTrue(jdbc.updateSql.contains("ELSE 'AVAILABLE'"));
    }

    private static final class RecordingJdbcTemplate extends JdbcTemplate {
        private String sql;
        private String updateSql;

        @Override
        public int update(String sql, Object... args) {
            this.updateSql = sql;
            return 1;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
            this.sql = sql;
            Technician active = new Technician(5L, 22L, "Alexa", "AVAILABLE", 0, 10, "");
            return List.of((T) active);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
            this.sql = sql;
            if (sql.contains("average_resolution_hours")) {
                TechnicianMetrics metrics = new TechnicianMetrics(0, null, null);
                return List.of((T) metrics);
            }
            Technician active = new Technician(5L, 22L, "Alexa", "AVAILABLE", 0, 10, "");
            return List.of((T) active);
        }
    }
}
