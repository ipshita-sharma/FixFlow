package com.fixflow.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.mock.env.MockEnvironment;

import com.fixflow.dao.UserDao;
import com.fixflow.service.PasswordService;

class DevelopmentAdminProvisionerTest {
    @Test
    void createsAnAdminWithAHashedEnvironmentPassword() {
        RecordingUserDao users = new RecordingUserDao();
        MockEnvironment environment = new MockEnvironment()
                .withProperty("fixflow.dev-admin.email", "local-admin@example.com")
                .withProperty("fixflow.dev-admin.password", "test-only-password")
                .withProperty("fixflow.dev-admin.full-name", "Local Administrator")
                .withProperty("fixflow.dev-admin.organization", "FixFlow Development");

        new DevelopmentAdminProvisioner(users, new PasswordService(), environment)
                .run(new DefaultApplicationArguments());

        assertEquals("ADMIN", users.role);
        assertTrue(users.passwordHash.startsWith("pbkdf2$"));
        assertEquals("local-admin@example.com", users.email);
    }

    private static final class RecordingUserDao extends UserDao {
        private String role;
        private String passwordHash;
        private String email;

        private RecordingUserDao() { super(null); }

        @Override
        public Optional<Map<String, Object>> findByEmail(String email) { return Optional.empty(); }

        @Override
        public long create(String fullName, String email, String phone, String organization, String role, String passwordHash) {
            this.email = email;
            this.role = role;
            this.passwordHash = passwordHash;
            return 99L;
        }
    }
}
