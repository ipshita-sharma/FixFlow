package com.fixflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(@NotBlank String fullName, @NotBlank @Email String email,
                              String phone, @NotBlank String organization, @NotBlank String role,
                              @NotBlank String password) { }
