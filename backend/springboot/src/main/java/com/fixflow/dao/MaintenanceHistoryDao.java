package com.fixflow.dao;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MaintenanceHistoryDao {
    private final JdbcTemplate jdbc;
    public MaintenanceHistoryDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public List<Map<String, Object>> findByAsset(long assetId) { return jdbc.queryForList("SELECT mh.maintenance_type, mh.notes, mh.duration_minutes, mh.performed_at, u.full_name AS technician FROM maintenance_history mh LEFT JOIN technicians t ON t.id = mh.technician_id LEFT JOIN users u ON u.id = t.user_id WHERE mh.asset_id = ? ORDER BY mh.performed_at DESC", assetId); }
}
