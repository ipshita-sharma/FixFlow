package com.fixflow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fixflow.dao.UserDao;
import com.fixflow.dto.RegisterRequest;
import com.fixflow.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

class AuthServiceTest {
    private final RecordingUserDao users = new RecordingUserDao();
    private final FixedPasswordService passwords = new FixedPasswordService();
    private final AuthService auth = new AuthService(users, passwords);

    @Test
    void publicRegistrationRejectsAdministratorRoleBeforeDatabaseWrite() {
        RegisterRequest request = request("ADMIN");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> auth.register(request));

        assertEquals("Administrator accounts must be provisioned through an authorized administrator workflow.", error.getMessage());
        assertEquals(false, users.createCalled);
    }

    @Test
    void publicRegistrationRejectsUnknownRoleBeforeDatabaseWrite() {
        assertThrows(IllegalArgumentException.class, () -> auth.register(request("MANAGER")));
        assertEquals(false, users.createCalled);
    }

    @Test
    void technicianRegistrationCreatesTechnicianProfile() {
        var response = auth.register(request("TECHNICIAN"));

        assertEquals("TECHNICIAN", response.get("role"));
        assertEquals(42L, users.technicianProfileUserId);
    }

    @Test
    void duplicateEmailBecomesConflict() {
        users.createFailure = new DuplicateKeyException("duplicate email");

        assertThrows(ConflictException.class, () -> auth.register(request("USER")));
    }

    private RegisterRequest request(String role) {
        return new RegisterRequest("Test User", "test@example.com", "+91 90000 00000", "FixFlow", role, "Password123");
    }

    private static final class FixedPasswordService extends PasswordService {
        @Override
        public String hash(String password) { return "hashed-password"; }
    }

    private static final class RecordingUserDao extends UserDao {
        private boolean createCalled;
        private long technicianProfileUserId;
        private RuntimeException createFailure;

        private RecordingUserDao() { super(null); }

        @Override
        public long create(String fullName, String email, String phone, String organization, String role, String passwordHash) {
            createCalled = true;
            if (createFailure != null) throw createFailure;
            return 42L;
        }

        @Override
        public void createTechnicianProfile(long userId) { technicianProfileUserId = userId; }
    }
}
