package com.fixflow.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fixflow.model.Asset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class AssetDaoTest {
    @Test
    void technicianAssetQueryUsesCurrentActiveAssignmentsAndWorkflowStatuses() {
        RecordingJdbcTemplate jdbc = new RecordingJdbcTemplate();

        assertEquals(1, new AssetDao(jdbc).findAllForTechnician(8L).size());
        assertTrue(jdbc.sql.contains("issue_assignments ia"));
        assertTrue(jdbc.sql.contains("ia.technician_id = ?"));
        assertTrue(jdbc.sql.contains("ia.is_current = TRUE"));
        assertTrue(jdbc.sql.contains("i.status IN ('ASSIGNED','IN_PROGRESS','WAITING_FOR_PARTS')"));
    }

    @Test
    void technicianAssetLookupBindsAssetAndTechnicianIds() {
        RecordingJdbcTemplate jdbc = new RecordingJdbcTemplate();

        new AssetDao(jdbc).findByIdForTechnician(4L, 8L);

        assertEquals("SELECT", jdbc.sql.substring(0, 6));
        assertTrue(jdbc.sql.contains("a.id = ? AND EXISTS"));
        assertTrue(jdbc.sql.contains("ia.technician_id = ?"));
        assertEquals(List.of(4L, 8L), jdbc.args);
    }

    private static final class RecordingJdbcTemplate extends JdbcTemplate {
        private String sql;
        private List<Object> args = List.of();

        @SuppressWarnings("unchecked")
        @Override
        public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
            this.sql = sql;
            return List.of((T) sample());
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
            this.sql = sql;
            this.args = List.of(args);
            return List.of((T) sample());
        }

        private static Asset sample() {
            return new Asset(1L, "HVAC-302", "Conference Room HVAC", "HVAC", "Block A · Floor 3",
                    "Needs attention", 68, 1, null, null);
        }
    }
}
