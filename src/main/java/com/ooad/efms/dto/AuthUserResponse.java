package com.ooad.efms.dto;

import com.ooad.efms.model.User;

public class AuthUserResponse {
    private final Long id;
    private final String name;
    private final String email;
    private final String role;

    private AuthUserResponse(Long id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public static AuthUserResponse from(User u) {
        return new AuthUserResponse(u.getId(), u.getName(), u.getEmail(), u.getRoleName());
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
