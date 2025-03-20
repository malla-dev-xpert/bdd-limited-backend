package com.xpertpro.bbd_project.config;

import com.xpertpro.bbd_project.enums.PermissionsEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    PermissionsEnum value();
}
