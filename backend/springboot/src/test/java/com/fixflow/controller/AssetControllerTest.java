package com.fixflow.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fixflow.dao.TechnicianDao;
import com.fixflow.exception.GlobalExceptionHandler;
import com.fixflow.model.Asset;
import com.fixflow.model.Technician;
import com.fixflow.service.AssetService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AssetControllerTest {
    private RecordingAssetService assets;
    private RecordingTechnicianDao technicians;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        assets = new RecordingAssetService();
        technicians = new RecordingTechnicianDao(true);
        mvc = MockMvcBuilders.standaloneSetup(new AssetController(assets, technicians))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void unauthenticatedRequestCannotFallThroughToFullInventory() throws Exception {
        mvc.perform(get("/api/assets")).andExpect(status().isUnauthorized());

        assertFalse(assets.findAllCalled);
    }

    @Test
    void userCanViewInventoryForIssueReporting() throws Exception {
        mvc.perform(get("/api/assets")
                        .sessionAttr("userId", 3L)
                        .sessionAttr("role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].assetCode").value("HVAC-302"));

        assertTrue(assets.findAllCalled);
        assertEquals(0L, assets.technicianId);
    }

    @Test
    void adminCanViewInventory() throws Exception {
        mvc.perform(get("/api/assets")
                        .sessionAttr("userId", 4L)
                        .sessionAttr("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));

        assertTrue(assets.findAllCalled);
    }

    @Test
    void technicianUsesAuthenticatedProfileForAssignedAssets() throws Exception {
        mvc.perform(get("/api/assets")
                        .sessionAttr("userId", 12L)
                        .sessionAttr("role", "TECHNICIAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].assetCode").value("HVAC-302"));

        assertEquals(8L, assets.technicianId);
        assertTrue(!assets.findAllCalled);
        assertEquals(12L, technicians.requestedUserId);
    }

    @Test
    void technicianWithoutAnActiveProfileCannotViewAssets() throws Exception {
        technicians = new RecordingTechnicianDao(false);
        mvc = MockMvcBuilders.standaloneSetup(new AssetController(assets, technicians))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mvc.perform(get("/api/assets")
                        .sessionAttr("userId", 99L)
                        .sessionAttr("role", "TECHNICIAN"))
                .andExpect(status().isForbidden());
    }

    private static Asset asset() {
        return new Asset(1L, "HVAC-302", "Conference Room HVAC", "HVAC", "Block A · Floor 3",
                "Needs attention", 68, 1, LocalDate.of(2026, 7, 21), LocalDate.of(2026, 7, 31));
    }

    private static List<Asset> inventory() {
        return List.of(
                asset(),
                new Asset(2L, "PRN-14", "Finance printer", "Printing", "Block B · Floor 2", "Needs attention", 68, 1, null, null),
                new Asset(3L, "UPS-04", "Server room power backup", "Power", "Block B · Basement", "Under maintenance", 78, 0, null, null),
                new Asset(4L, "SRV-02", "Application server", "Server", "Block B · Basement", "Healthy", 94, 0, null, null));
    }

    private static final class RecordingAssetService extends AssetService {
        private boolean findAllCalled;
        private long technicianId;

        private RecordingAssetService() {
            super(null, null);
        }

        @Override
        public List<Asset> findAll() {
            findAllCalled = true;
            return inventory();
        }

        @Override
        public List<Asset> findAllForTechnician(long technicianId) {
            this.technicianId = technicianId;
            return List.of(asset());
        }

        @Override
        public Asset find(long id) {
            return asset();
        }

        @Override
        public Asset findForTechnician(long id, long technicianId) {
            this.technicianId = technicianId;
            return asset();
        }

        @Override
        public Map<String, Object> history(long id) {
            return Map.of("asset", asset(), "maintenance", List.of());
        }

        @Override
        public Map<String, Object> historyForTechnician(long id, long technicianId) {
            this.technicianId = technicianId;
            return Map.of("asset", asset(), "maintenance", List.of());
        }
    }

    private static final class RecordingTechnicianDao extends TechnicianDao {
        private final boolean profileAvailable;
        private long requestedUserId;

        private RecordingTechnicianDao(boolean profileAvailable) {
            super(null);
            this.profileAvailable = profileAvailable;
        }

        @Override
        public Optional<Technician> findByUserId(long userId) {
            requestedUserId = userId;
            return profileAvailable
                    ? Optional.of(new Technician(8L, userId, "Arjun Mehta", "AVAILABLE", 1, 10, "HVAC"))
                    : Optional.empty();
        }
    }
}
