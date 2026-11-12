package com.fixflow.dao;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationDao {
    private final JdbcTemplate jdbc;
    public NotificationDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public List<Map<String, Object>> findForUser(long userId) { return jdbc.queryForList("SELECT id, title, message, notification_type AS type, is_read, created_at FROM notifications WHERE user_id = ? ORDER BY created_at DESC", userId); }
    public void markRead(long id, long userId) { jdbc.update("UPDATE notifications SET is_read = TRUE WHERE id = ? AND user_id = ?", id, userId); }
}
