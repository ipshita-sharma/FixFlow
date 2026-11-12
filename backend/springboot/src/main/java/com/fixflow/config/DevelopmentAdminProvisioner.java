package com.fixflow.config;

import java.util.Locale;
import java.util.Map;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fixflow.dao.UserDao;
import com.fixflow.service.PasswordService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Profile("dev")
@ConditionalOnProperty(prefix = "fixflow.dev-admin", name = "enabled", havingValue = "true")
public class DevelopmentAdminProvisioner implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(DevelopmentAdminProvisioner.class);

    private final UserDao users;
    private final PasswordService passwords;
    private final Environment environment;

    public DevelopmentAdminProvisioner(UserDao users, PasswordService passwords, Environment environment) {
        this.users = users;
        this.passwords = passwords;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String email = required("fixflow.dev-admin.email").trim().toLowerCase(Locale.ROOT);
        String password = required("fixflow.dev-admin.password");
        String fullName = required("fixflow.dev-admin.full-name");
        String organization = required("fixflow.dev-admin.organization");

        Map<String, Object> existing = users.findByEmail(email).orElse(null);
        if (existing != null) {
            if ("ADMIN".equals(existing.get("role"))) {
                logger.info("Development administrator {} already exists; no credentials were changed.", email);
                return;
            }
            throw new IllegalStateException("The development administrator email belongs to a non-administrator account.");
        }

        long id = users.create(fullName, email, null, organization, "ADMIN", passwords.hash(password));
        logger.info("Created development administrator account id {} for {}. The supplied password was not logged.", id, email);
    }

    private String required(String key) {
        String value = environment.getProperty(key);
        if (value == null || value.isBlank()) throw new IllegalStateException("Missing required development administrator setting: " + key);
        return value;
    }
}
