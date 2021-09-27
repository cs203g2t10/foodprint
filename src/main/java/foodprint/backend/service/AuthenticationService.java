package foodprint.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import foodprint.backend.model.User;

@Service
public class AuthenticationService {

    private UserDetailsService userDetailsService;
    
    private PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationService(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    public Authentication authenticate(UsernamePasswordAuthenticationToken token) {

        User user = (User) userDetailsService.loadUserByUsername((String) token.getPrincipal());

        if (passwordEncoder.matches((String) token.getCredentials(), user.getPassword())) {
            return new UsernamePasswordAuthenticationToken(user, null);
        }

        throw new BadCredentialsException("Incorrect credentials provided");
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
