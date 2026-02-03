package com.andy.iamapi.domain.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString()
@EqualsAndHashCode(of = "id")
public class Permission {

    private UUID id;
    private String name;
    private String description;
    private String resource;
    private String action;

    private Permission () {}

    //Factory Method
    public static Permission create(String name, String resource, String action, String description) {
        Permission permission = new Permission();
        permission.id = UUID.randomUUID();
        permission.name = name.toUpperCase();
        permission.resource = resource.toUpperCase();
        permission.action = action.toUpperCase();
        permission.description = description;

        return permission;
    }

    //Factory Method que acepta ID, para los mappers
    public static Permission reconstitute(UUID id,String name, String resource, String action, String description) {
        Permission permission = new Permission();
        permission.id = id;
        permission.name = name.toUpperCase();
        permission.resource = resource.toUpperCase();
        permission.action = action.toUpperCase();
        permission.description = description;

        return permission;
    }

    //Método de utilidad
    public String getFullPermission() {
        return resource + ":" + action;
    }
}
