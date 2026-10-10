package com.clinic.util;

import com.clinic.entity.Role;
import com.clinic.exception.UnauthorizedException;
import com.clinic.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new UnauthorizedException("User is not authenticated");
        }
        return principal;
    }

    public static String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public static Role getCurrentUserRole() {
        return getCurrentUser().getRole();
    }

    public static boolean isAdmin() {
        return getCurrentUser().getRole() == Role.ADMIN;
    }

    public static boolean isDoctor() {
        return getCurrentUser().getRole() == Role.DOCTOR;
    }

    public static boolean isPatient() {
        return getCurrentUser().getRole() == Role.PATIENT;
    }
}
