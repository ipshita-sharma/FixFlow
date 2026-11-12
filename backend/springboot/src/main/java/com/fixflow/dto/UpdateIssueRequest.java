package com.fixflow.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateIssueRequest(@NotBlank String status, String note, Long authorId) { }
