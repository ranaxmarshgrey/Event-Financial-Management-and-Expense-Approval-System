package com.ooad.efms.model;

import jakarta.persistence.*;

/**
 * Abstract base class for all system users.
 * Uses SINGLE_TABLE inheritance so subclass roles (Organizer, ApprovingAuthority,
 * FinanceAdmin) live in one "users" table discriminated by the "role" column.
 *
 * Design: Open/Closed Principle — adding a new role means adding a new subclass,
 * without modifying existing user code.
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    /** BCrypt hash of the user's password. Nullable for legacy/seed records. */
    @Column(name = "password_hash")
    private String passwordHash;

    protected User() {}

    protected User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public abstract String getRoleName();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
