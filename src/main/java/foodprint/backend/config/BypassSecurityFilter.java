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
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;
import foodprint.backend.service.UserService;

@Component
public class BypassSecurityFilter extends OncePerRequestFilter {

    private final UserService userService;

    private final UserRepo userRepo;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    public BypassSecurityFilter(UserRepo userRepo, UserService userService) {
        this.userRepo = userRepo;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        User createdUser = userRepo.findByEmail("bobbytan@bypass.com").orElse(null);
                
        if (createdUser == null) {
            User user = new User("bobbytan@bypass.com", "SuperSecurePassw0rd", "Bobby Bypass");
            user.setRoles("FP_USER,FP_MANAGER,FP_ADMIN");
            user.setRegisteredOn(LocalDateTime.now());
            createdUser = userService.createUser(user);
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
