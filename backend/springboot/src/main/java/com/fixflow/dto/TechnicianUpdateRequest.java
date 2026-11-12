package com.fixflow.dto;

import jakarta.validation.constraints.NotBlank;
public record TechnicianUpdateRequest(@NotBlank String status, String note) { }
