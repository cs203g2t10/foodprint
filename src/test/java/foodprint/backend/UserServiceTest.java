package foodprint.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;
import foodprint.backend.service.UserDetailsServiceImpl;
import foodprint.backend.service.UserService;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    
    @Mock
    private UserRepo users;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;
    
    @Test // Why does it not work?
    void addUser_NewEmail_ReturnUser() {
        User user = new User("bobbytan@gmail.com", "SuperSecurePassw0rd", "Bobby Tan");

        when(users.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(users.saveAndFlush(any(User.class))).thenReturn(user);
        when(users.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode("SuperSecurePassw0rd")).thenReturn("$2a$12$uaTxLl9sPzGbIozqCB0wcuKjmmsZNW2mswGw5VRdsU4XFWs9Se7Uq");

        User savedUser = userService.createUser(user);
        assertNotNull(savedUser);
        verify(users).findByEmail(user.getEmail());
        verify(users).save(user);
    }

    @Test
    void addUser_SameEmail_ReturnError() {
        User user = new User("bobbytan@gmail.com", "SuperSecurePassw0rd", "Bobby Tan");
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));

        try {
            userService.createUser(user);
        } catch (Exception e) {
            assertEquals("User with the same email already exists", e.getMessage());
        }

        verify(users).findByEmail(user.getEmail());
    }
}