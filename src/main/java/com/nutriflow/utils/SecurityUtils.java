package com.nutriflow.utils;

import com.nutriflow.enums.Role;
import com.nutriflow.security.SecurityUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Security əməliyyatları üçün yardımçı sinif.
 * Authentication, Authorization və current user məlumatlarının əldə edilməsi.
 */
@Component
@Slf4j
public class SecurityUtils {

    /**
     * Cari authenticated user-in ID-sini qaytarır.
     *
     * @return User ID
     * @throws AccessDeniedException Əgər sistemə giriş edilməyibsə
     */
    public static Long getCurrentUserId() {
        SecurityUser securityUser = getCurrentSecurityUser();
        return securityUser.getId();
    }

    /**
     * Cari authenticated user-in email-ini qaytarır.
     *
     * @return User email
     * @throws AccessDeniedException Əgər sistemə giriş edilməyibsə
     */
    public static String getCurrentUserEmail() {
        SecurityUser securityUser = getCurrentSecurityUser();
        return securityUser.getUsername();
    }

    /**
     * Cari authenticated user-in Role-unu qaytarır.
     *
     * @return User role
     * @throws AccessDeniedException Əgər sistemə giriş edilməyibsə
     */
    public static Role getCurrentUserRole() {
        SecurityUser securityUser = getCurrentSecurityUser();
        // SecurityUser.getRole() String qaytarırsa, Role enum-a çeviririk
        String roleString = securityUser.getRole();
        return Role.valueOf(roleString);
    }

    /**
     * SecurityContext-dən SecurityUser obyektini çıxarır.
     *
     * @return SecurityUser
     * @throws AccessDeniedException Əgər sistemə giriş edilməyibsə
     */
    public static SecurityUser getCurrentSecurityUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Sistemə giriş edilməyib!");
            throw new AccessDeniedException("Sistemə giriş tələb olunur!");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof SecurityUser)) {
            log.error("Principal SecurityUser tipində deyil: {}", principal.getClass().getName());
            throw new AccessDeniedException("Yanlış authentication tipi!");
        }

        return (SecurityUser) principal;
    }

    /**
     * User-in müəyyən role-a sahib olub-olmadığını yoxlayır.
     *
     * @param role Yoxlanılacaq role
     * @return true əgər user həmin role-a sahibdirsə
     */
    public static boolean hasRole(Role role) {
        try {
            return getCurrentUserRole() == role;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * User-in ADMIN roluna sahib olub-olmadığını yoxlayır.
     *
     * @return true əgər admin-dirsə
     */
    public static boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    /**
     * User-in DIETITIAN roluna sahib olub-olmadığını yoxlayır.
     *
     * @return true əgər dietitian-dırsa
     */
    public static boolean isDietitian() {
        return hasRole(Role.DIETITIAN);
    }

    /**
     * User-in CATERER roluna sahib olub-olmadığını yoxlayır.
     *
     * @return true əgər caterer-dirsə
     */
    public static boolean isCaterer() {
        return hasRole(Role.CATERER);
    }

    /**
     * Sistemə giriş edilib-edilmədiyini yoxlayır.
     *
     * @return true əgər authenticated-dirsə
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Current user-in başqa bir user-ə access hüququnu yoxlayır.
     * Admin və Dietitian öz müştərilərinə baxa bilər.
     *
     * @param targetUserId Baxılacaq user-in ID-si
     * @return true əgər access varsa
     */
    public static boolean canAccessUser(Long targetUserId) {
        try {
            Long currentUserId = getCurrentUserId();

            // Özünə həmişə baxa bilər
            if (currentUserId.equals(targetUserId)) {
                return true;
            }

            // Admin hər kəsə baxa bilər
            if (isAdmin()) {
                return true;
            }

            // Dietitian və Caterer öz müştərilərinə baxa bilər (bu logic service-də yoxlanılmalı)
            return isDietitian() || isCaterer();

        } catch (Exception e) {
            log.error("Access yoxlaması zamanı xəta: {}", e.getMessage());
            return false;
        }
    }
}