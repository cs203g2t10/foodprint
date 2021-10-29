package foodprint.backend.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;

/**
 * This class is only used for STAGING TESTS because verifying an email
 * for registration is too difficult.
 * 
 * To use, set application property security.bypass=true
 */
@Component
public class BypassSecurityFilter extends OncePerRequestFilter {

    private final PasswordEncoder encoder;

    private final UserRepo userRepo;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public BypassSecurityFilter(UserRepo userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        User createdUser = userRepo.findByEmail("bobbytan@bypass.com").orElse(null);
                
        if (createdUser == null) {
            User user = new User("bobbytan@bypass.com", "SuperSecurePassw0rd", "Bobby Bypass");
            user.setRoles("FP_USER,FP_MANAGER");
            user.setRegisteredOn(LocalDateTime.now());
            String plaintextPassword = user.getPassword();
            String encodedPassword = encoder.encode(plaintextPassword);
            user.setPassword(encodedPassword);
            createdUser = userRepo.save(user);
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            createdUser, null,
            createdUser == null ? List.of() : createdUser.getAuthorities()
        );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        logger.info("Auth OK - Email: {}: - Authorities: {}", createdUser.getUsername(), createdUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

}
