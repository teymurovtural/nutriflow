package com.nutriflow.utils;

import com.nutriflow.enums.Role;
import com.nutriflow.security.SecurityUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility class for security operations.
 * Handles authentication, authorization, and retrieval of current user information.
 */
@Component
@Slf4j
public class SecurityUtils {

    /**
     * Returns the ID of the currently authenticated user.
     *
     * @return User ID
     * @throws AccessDeniedException if the user is not logged in
     */
    public static Long getCurrentUserId() {
        SecurityUser securityUser = getCurrentSecurityUser();
        return securityUser.getId();
    }

    /**
     * Returns the email of the currently authenticated user.
     *
     * @return User email
     * @throws AccessDeniedException if the user is not logged in
     */
    public static String getCurrentUserEmail() {
        SecurityUser securityUser = getCurrentSecurityUser();
        return securityUser.getUsername();
    }

    /**
     * Returns the role of the currently authenticated user.
     *
     * @return User role
     * @throws AccessDeniedException if the user is not logged in
     */
    public static Role getCurrentUserRole() {
        SecurityUser securityUser = getCurrentSecurityUser();
        // If SecurityUser.getRole() returns a String, convert it to Role enum
        String roleString = securityUser.getRole();
        return Role.valueOf(roleString);
    }

    /**
     * Extracts the SecurityUser object from the SecurityContext.
     *
     * @return SecurityUser
     * @throws AccessDeniedException if the user is not logged in
     */
    public static SecurityUser getCurrentSecurityUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("User is not authenticated!");
            throw new AccessDeniedException("Authentication is required!");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof SecurityUser)) {
            log.error("Principal is not of type SecurityUser: {}", principal.getClass().getName());
            throw new AccessDeniedException("Invalid authentication type!");
        }

        return (SecurityUser) principal;
    }

    /**
     * Checks whether the user has a specific role.
     *
     * @param role The role to check
     * @return true if the user has the given role
     */
    public static boolean hasRole(Role role) {
        try {
            return getCurrentUserRole() == role;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks whether the user has the ADMIN role.
     *
     * @return true if the user is an admin
     */
    public static boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    /**
     * Checks whether the user has the DIETITIAN role.
     *
     * @return true if the user is a dietitian
     */
    public static boolean isDietitian() {
        return hasRole(Role.DIETITIAN);
    }

    /**
     * Checks whether the user has the CATERER role.
     *
     * @return true if the user is a caterer
     */
    public static boolean isCaterer() {
        return hasRole(Role.CATERER);
    }

    /**
     * Checks whether the user is currently authenticated.
     *
     * @return true if the user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Checks whether the current user has access to another user's data.
     * Admins and Dietitians can view their own clients.
     *
     * @param targetUserId The ID of the user to be accessed
     * @return true if access is granted
     */
    public static boolean canAccessUser(Long targetUserId) {
        try {
            Long currentUserId = getCurrentUserId();

            // Users can always access their own data
            if (currentUserId.equals(targetUserId)) {
                return true;
            }

            // Admin can access anyone
            if (isAdmin()) {
                return true;
            }

            // Dietitian and Caterer can access their own clients (this logic should be validated in the service layer)
            return isDietitian() || isCaterer();

        } catch (Exception e) {
            log.error("Error during access check: {}", e.getMessage());
            return false;
        }
    }
}