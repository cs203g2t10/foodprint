package foodprint.backend.config;

import org.springframework.security.core.context.SecurityContextHolder;

import foodprint.backend.model.User;

public class AuthHelper {
    public static User getCurrentUser() {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (principal instanceof User) {
            return (User) principal;
        }

        return null;
        
    }
}
