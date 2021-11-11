package foodprint.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import foodprint.backend.exceptions.InvalidException;
import foodprint.backend.exceptions.UserUnverifiedException;
import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;
import foodprint.backend.service.AuthenticationService;
import foodprint.backend.service.TwoFaService;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {
    
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private AuthenticationService authenticationService;

    @InjectMocks
    private TwoFaService twoFaService;

    private User user;
    private UsernamePasswordAuthenticationToken token;

    @BeforeEach
    void init() {
        user = new User("bobbytan@gmail.com", "SuperSecurePassw0rd", "Bobby Tan");
        token = new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());
    }

    @Test
    void authenticateUser_CorrectCredentials_ReturnAuthentication() {
        when(userDetailsService.loadUserByUsername(any(String.class))).thenReturn(user);
        when(passwordEncoder.matches(any(String.class), any(String.class))).thenReturn(true);

        authenticationService.authenticate(token);

        verify(userDetailsService).loadUserByUsername(user.getEmail());
        verify(passwordEncoder).matches("SuperSecurePassw0rd", "SuperSecurePassw0rd");
    }

    @Test
    void authenticateUser_WrongCredentials_ReturnException() {
        when(userDetailsService.loadUserByUsername(any(String.class))).thenReturn(user);
        when(passwordEncoder.matches(any(String.class), any(String.class))).thenReturn(false);

        try {
            authenticationService.authenticate(token);
        } catch (BadCredentialsException e) {
            assertEquals("Incorrect credentials provided", e.getMessage());
        }

        verify(userDetailsService).loadUserByUsername(user.getEmail());
        verify(passwordEncoder).matches("SuperSecurePassw0rd", "SuperSecurePassw0rd");
    }

    @Test
    void authenticateUser_UserUnverified_ReturnException() {
        when(userDetailsService.loadUserByUsername(any(String.class))).thenReturn(user);
        user.setRoles("FP_UNVERIFIED");

        UserUnverifiedException exception = null;
        try {
            authenticationService.authenticate(token);
        } catch (UserUnverifiedException e) {
            exception = e;
        }
        assertNotNull(exception);
        verify(userDetailsService).loadUserByUsername(user.getEmail());
    }

    @Test
    void encodePassword_Success() {
        String password = "SuperSecurePassw0rd";
        String encodedPassword = "$2a$12$uaTxLl9sPzGbIozqCB0wcuKjmmsZNW2mswGw5VRdsU4XFWs9Se7Uq";
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        passwordEncoder.encode(password);

        verify(passwordEncoder).encode(password);
    }

}
