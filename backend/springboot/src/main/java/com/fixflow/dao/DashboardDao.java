package com.fixflow.dao;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardDao {
    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);
    private final JdbcTemplate jdbc;

    public DashboardDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Map<String, Object> summary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("openIssues", count("SELECT COUNT(*) FROM issues WHERE status NOT IN ('RESOLVED','CLOSED')"));
        summary.put("inProgress", count("SELECT COUNT(*) FROM issues WHERE status = 'IN_PROGRESS'"));
        summary.put("urgentAttention", count("SELECT COUNT(*) FROM issues WHERE priority IN ('CRITICAL','HIGH') AND status NOT IN ('RESOLVED','CLOSED')"));
        summary.put("criticalIssues", count("SELECT COUNT(*) FROM issues WHERE priority = 'CRITICAL' AND status NOT IN ('RESOLVED','CLOSED')"));
        summary.put("activeRepairs", count("SELECT COUNT(*) FROM issues WHERE status IN ('ASSIGNED','IN_PROGRESS','WAITING_FOR_PARTS')"));

        LocalDate month = LocalDate.now(APP_ZONE).withDayOfMonth(1);
        Timestamp monthStart = timestamp(month);
        Timestamp nextMonth = timestamp(month.plusMonths(1));
        summary.put("resolvedThisMonth", jdbc.queryForObject(
                "SELECT COUNT(*) FROM issues WHERE resolved_at >= ? AND resolved_at < ?",
                Long.class, monthStart, nextMonth));
        summary.put("slaCompliance", jdbc.queryForObject(
                "SELECT COALESCE(ROUND(AVG(CASE WHEN status IN ('RESOLVED','CLOSED') THEN CASE WHEN resolved_at <= sla_deadline THEN 100 ELSE 0 END END), 0), 0) FROM issues",
                Integer.class));
        return summary;
    }

    public long activeAssets() {
        return count("SELECT COUNT(*) FROM assets");
    }

    public List<Map<String, Object>> activity(int limit) {
        String sql = """
                SELECT event_type, issue_number, title, details, actor,
                       DATE_FORMAT(event_time, '%Y-%m-%dT%H:%i:%s+05:30') AS event_time
                FROM (
                    SELECT 'CREATED' AS event_type, i.issue_number,
                           'Issue reported' AS title,
                           CONCAT(i.issue_number, ' · ', i.title) AS details,
                           reporter.full_name AS actor, i.created_at AS event_time
                    FROM issues i
                    JOIN users reporter ON reporter.id = i.reporter_id
                    UNION ALL
                    SELECT 'ASSIGNED' AS event_type, i.issue_number,
                           CONCAT('Assigned to ', technician_user.full_name) AS title,
                           CONCAT(i.issue_number, ' was assigned to ', technician_user.full_name, '.') AS details,
                           assigner.full_name AS actor, ia.assigned_at AS event_time
                    FROM issue_assignments ia
                    JOIN issues i ON i.id = ia.issue_id
                    JOIN technicians technician ON technician.id = ia.technician_id
                    JOIN users technician_user ON technician_user.id = technician.user_id
                    JOIN users assigner ON assigner.id = ia.assigned_by
                    UNION ALL
                    SELECT 'STATUS' AS event_type, i.issue_number,
                           CONCAT('Status changed to ', iu.status) AS title,
                           COALESCE(iu.note, 'Issue status updated.') AS details,
                           author.full_name AS actor, iu.created_at AS event_time
                    FROM issue_updates iu
                    JOIN issues i ON i.id = iu.issue_id
                    JOIN users author ON author.id = iu.author_id
                ) events
                ORDER BY event_time DESC
                LIMIT ?
                """;
        return jdbc.query(sql, (rs, row) -> {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("type", rs.getString("event_type"));
            event.put("issueNumber", rs.getString("issue_number"));
            event.put("title", rs.getString("title"));
            event.put("details", rs.getString("details"));
            event.put("actor", rs.getString("actor"));
            event.put("occurredAt", rs.getString("event_time"));
            return event;
        }, limit);
    }

    public List<Map<String, Object>> notifications(Long userId) {
        List<Map<String, Object>> result = new ArrayList<>();
        String userFilter = userId == null ? "" : " AND n.user_id = ?";
        String storedSql = """
                SELECT n.id, n.title, n.message, n.notification_type AS type,
                       n.is_read AS isRead,
                       DATE_FORMAT(n.created_at, '%Y-%m-%dT%H:%i:%s+05:30') AS created_at
                FROM notifications n
                LEFT JOIN issues i ON i.id = n.issue_id
                WHERE (n.notification_type <> 'WARNING'
                       OR i.status IS NULL OR i.status NOT IN ('RESOLVED','CLOSED'))
                """ + userFilter + " ORDER BY n.created_at DESC LIMIT 10";
        if (userId == null) {
            result.addAll(mapNotifications(jdbc.queryForList(storedSql)));
        } else {
            result.addAll(mapNotifications(jdbc.queryForList(storedSql, userId)));
        }

        String slaSql = """
                SELECT NULL AS id, 'SLA watch' AS title,
                       CASE WHEN TIMESTAMPDIFF(MINUTE, CURRENT_TIMESTAMP, i.sla_deadline) < 0
                            THEN CONCAT(i.issue_number, ' has breached its SLA.')
                            ELSE CONCAT(i.issue_number, ' has ',
                                        FLOOR(TIMESTAMPDIFF(MINUTE, CURRENT_TIMESTAMP, i.sla_deadline) / 60),
                                        'h ', MOD(TIMESTAMPDIFF(MINUTE, CURRENT_TIMESTAMP, i.sla_deadline), 60),
                                        'm remaining.') END AS message,
                       CASE WHEN TIMESTAMPDIFF(MINUTE, CURRENT_TIMESTAMP, i.sla_deadline) < 0
                            THEN 'CRITICAL' ELSE 'WARNING' END AS type,
                       FALSE AS isRead,
                       DATE_FORMAT(CURRENT_TIMESTAMP, '%Y-%m-%dT%H:%i:%s+05:30') AS created_at
                FROM issues i
                WHERE i.status NOT IN ('RESOLVED','CLOSED')
                  AND TIMESTAMPDIFF(MINUTE, CURRENT_TIMESTAMP, i.sla_deadline) <= 180
                ORDER BY i.sla_deadline ASC
                LIMIT 10
                """;
        result.addAll(mapNotifications(jdbc.queryForList(slaSql)));
        result.sort(Comparator.comparing(item -> String.valueOf(item.get("createdAt")), Comparator.reverseOrder()));
        return result.stream().limit(10).toList();
    }

    public List<Map<String, Object>> issueVolume() {
        List<Map<String, Object>> volume = new ArrayList<>();
        LocalDate today = LocalDate.now(APP_ZONE);
        for (int daysAgo = 6; daysAgo >= 0; daysAgo--) {
            LocalDate day = today.minusDays(daysAgo);
            long count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM issues WHERE created_at >= ? AND created_at < ?",
                    Long.class, timestamp(day), timestamp(day.plusDays(1)));
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", day.toString());
            point.put("label", day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + DATE_FORMAT.format(day));
            point.put("count", count);
            volume.add(point);
        }
        return volume;
    }

    private List<Map<String, Object>> mapNotifications(List<Map<String, Object>> rows) {
        List<Map<String, Object>> mapped = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> notification = new LinkedHashMap<>();
            notification.put("id", row.get("id"));
            notification.put("title", row.get("title"));
            notification.put("message", row.get("message"));
            notification.put("type", row.get("type"));
            notification.put("isRead", row.get("isRead"));
            notification.put("createdAt", formatTemporal(row.get("created_at")));
            mapped.add(notification);
        }
        return mapped;
    }

    private long count(String sql) { return jdbc.queryForObject(sql, Long.class); }

    private Timestamp timestamp(LocalDate date) {
        return Timestamp.from(date.atStartOfDay(APP_ZONE).toInstant());
    }

    private String formatTemporal(Object value) {
        if (value == null) return null;
        if (value instanceof Timestamp timestamp) return timestamp.toInstant().atZone(APP_ZONE).toOffsetDateTime().toString();
        if (value instanceof LocalDateTime dateTime) return dateTime.atZone(APP_ZONE).toOffsetDateTime().toString();
        return String.valueOf(value);
    }
}
