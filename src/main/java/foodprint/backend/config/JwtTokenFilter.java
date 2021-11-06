package foodprint.backend.config;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import foodprint.backend.model.UserRepo;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepo userRepo;

    private final Logger loggr = LoggerFactory.getLogger(this.getClass());

    @Value("${security.bypass}")
    private boolean SECURITY_BYPASSED;

    @Autowired
    public JwtTokenFilter(JwtTokenUtil jwtTokenUtil, UserRepo userRepo) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepo = userRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        if (SECURITY_BYPASSED) {
            // skip this filter if security is bypassed
            chain.doFilter(request, response);
            return;
        }

        // Get authorization header and validate
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null) {
            loggr.warn("Authorization header missing");
            chain.doFilter(request, response);
            return;
        }

        // logger.info(authHeader);

        if (authHeader.isEmpty() || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // Get jwt token and validate
        final String token = authHeader.split(" ")[1].trim();
        if (!jwtTokenUtil.validate(token)) {
            loggr.warn("JWT VALIDATION FAILED");
            chain.doFilter(request, response);
            return;
        }

        // Get user identity and set it on the spring security context
        UserDetails userDetails = userRepo
            .findByEmail(jwtTokenUtil.getUsername(token))
            .orElse(null);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null,
            userDetails == null ? List.of() : userDetails.getAuthorities()
        );

        if (userDetails == null) {
            loggr.warn("Auth Error - userDetails is null");
            chain.doFilter(request, response);
            return;
        }

        authentication.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request)
        );

        loggr.info("Auth OK - Email: {}: - Authorities: {}", userDetails.getUsername(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

}
