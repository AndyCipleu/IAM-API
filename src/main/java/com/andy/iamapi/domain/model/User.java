package com.andy.iamapi.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@ToString(exclude = {"password", "roles"})  // Excluimos datos sensibles
@EqualsAndHashCode(of = "id")  // Comparamos solo por ID
public class User {

    private UUID id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private boolean accountNonLocked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<Role> roles;

    private User() {
        this.roles = new HashSet<>();
    }

    //Factory method
    public static User create(String email, String hashedPassword,
                              String firstName, String lastName) {
        User user = new User();
        user.id = UUID.randomUUID();
        user.email = email.toLowerCase().trim();
        user.password = hashedPassword;
        user.firstName = firstName;
        user.lastName = lastName;
        user.enabled = true;
        user.accountNonLocked = true;
        user.createdAt = LocalDateTime.now();
        user.updatedAt = LocalDateTime.now();
        return user;
    }

    //Factory Method aceptando otros parametros, para los mappers
    public static User reconstitute(UUID id,String email, String hashedPassword,
                                    String firstName, String lastName, boolean enabled,
                                    boolean accountNonLocked, LocalDateTime createdAt,
                                    LocalDateTime updatedAt) {
        User user = new User();

        user.id = id;
        user.email = email.toLowerCase().trim();
        user.password = hashedPassword;
        user.firstName = firstName;
        user.lastName = lastName;
        user.enabled = enabled;
        user.accountNonLocked = accountNonLocked;
        user.createdAt = createdAt;
        user.updatedAt = updatedAt;

        return user;
    }

    // Métodos de negocio
    public void addRole(Role role) {
        this.roles.add(role);
        this.updatedAt = LocalDateTime.now();
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
        this.updatedAt = LocalDateTime.now();
    }

    public void lock() {
        this.accountNonLocked = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void unlock() {
        this.accountNonLocked = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void disable() {
        this.enabled = false;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }

    // Override manual para defensive copy
    public Set<Role> getRoles() {
        return new HashSet<>(roles);
    }
}