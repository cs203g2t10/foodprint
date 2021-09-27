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

import java.util.List;
import java.util.Optional;

import foodprint.backend.exceptions.AlreadyExistsException;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;
import foodprint.backend.service.UserService;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    
    @Mock
    private UserRepo users;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;
    
    @Test
    void addUser_NewEmail_ReturnUser() {
        User user = new User("bobbytan@gmail.com", "SuperSecurePassw0rd", "Bobby Tan");
        when(users.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(users.saveAndFlush(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode("SuperSecurePassw0rd")).thenReturn("$2a$12$uaTxLl9sPzGbIozqCB0wcuKjmmsZNW2mswGw5VRdsU4XFWs9Se7Uq");

        User savedUser = userService.createUser(user);
        
        assertNotNull(savedUser);
        verify(users).findByEmail(user.getEmail());
        verify(passwordEncoder).encode("SuperSecurePassw0rd");
        verify(users).saveAndFlush(user);
    }

    @Test
    void addUser_SameEmail_ReturnError() {
        User user = new User("bobbytan@gmail.com", "SuperSecurePassw0rd", "Bobby Tan");
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));

        try {
            userService.createUser(user);
        } catch (AlreadyExistsException e) {
            assertEquals("User with the same email already exists", e.getMessage());
        }

        verify(users).findByEmail(user.getEmail());
    }

    @Test
    void getUsers_ReturnUsers() {
        List<User> retrievedUser = userService.getAllUsers();
        assertNotNull(retrievedUser);
    }

    @Test
    void updateUser_NotFound_ReturnError(){
        User user = new User("bobbytan@gmail.com", "SuperSecurePassw0rd", "Bobby Tan");
        Long userId = 10L;
        when(users.findByEmail(any(String.class))).thenReturn(Optional.empty());

        try {
            userService.updateUser(userId, user);
        } catch (NotFoundException e) {
            assertEquals("User not found", e.getMessage());
        }

        verify(users).findByEmail(user.getEmail());
    }

    @Test
    void updateUser_SameEmail_ReturnError() {
        User user = new User("bobbytan@gmail.com", "SuperSecurePassw0rd", "Bobby Tan");
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));

        try {
            userService.updateUser(user.getId(), user);
        } catch (AlreadyExistsException e) {
            assertEquals("User with the same email already exists", e.getMessage());
        }

        verify(users).findByEmail(user.getEmail());
    }
    
    @Test
    void updateUser_NewEmail_ReturnUser() {
        User user = new User("bobbytan@gmail.com", "SuperSecurePassw0rd", "Bobby Tan");
        when(users.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(users.findById(null)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("SuperSecurePassw0rd")).thenReturn("$2a$12$uaTxLl9sPzGbIozqCB0wcuKjmmsZNW2mswGw5VRdsU4XFWs9Se7Uq");
        when(users.saveAndFlush(any(User.class))).thenReturn(user);

        User updatedUser = userService.updateUser(user.getId(), user);

        assertNotNull(updatedUser);
        verify(users).findByEmail(user.getEmail());
        verify(users).findById(user.getId());
        verify(passwordEncoder).encode("SuperSecurePassw0rd");
        verify(users).saveAndFlush(user);
    }

    @Test
    void deleteUser_UserDoesNotExist_ReturnError() {
        User user = new User("bobbytan@gmail.com", "SuperSecurePassw0rd", "Bobby Tan");
        when(users.findById(null)).thenReturn(Optional.empty());

        try {
            userService.deleteUser(user.getId());
        } catch (NotFoundException e) {
            assertEquals("User not found", e.getMessage());
        }
        verify(users).findById(user.getId());
    }
}