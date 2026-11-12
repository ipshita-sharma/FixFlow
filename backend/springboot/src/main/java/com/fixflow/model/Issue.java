package com.fixflow.model;

import java.time.LocalDateTime;

public record Issue(Long id, String issueNumber, String title, String description, String category,
                    String location, String assetCode, String severity, int priorityScore,
                    String priority, String status, LocalDateTime slaDeadline, String technician,
                    LocalDateTime createdAt) { }
