package com.fixflow.dao;

import com.fixflow.model.Technician;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TechnicianDao {
    private final JdbcTemplate jdbc;
    public TechnicianDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    private final org.springframework.jdbc.core.RowMapper<Technician> mapper = (rs, row) -> new Technician(rs.getLong("id"), rs.getLong("user_id"), rs.getString("full_name"), rs.getString("availability_status"), rs.getInt("current_jobs"), rs.getInt("capacity_limit"), rs.getString("skills"));
    private String select() { return "SELECT t.id, t.user_id, u.full_name, t.availability_status, t.capacity_limit, COUNT(DISTINCT i.id) AS current_jobs, COALESCE(GROUP_CONCAT(s.name SEPARATOR ', '), '') AS skills FROM technicians t JOIN users u ON u.id = t.user_id JOIN roles r ON r.id = u.role_id AND r.name = 'TECHNICIAN' LEFT JOIN issue_assignments ia ON ia.technician_id = t.id AND ia.is_current = TRUE LEFT JOIN issues i ON i.id = ia.issue_id AND i.status IN ('ASSIGNED','IN_PROGRESS','WAITING_FOR_PARTS') LEFT JOIN technician_skills ts ON ts.technician_id = t.id LEFT JOIN skills s ON s.id = ts.skill_id WHERE u.is_active = TRUE GROUP BY t.id, t.user_id, u.full_name, t.availability_status, t.capacity_limit "; }
    public List<Technician> findAll() { return jdbc.query(select() + "ORDER BY current_jobs ASC", mapper); }
    public Optional<Technician> findById(long id) { return jdbc.query(select() + "HAVING t.id = ?", mapper, id).stream().findFirst(); }
    public Optional<Technician> findByUserId(long userId) { return jdbc.query(select() + "HAVING t.user_id = ?", mapper, userId).stream().findFirst(); }

    public void refreshAvailability(long technicianId) {
        jdbc.update("""
                UPDATE technicians t
                SET availability_status = CASE
                    WHEN availability_status = 'OFFLINE' THEN 'OFFLINE'
                    WHEN EXISTS (
                        SELECT 1
                        FROM issue_assignments ia
                        JOIN issues i ON i.id = ia.issue_id
                        WHERE ia.technician_id = t.id
                          AND ia.is_current = TRUE
                          AND i.status IN ('ASSIGNED','IN_PROGRESS','WAITING_FOR_PARTS')
                    ) THEN 'BUSY'
                    ELSE 'AVAILABLE'
                END
                WHERE t.id = ?
                """, technicianId);
    }

    public Optional<com.fixflow.model.TechnicianMetrics> findMetricsByUserId(long userId) {
        String sql = """
                SELECT COUNT(DISTINCT CASE WHEN i.status IN ('RESOLVED', 'CLOSED')
                                      AND i.resolved_at >= DATE_FORMAT(CURRENT_DATE, '%Y-%m-01')
                                      THEN i.id END) AS completed_this_month,
                       AVG(CASE WHEN i.status IN ('RESOLVED', 'CLOSED') AND i.resolved_at IS NOT NULL
                                THEN TIMESTAMPDIFF(MINUTE, i.created_at, i.resolved_at) / 60.0 END) AS average_resolution_hours,
                       CASE WHEN COUNT(DISTINCT CASE WHEN i.status IN ('RESOLVED', 'CLOSED')
                                                     AND i.resolved_at IS NOT NULL THEN i.id END) = 0 THEN NULL
                            ELSE ROUND(100 * COUNT(DISTINCT CASE WHEN i.status IN ('RESOLVED', 'CLOSED')
                                                                AND i.resolved_at <= i.sla_deadline THEN i.id END)
                                      / COUNT(DISTINCT CASE WHEN i.status IN ('RESOLVED', 'CLOSED')
                                                            AND i.resolved_at IS NOT NULL THEN i.id END), 0)
                       END AS on_time_rate
                FROM technicians t
                JOIN users u ON u.id = t.user_id
                JOIN roles r ON r.id = u.role_id AND r.name = 'TECHNICIAN'
                LEFT JOIN issue_assignments ia ON ia.technician_id = t.id
                LEFT JOIN issues i ON i.id = ia.issue_id
                WHERE t.user_id = ? AND u.is_active = TRUE
                GROUP BY t.id
                """;
        return jdbc.query(sql, (rs, row) -> {
            Object average = rs.getObject("average_resolution_hours");
            Object onTime = rs.getObject("on_time_rate");
            return new com.fixflow.model.TechnicianMetrics(
                    rs.getInt("completed_this_month"),
                    average instanceof Number number ? number.doubleValue() : null,
                    onTime instanceof Number number ? number.intValue() : null);
        }, userId).stream().findFirst();
    }
}
