package com.fixflow.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    private final SecureRandom random = new SecureRandom();
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;

    public String hash(String password) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return "pbkdf2$" + Base64.getEncoder().encodeToString(salt) + "$" + digest(password, salt);
    }

    public boolean matches(String password, String stored) {
        if (stored == null || !stored.startsWith("pbkdf2$")) return false;
        String[] parts = stored.split("\\$");
        if (parts.length != 3) return false;
        return MessageDigest.isEqual(digest(password, Base64.getDecoder().decode(parts[1])).getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8));
    }

    private String digest(String password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            byte[] key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            spec.clearPassword();
            return Base64.getEncoder().encodeToString(key);
        } catch (Exception ex) { throw new IllegalStateException("Secure hashing is unavailable", ex); }
    }
}
