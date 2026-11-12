package com.fixflow.service;

import java.util.Map;
import org.springframework.stereotype.Service;

import com.fixflow.dao.DashboardDao;
import com.fixflow.dao.IssueDao;
import com.fixflow.dao.UserDao;

@Service
public class DashboardService {
    private final DashboardDao dashboard;
    private final IssueDao issues;
    private final UserDao users;

    public DashboardService(DashboardDao dashboard, IssueDao issues, UserDao users) {
        this.dashboard = dashboard;
        this.issues = issues;
        this.users = users;
    }

    public Map<String, Object> summary(String role) {
        Map<String, Object> result = new java.util.LinkedHashMap<>(dashboard.summary());
        result.put("role", role.toUpperCase());
        result.put("activeAssets", dashboard.activeAssets());
        return result;
    }

    public Map<String, Object> data(Long userId) {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("user", userId == null ? null : users.findPublicById(userId).orElse(null));
        result.put("summary", dashboard.summary());
        result.put("recentIssues", issues.findRecent(8));
        result.put("activity", dashboard.activity(8));
        result.put("notifications", dashboard.notifications(userId));
        result.put("volume", dashboard.issueVolume());
        return result;
    }

}
