package com.fixflow.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fixflow.dao.IssueDao;
import com.fixflow.dao.TechnicianDao;
import com.fixflow.dao.UserDao;
import com.fixflow.dto.TechnicianUpdateRequest;
import com.fixflow.dto.UpdateIssueRequest;
import com.fixflow.exception.ForbiddenException;
import com.fixflow.model.Issue;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class IssueServiceTest {
    @Test
    void technicianCannotUpdateAnotherTechniciansIssue() {
        RecordingIssueDao issues = new RecordingIssueDao();
        IssueService service = service(issues, new RecordingUserDao(false), new RecordingTechnicianDao());

        assertThrows(IllegalArgumentException.class, () -> service.technicianUpdate(2L,
                new TechnicianUpdateRequest("RESOLVED", "Unauthorized update"), 2L, 99L));
        assertFalse(issues.updateCalled);
    }

    @Test
    void technicianCannotResolveBeforeAcceptingAssignment() {
        RecordingIssueDao issues = new RecordingIssueDao();
        issues.status = "ASSIGNED";
        IssueService service = service(issues, new RecordingUserDao(true), new RecordingTechnicianDao());

        assertThrows(IllegalArgumentException.class, () -> service.technicianUpdate(2L,
                new TechnicianUpdateRequest("RESOLVED", "Premature resolution"), 2L, 99L));
        assertFalse(issues.updateCalled);
    }

    @Test
    void nonAdminCannotAssignAnIssue() {
        RecordingIssueDao issues = new RecordingIssueDao();
        IssueService service = service(issues, new RecordingUserDao(false, false, false), new RecordingTechnicianDao());

        assertThrows(ForbiddenException.class, () -> service.assign(2L, 2L, 99L));
        assertFalse(issues.assignmentCalled);
    }

    @Test
    void administratorCanAssignARegisteredTechnician() {
        RecordingIssueDao issues = new RecordingIssueDao();
        IssueService service = service(issues, new RecordingUserDao(true, false, true), new RecordingTechnicianDao());

        service.assign(2L, 2L, 4L);

        assertTrue(issues.assignmentCalled);
    }

    @Test
    void resolvingIssueCompletesCurrentAssignmentAndRefreshesAvailability() {
        RecordingIssueDao issues = new RecordingIssueDao();
        RecordingTechnicianDao technicians = new RecordingTechnicianDao();

        service(issues, new RecordingUserDao(false), technicians)
                .update(2L, new UpdateIssueRequest("RESOLVED", "Fixed", null), 99L);

        assertTrue(issues.updateCalled);
        assertTrue(issues.completeAssignmentsCalled);
        assertEquals(List.of(2L), technicians.refreshedTechnicians);
    }

    @Test
    void reassignmentRefreshesPreviousAndNewTechnicianAvailability() {
        RecordingIssueDao issues = new RecordingIssueDao();
        RecordingTechnicianDao technicians = new RecordingTechnicianDao();

        service(issues, new RecordingUserDao(true, false, true), technicians)
                .assign(2L, 3L, 4L);

        assertEquals(List.of(2L, 3L), technicians.refreshedTechnicians);
    }

    @Test
    void acceptanceRefreshesTechnicianAvailability() {
        RecordingTechnicianDao technicians = new RecordingTechnicianDao();

        service(new RecordingIssueDao(), new RecordingUserDao(true), technicians)
                .accept(2L, 2L, 99L);

        assertEquals(List.of(2L), technicians.refreshedTechnicians);
    }

    private IssueService service(RecordingIssueDao issues, RecordingUserDao users, RecordingTechnicianDao technicians) {
        return new IssueService(issues, null, new PriorityEngine(), new SlaEngine(), users, technicians);
    }

    private static final class RecordingIssueDao extends IssueDao {
        private boolean updateCalled;
        private boolean assignmentCalled;
        private boolean completeAssignmentsCalled;
        private String status = "IN_PROGRESS";
        private List<Long> currentTechnicianIds = List.of(2L);

        private RecordingIssueDao() { super(null); }

        @Override
        public Optional<Issue> findById(long id) {
            return Optional.of(new Issue(id, "FF-TEST", "Test issue", "Test description", "IT / Network",
                    "Block A · Floor 1 · Classroom 101", null, "LOW", 5, "LOW", status,
                    LocalDateTime.now().plusHours(24), "Assigned technician", LocalDateTime.now()));
        }

        @Override
        public boolean hasCurrentAssignment(long issueId, long technicianId) { return true; }

        @Override
        public List<Long> findCurrentTechnicianIds(long issueId) { return currentTechnicianIds; }

        @Override
        public void update(long id, String status, String note, long authorId) { updateCalled = true; this.status = status; }

        @Override
        public void completeCurrentAssignments(long issueId) { completeAssignmentsCalled = true; }

        @Override
        public void addAssignment(long issueId, long technicianId, long assignedBy) { assignmentCalled = true; }

        @Override
        public void acceptAssignment(long issueId, long technicianId, long userId) { }
    }

    private static final class RecordingTechnicianDao extends TechnicianDao {
        private final List<Long> refreshedTechnicians = new java.util.ArrayList<>();

        private RecordingTechnicianDao() { super(null); }

        @Override
        public void refreshAvailability(long technicianId) { refreshedTechnicians.add(technicianId); }
    }

    private static final class RecordingUserDao extends UserDao {
        private final boolean technicianIdentity;
        private final boolean admin;
        private final boolean registeredTechnician;

        private RecordingUserDao(boolean technicianIdentity) {
            super(null);
            this.technicianIdentity = technicianIdentity;
            this.admin = false;
            this.registeredTechnician = false;
        }

        private RecordingUserDao(boolean admin, boolean technicianIdentity, boolean registeredTechnician) {
            super(null);
            this.admin = admin;
            this.technicianIdentity = technicianIdentity;
            this.registeredTechnician = registeredTechnician;
        }

        @Override
        public boolean isTechnicianForUser(long technicianId, long userId) { return technicianIdentity; }

        @Override
        public boolean isAdminUser(long userId) { return admin; }

        @Override
        public boolean isTechnician(long technicianId) { return registeredTechnician; }
    }
}
