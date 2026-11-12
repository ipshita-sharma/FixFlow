package com.fixflow.service;

import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class SlaEngine {
    public LocalDateTime deadline(LocalDateTime createdAt, String severity) {
        int hours = switch (severity.toUpperCase()) { case "CRITICAL" -> 4; case "HIGH" -> 6; case "MEDIUM" -> 24; default -> 48; };
        return createdAt.plusHours(hours);
    }

    public String status(LocalDateTime deadline, String issueStatus) {
        if ("RESOLVED".equals(issueStatus) || "CLOSED".equals(issueStatus)) return "COMPLETE";
        long minutes = Duration.between(LocalDateTime.now(), deadline).toMinutes();
        if (minutes < 0) return "BREACHED";
        if (minutes <= 180) return "AT_RISK";
        return "SAFE";
    }
}
