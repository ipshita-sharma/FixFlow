package com.fixflow.dao;

import com.fixflow.model.Asset;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AssetDao {
    private final JdbcTemplate jdbc;
    public AssetDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    private final org.springframework.jdbc.core.RowMapper<Asset> mapper = (rs, row) -> new Asset(rs.getLong("id"), rs.getString("asset_code"), rs.getString("asset_name"), rs.getString("category"), rs.getString("location"), rs.getString("condition_label"), rs.getDouble("health_score"), rs.getInt("failure_count"), rs.getDate("last_maintenance_date") == null ? null : rs.getDate("last_maintenance_date").toLocalDate(), rs.getDate("warranty_end") == null ? null : rs.getDate("warranty_end").toLocalDate());
    private String select() { return "SELECT a.id, a.asset_code, a.asset_name, ac.name AS category, CONCAT(l.building, ' · ', l.floor_name) AS location, CASE WHEN a.health_score < 70 THEN 'Needs attention' WHEN a.next_maintenance_date <= CURRENT_DATE + INTERVAL 7 DAY THEN 'Under maintenance' ELSE 'Healthy' END AS condition_label, a.health_score, (SELECT COUNT(*) FROM maintenance_history mh WHERE mh.asset_id = a.id AND mh.maintenance_type = 'CORRECTIVE') AS failure_count, a.last_maintenance_date, a.warranty_end FROM assets a JOIN asset_categories ac ON ac.id = a.category_id JOIN locations l ON l.id = a.location_id "; }
    private String assignedToTechnician() { return "WHERE EXISTS (SELECT 1 FROM issue_assignments ia JOIN issues i ON i.id = ia.issue_id WHERE i.asset_id = a.id AND ia.technician_id = ? AND ia.is_current = TRUE AND i.status IN ('ASSIGNED','IN_PROGRESS','WAITING_FOR_PARTS')) "; }
    public List<Asset> findAll() { return jdbc.query(select() + "ORDER BY a.health_score ASC, a.asset_code", mapper); }
    public List<Asset> findAllForTechnician(long technicianId) { return jdbc.query(select() + assignedToTechnician() + "ORDER BY a.health_score ASC, a.asset_code", mapper, technicianId); }
    public Optional<Asset> findById(long id) { return jdbc.query(select() + "WHERE a.id = ?", mapper, id).stream().findFirst(); }
    public Optional<Asset> findByIdForTechnician(long assetId, long technicianId) { return jdbc.query(select() + "WHERE a.id = ? AND EXISTS (SELECT 1 FROM issue_assignments ia JOIN issues i ON i.id = ia.issue_id WHERE i.asset_id = a.id AND ia.technician_id = ? AND ia.is_current = TRUE AND i.status IN ('ASSIGNED','IN_PROGRESS','WAITING_FOR_PARTS'))", mapper, assetId, technicianId).stream().findFirst(); }
}
