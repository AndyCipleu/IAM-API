package com.andy.iamapi.domain.exception;

import java.util.UUID;

public class RoleNotFoundException extends RuntimeException{
    public RoleNotFoundException(UUID roleName) {
        super("Role not found: " + roleName);
    }
}
