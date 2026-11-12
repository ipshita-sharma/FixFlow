package com.fixflow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class SlaEngineTest {
    private final SlaEngine engine = new SlaEngine();

    @Test
    void expiredDeadlineIsBreached() {
        assertEquals("BREACHED", engine.status(LocalDateTime.now().minusMinutes(2), "IN_PROGRESS"));
    }

    @Test
    void completedIssueIsComplete() {
        assertEquals("COMPLETE", engine.status(LocalDateTime.now().minusMinutes(2), "RESOLVED"));
    }
}
