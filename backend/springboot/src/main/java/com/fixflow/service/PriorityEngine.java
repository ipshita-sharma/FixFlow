package com.fixflow.service;

import org.springframework.stereotype.Service;

@Service
public class PriorityEngine {
    public int score(String severity, int impact, int affectedUsers, String assetCriticality, int slaUrgency) {
        return severityWeight(severity) + Math.min(impact, 4) + Math.min(affectedUsers / 10, 4) + criticalityWeight(assetCriticality) + slaUrgency;
    }

    public String label(int score) {
        if (score >= 16) return "CRITICAL";
        if (score >= 11) return "HIGH";
        if (score >= 7) return "MEDIUM";
        return "LOW";
    }

    private int severityWeight(String value) { return switch (value.toUpperCase()) { case "CRITICAL" -> 8; case "HIGH" -> 6; case "MEDIUM" -> 4; default -> 2; }; }
    private int criticalityWeight(String value) { return switch (value == null ? "MEDIUM" : value.toUpperCase()) { case "CRITICAL" -> 4; case "HIGH" -> 3; case "LOW" -> 1; default -> 2; }; }
}
