package foodprint.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import foodprint.backend.model.UserRepo;

@Service
public class AuthenticationService {

    private UserRepo userRepo;

    @Autowired
    AuthenticationService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public Authentication authenticate(UsernamePasswordAuthenticationToken token) {
        return new UsernamePasswordAuthenticationToken(
            userRepo.findByEmail((String) token.getPrincipal()).orElseThrow(
                    () -> new BadCredentialsException("USERNOTFOUND")
                ),
                null
            );
    }
    
    public boolean authenticate(String email, String password) {
        return true;
    }

}
