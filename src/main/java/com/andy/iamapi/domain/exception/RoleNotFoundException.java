package com.andy.iamapi.domain.exception;

import java.util.UUID;

public class RoleNotFoundException extends RuntimeException{
    public RoleNotFoundException(UUID roleId) {
        super("Role not found: " + roleId);
    }

    public RoleNotFoundException(String name) {
        super("Role not found with name: " + name);
    }
}
