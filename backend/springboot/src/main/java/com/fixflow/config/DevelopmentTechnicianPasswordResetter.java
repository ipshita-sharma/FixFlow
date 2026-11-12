package com.fixflow.config;

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

@Component
@Profile("dev")
@ConditionalOnProperty(prefix = "fixflow.dev-technician-password-reset", name = "enabled", havingValue = "true")
public class DevelopmentTechnicianPasswordResetter implements ApplicationRunner {
    static final String ARJUN_EMAIL = "arjun@vertexbusiness.com";
    static final String RAHUL_EMAIL = "rahul@vertexbusiness.com";
    private static final String PREFIX = "fixflow.dev-technician-password-reset.";

    private final UserDao users;
    private final PasswordService passwords;
    private final Environment environment;

    public DevelopmentTechnicianPasswordResetter(UserDao users, PasswordService passwords, Environment environment) {
        this.users = users;
        this.passwords = passwords;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String arjunPassword = required("arjun-password");
        String rahulPassword = required("rahul-password");

        requireTechnician(ARJUN_EMAIL);
        requireTechnician(RAHUL_EMAIL);

        users.updatePasswordHash(ARJUN_EMAIL, passwords.hash(arjunPassword));
        users.updatePasswordHash(RAHUL_EMAIL, passwords.hash(rahulPassword));
    }

    private Map<String, Object> requireTechnician(String email) {
        Map<String, Object> account = users.findByEmailForPasswordReset(email)
                .orElseThrow(() -> new IllegalStateException("Required technician account was not found: " + email));
        if (!Boolean.TRUE.equals(account.get("active"))) {
            throw new IllegalStateException("Required technician account is inactive: " + email);
        }
        if (!"TECHNICIAN".equals(account.get("role"))) {
            throw new IllegalStateException("Required account is not a technician: " + email);
        }
        return account;
    }

    private String required(String suffix) {
        String value = environment.getProperty(PREFIX + suffix);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required development technician password setting: " + PREFIX + suffix);
        }
        return value;
    }
}
