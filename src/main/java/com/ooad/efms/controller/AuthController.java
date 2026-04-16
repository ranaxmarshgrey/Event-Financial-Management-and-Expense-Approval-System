package com.ooad.efms.controller;

import com.ooad.efms.dto.AuthUserResponse;
import com.ooad.efms.dto.LoginRequest;
import com.ooad.efms.model.User;
import com.ooad.efms.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Session-based authentication: BCrypt-verifies the credentials, stores the
 * authenticated user id on the HTTP session, and exposes /me + /logout for
 * the SPA. Intentionally lightweight (no Spring Security framework) so the
 * existing controllers stay open during the demo while still demonstrating
 * a real password-hash flow.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public static final String SESSION_USER_ID = "authUserId";

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, HttpSession session) {
        return userRepository.findByEmail(req.getEmail())
                .filter(u -> u.getPasswordHash() != null && encoder.matches(req.getPassword(), u.getPasswordHash()))
                .<ResponseEntity<?>>map(u -> {
                    session.setAttribute(SESSION_USER_ID, u.getId());
                    return ResponseEntity.ok(AuthUserResponse.from(u));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(java.util.Map.of("message", "Invalid email or password")));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Long id = (Long) session.getAttribute(SESSION_USER_ID);
        if (id == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return userRepository.findById(id)
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(AuthUserResponse.from(u)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
