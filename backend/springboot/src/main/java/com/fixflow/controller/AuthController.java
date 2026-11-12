package com.fixflow.controller;

import com.fixflow.dto.AuthRequest;
import com.fixflow.dto.RegisterRequest;
import com.fixflow.service.AuthService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest request, HttpSession session) {
        return establishSession(auth.register(request), session);
    }
    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody AuthRequest request, HttpSession session) {
        return establishSession(auth.login(request), session);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession session) { session.invalidate(); }

    private Map<String, Object> establishSession(Map<String, Object> user, HttpSession session) {
        session.setAttribute("userId", ((Number) user.get("id")).longValue());
        session.setAttribute("role", user.get("role").toString());
        return user;
    }
}
