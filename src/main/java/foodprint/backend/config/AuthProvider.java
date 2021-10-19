package foodprint.backend.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;
import foodprint.backend.service.TwoFaService;

public class AuthProvider implements AuthenticationProvider {
    @Autowired(required = false)
    private HttpServletRequest request;

    @Autowired
    UserRepo userRepo;

    @Autowired
    TwoFaService twoFaService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {

        String email = authentication.getName();
        String password = authentication.getCredentials().toString();
        String twoFaToken = request.getParameter("twoFaToken");

        Optional<User> optUser = userRepo.findByEmail(email);

        if (optUser.isEmpty()) {
            throw new BadCredentialsException("Email does not exist");
        }
        User user = optUser.get();

        List<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
        String role = user.getRoles();
        String twoFaSecret = user.getTwoFaSecret();
        SimpleGrantedAuthority sgaRole = new SimpleGrantedAuthority(role);
        authorities.add(sgaRole);

        try {
            if (role.contentEquals("FP_UNVERIFIED")) {
                throw new BadCredentialsException("To login, please verify your email.");
            }

            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new BadCredentialsException("Password is incorrect");
            }

            if (twoFaService.checkEmailHas2FA(email) && !twoFaService.validate(twoFaSecret, twoFaToken)) {
                throw new BadCredentialsException("Your second factor authentication token is incorrect.");
            }

            userRepo.save(user);
            return new UsernamePasswordAuthenticationToken(email, password, authorities);

        } catch (NumberFormatException e) {
            throw new BadCredentialsException("Your second factor authentication token is incorrect.");
        }

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
