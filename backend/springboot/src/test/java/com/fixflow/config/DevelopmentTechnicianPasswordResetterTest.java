package com.fixflow.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.mock.env.MockEnvironment;

import com.fixflow.dao.UserDao;
import com.fixflow.service.PasswordService;

class DevelopmentTechnicianPasswordResetterTest {
    private static final String PREFIX = "fixflow.dev-technician-password-reset.";
    private static final String ARJUN_PASSWORD = "Arjun-development-password-1";
    private static final String RAHUL_PASSWORD = "Rahul-development-password-2";

    @Test
    void rejectsMissingTechnicianAccountBeforeUpdatingEitherPassword() {
        RecordingUserDao users = new RecordingUserDao();
        users.addAccount(DevelopmentTechnicianPasswordResetter.ARJUN_EMAIL, true, "TECHNICIAN");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> resetter(users, environment()).run(new DefaultApplicationArguments()));

        assertTrue(error.getMessage().contains(DevelopmentTechnicianPasswordResetter.RAHUL_EMAIL));
        assertTrue(users.updatedHashes.isEmpty());
    }

    @Test
    void rejectsMissingPasswordBeforeReadingAccounts() {
        RecordingUserDao users = new RecordingUserDao();
        users.addAccount(DevelopmentTechnicianPasswordResetter.ARJUN_EMAIL, true, "TECHNICIAN");
        users.addAccount(DevelopmentTechnicianPasswordResetter.RAHUL_EMAIL, true, "TECHNICIAN");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> resetter(users, new MockEnvironment().withProperty(PREFIX + "arjun-password", ARJUN_PASSWORD))
                        .run(new DefaultApplicationArguments()));

        assertTrue(error.getMessage().contains("rahul-password"));
        assertTrue(users.lookups.isEmpty());
        assertTrue(users.updatedHashes.isEmpty());
    }

    @Test
    void rejectsWrongRoleBeforeUpdatingEitherPassword() {
        RecordingUserDao users = new RecordingUserDao();
        users.addAccount(DevelopmentTechnicianPasswordResetter.ARJUN_EMAIL, true, "TECHNICIAN");
        users.addAccount(DevelopmentTechnicianPasswordResetter.RAHUL_EMAIL, true, "USER");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> resetter(users, environment()).run(new DefaultApplicationArguments()));

        assertTrue(error.getMessage().contains("not a technician"));
        assertTrue(users.updatedHashes.isEmpty());
    }

    @Test
    void rejectsInactiveAccountBeforeUpdatingEitherPassword() {
        RecordingUserDao users = new RecordingUserDao();
        users.addAccount(DevelopmentTechnicianPasswordResetter.ARJUN_EMAIL, true, "TECHNICIAN");
        users.addAccount(DevelopmentTechnicianPasswordResetter.RAHUL_EMAIL, false, "TECHNICIAN");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> resetter(users, environment()).run(new DefaultApplicationArguments()));

        assertTrue(error.getMessage().contains("inactive"));
        assertTrue(users.updatedHashes.isEmpty());
    }

    @Test
    void hashesAndUpdatesBothTechnicianPasswords() {
        RecordingUserDao users = new RecordingUserDao();
        users.addAccount(DevelopmentTechnicianPasswordResetter.ARJUN_EMAIL, true, "TECHNICIAN");
        users.addAccount(DevelopmentTechnicianPasswordResetter.RAHUL_EMAIL, true, "TECHNICIAN");

        resetter(users, environment()).run(new DefaultApplicationArguments());

        PasswordService passwords = new PasswordService();
        assertEquals(2, users.updatedHashes.size());
        assertTrue(passwords.matches(ARJUN_PASSWORD, users.updatedHashes.get(DevelopmentTechnicianPasswordResetter.ARJUN_EMAIL)));
        assertTrue(passwords.matches(RAHUL_PASSWORD, users.updatedHashes.get(DevelopmentTechnicianPasswordResetter.RAHUL_EMAIL)));
        assertFalse(passwords.matches("wrong-password", users.updatedHashes.get(DevelopmentTechnicianPasswordResetter.ARJUN_EMAIL)));
    }

    private DevelopmentTechnicianPasswordResetter resetter(RecordingUserDao users, MockEnvironment environment) {
        return new DevelopmentTechnicianPasswordResetter(users, new PasswordService(), environment);
    }

    private MockEnvironment environment() {
        return new MockEnvironment()
                .withProperty(PREFIX + "arjun-password", ARJUN_PASSWORD)
                .withProperty(PREFIX + "rahul-password", RAHUL_PASSWORD);
    }

    private static final class RecordingUserDao extends UserDao {
        private final Map<String, Map<String, Object>> accounts = new HashMap<>();
        private final Map<String, String> updatedHashes = new HashMap<>();
        private final Map<String, Integer> lookups = new HashMap<>();

        private RecordingUserDao() { super(null); }

        private void addAccount(String email, boolean active, String role) {
            accounts.put(email, Map.of("id", accounts.size() + 1L, "email", email, "active", active, "role", role));
        }

        @Override
        public Optional<Map<String, Object>> findByEmailForPasswordReset(String email) {
            lookups.merge(email, 1, Integer::sum);
            return Optional.ofNullable(accounts.get(email));
        }

        @Override
        public void updatePasswordHash(String email, String passwordHash) {
            updatedHashes.put(email, passwordHash);
        }
    }
}
