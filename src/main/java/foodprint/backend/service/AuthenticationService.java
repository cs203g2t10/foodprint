package foodprint.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;

@Service
public class AuthenticationService {

    private UserRepo userRepo;
    
    private PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationService(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public Authentication authenticate(UsernamePasswordAuthenticationToken token) {

        User user = userRepo.findByEmail((String) token.getPrincipal()).orElseThrow(
                    () -> new BadCredentialsException("User not found")
            );

        if (passwordEncoder.matches((String) token.getCredentials(), user.getPassword())) {
            return new UsernamePasswordAuthenticationToken(user, null);
        }

        throw new BadCredentialsException("Incorrect credentials provided");
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
