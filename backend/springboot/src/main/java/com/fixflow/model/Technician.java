package com.fixflow.model;

public record Technician(Long id, Long userId, String fullName, String availabilityStatus,
                         int currentJobs, int capacityLimit, String skills) { }
