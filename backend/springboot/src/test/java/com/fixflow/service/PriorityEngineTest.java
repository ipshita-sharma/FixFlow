package com.fixflow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class PriorityEngineTest {
    private final PriorityEngine engine = new PriorityEngine();

    @Test
    void criticalInputsBecomeCritical() {
        int score = engine.score("CRITICAL", 4, 35, "CRITICAL", 4);
        assertEquals("CRITICAL", engine.label(score));
    }

    @Test
    void lowInputsStayLow() {
        int score = engine.score("LOW", 1, 1, "LOW", 1);
        assertEquals("LOW", engine.label(score));
    }
}
