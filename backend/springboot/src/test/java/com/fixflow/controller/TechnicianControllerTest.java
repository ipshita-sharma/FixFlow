package com.fixflow.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fixflow.dao.TechnicianDao;
import com.fixflow.exception.GlobalExceptionHandler;
import com.fixflow.model.Technician;
import com.fixflow.model.TechnicianMetrics;

class TechnicianControllerTest {
    private TechnicianDao technicians;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        technicians = new RecordingTechnicianDao(true);
        mvc = mvcFor(technicians);
    }

    private MockMvc mvcFor(TechnicianDao dao) {
        TechnicianController controller = new TechnicianController(dao, null);
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void unauthenticatedRequestReturns401() throws Exception {
        mvc.perform(get("/api/technicians"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void regularUserRequestReturns403() throws Exception {
        mvc.perform(get("/api/technicians")
                        .sessionAttr("userId", 1L)
                        .sessionAttr("role", "USER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void technicianRequestReturns403() throws Exception {
        mvc.perform(get("/api/technicians")
                        .sessionAttr("userId", 2L)
                        .sessionAttr("role", "TECHNICIAN"))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorRequestReturnsExistingTechnicianList() throws Exception {
        mvc.perform(get("/api/technicians")
                        .sessionAttr("userId", 4L)
                        .sessionAttr("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].userId").value(12))
                .andExpect(jsonPath("$[0].fullName").value("Arjun Mehta"));

        org.junit.jupiter.api.Assertions.assertTrue(((RecordingTechnicianDao) technicians).findAllCalled);
    }

    @Test
    void technicianCanLoadOwnProfileAndMetrics() throws Exception {
        mvc.perform(get("/api/technicians/me")
                        .sessionAttr("userId", 12L)
                        .sessionAttr("role", "TECHNICIAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.userId").value(12));

        mvc.perform(get("/api/technicians/me/metrics")
                        .sessionAttr("userId", 12L)
                        .sessionAttr("role", "TECHNICIAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedThisMonth").value(0));
    }

    @Test
    void unauthenticatedAndNonTechnicianUsersCannotLoadTechnicianProfile() throws Exception {
        mvc.perform(get("/api/technicians/me"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/technicians/me")
                        .sessionAttr("userId", 4L)
                        .sessionAttr("role", "ADMIN"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/technicians/me")
                        .sessionAttr("userId", 1L)
                        .sessionAttr("role", "USER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void technicianWithoutProfileGetsForbiddenResponse() throws Exception {
        mvc = mvcFor(new RecordingTechnicianDao(false));

        mvc.perform(get("/api/technicians/me")
                        .sessionAttr("userId", 99L)
                        .sessionAttr("role", "TECHNICIAN"))
                .andExpect(status().isForbidden());
    }

    private static final class RecordingTechnicianDao extends TechnicianDao {
        private boolean findAllCalled;
        private final boolean profileAvailable;

        private RecordingTechnicianDao(boolean profileAvailable) {
            super(null);
            this.profileAvailable = profileAvailable;
        }

        @Override
        public List<Technician> findAll() {
            findAllCalled = true;
            return List.of(new Technician(7L, 12L, "Arjun Mehta", "AVAILABLE", 0, 5, "HVAC"));
        }

        @Override
        public Optional<Technician> findByUserId(long userId) {
            return profileAvailable
                    ? Optional.of(new Technician(7L, userId, "Arjun Mehta", "AVAILABLE", 0, 5, "HVAC"))
                    : Optional.empty();
        }

        @Override
        public Optional<TechnicianMetrics> findMetricsByUserId(long userId) {
            return profileAvailable ? Optional.of(new TechnicianMetrics(0, null, null)) : Optional.empty();
        }
    }
}
