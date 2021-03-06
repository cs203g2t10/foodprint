package foodprint.backend.config;

import org.springframework.security.core.context.SecurityContextHolder;

import foodprint.backend.model.User;

public class AuthHelper {
    private AuthHelper() {
        throw new IllegalStateException("AuthHelper class");
    }

    public static User getCurrentUser() {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        }

        return null;

    }
}
