package com.fixflow.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateIssueRequest(@NotBlank String title, @NotBlank String description,
                                 @NotBlank String category, @NotBlank String severity,
                                 @NotBlank String building, @NotBlank String floor,
                                 @NotBlank String room, String assetCode,
                                 @Min(1) int impactScore, @Min(1) int affectedUsers,
                                 Long reporterId, Long locationId) { }
