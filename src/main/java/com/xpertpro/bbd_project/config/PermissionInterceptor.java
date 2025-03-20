package com.xpertpro.bbd_project.config;

import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.enums.PermissionsEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class PermissionInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod method) {
            RequirePermission annotation = method.getMethodAnnotation(RequirePermission.class);
            if (annotation != null) {
                // Récupérer l'utilisateur connecté
                UserEntity user = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

                Set<PermissionsEnum> userPermissions = user.getRole().getPermissions();

                if (!userPermissions.contains(annotation.value())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Vous n'avez pas la permission.");
                    return false;
                }
            }
        }
        return true;
    }
}
