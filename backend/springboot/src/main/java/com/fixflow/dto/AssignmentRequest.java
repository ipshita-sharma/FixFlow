package com.fixflow.dto;

import jakarta.validation.constraints.NotNull;

public record AssignmentRequest(@NotNull Long technicianId, Long assignedBy) { }
