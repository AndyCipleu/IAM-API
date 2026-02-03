package com.andy.iamapi.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@ToString(exclude = "permissions") //Evitar lazy loading issues
@EqualsAndHashCode(of = "id")
public class Role {

    private UUID id;
    private String name;
    private String description;
    private Set<Permission> permissions;

    private Role() {
        this.permissions = new HashSet<>();
    }

    //Factory Method
    public static Role create(String name, String description) {
        Role role = new Role();
         role.id = UUID.randomUUID();
         role.name = name;
         role.description = description;
         return role;
    }
    //Factory Method aceptando ID, para los mappers
    public static Role reconstitute(UUID id,String name, String description) {
        Role role = new Role();
        role.id = id;
        role.name = name;
        role.description = description;
        return role;
    }

    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
    }

    public boolean hasPermission (String permissionName) {
        return permissions.stream().anyMatch(permission -> permission.getName().equals(permissionName));
    }

    public Set<Permission> getPermission() {
        return new HashSet<>(permissions);
    }
}
