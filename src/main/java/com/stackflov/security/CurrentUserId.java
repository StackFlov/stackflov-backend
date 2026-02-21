package com.stackflov.security; // ✅ 아무 데나 가능. config 말고 security/util 추천

import com.stackflov.config.CustomUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentUserId {

    private CurrentUserId() {}

    public static Long get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Unauthenticated");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserPrincipal p) {
            return p.getId();
        }

        // anonymousUser 같은 케이스 방어
        throw new IllegalStateException("Principal is not CustomUserPrincipal: " + principal);
    }
}