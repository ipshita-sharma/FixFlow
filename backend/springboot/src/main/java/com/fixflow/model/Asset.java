package com.fixflow.model;

import java.time.LocalDate;

public record Asset(Long id, String assetCode, String assetName, String category, String location,
                    String condition, double healthScore, int failureCount, LocalDate lastMaintenance,
                    LocalDate warrantyEnd) { }
