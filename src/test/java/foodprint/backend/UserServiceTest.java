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
    void addUser_SameEmail_ReturnException() {
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
    void getUser_NonExistentId_ReturnException() {
        Long userId = 1L;
        when(users.findById(any(Long.class))).thenReturn(Optional.empty());

        try {
            userService.getUser(userId);
        } catch (NotFoundException e) {
            assertEquals("User not found", e.getMessage());
        }

        verify(users).findById(userId);
    }

    @Test
    void getUser_ExistingId_ReturnUser() {
        User user = new User("bobbytan@gmail.com", "SuperSecurePassw0rd", "Bobby Tan");
        Long userId = 1L;
        when(users.findById(any(Long.class))).thenReturn(Optional.of(user));

        userService.getUser(userId);

        verify(users).findById(userId);
    }

    @Test
    void updateUser_NotFound_ReturnException(){
        User user = new User("bobbytan@gmail.com", "SuperSecurePassw0rd", "Bobby Tan");
        Long userId = 1L;
        when(users.findById(any(Long.class))).thenReturn(Optional.empty());

        try {
            userService.updateUser(userId, user);
        } catch (NotFoundException e) {
            assertEquals("User not found", e.getMessage());
        }

        verify(users).findById(userId);
    }

    @Test
    void updateUser_SameEmail_ReturnUser() {
        User user = new User("bobbytan@gmail.com", "SuperSecurePassw0rd", "Bobby Tan");
        Long userId = 1L;
        when(users.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        when(users.saveAndFlush(any(User.class))).thenReturn(user);

        User updatedUser = userService.updateUser(userId, user);

        assertNotNull(updatedUser);
        verify(users).findById(userId);
        verify(users).findByEmail(user.getEmail());
        verify(users).saveAndFlush(user);
    }
    
    @Test
    void updateUser_NewEmail_ReturnUser() {
        User user = new User("bobbytan@gmail.com", "SuperSecurePassw0rd", "Bobby Tan");
        Long userId = 1L;
        when(users.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(users.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("SuperSecurePassw0rd")).thenReturn("$2a$12$uaTxLl9sPzGbIozqCB0wcuKjmmsZNW2mswGw5VRdsU4XFWs9Se7Uq");
        when(users.saveAndFlush(any(User.class))).thenReturn(user);

        User updatedUser = userService.updateUser(userId, user);

        assertNotNull(updatedUser);
        verify(users).findByEmail(user.getEmail());
        verify(users).findById(userId);
        verify(passwordEncoder).encode("SuperSecurePassw0rd");
        verify(users).saveAndFlush(user);
    }

    @Test
    void deleteUser_UserDoesNotExist_ReturnException() {
        Long userId = 1L;
        when(users.findById(any(Long.class))).thenReturn(Optional.empty());

        try {
            userService.deleteUser(userId);
        } catch (NotFoundException e) {
            assertEquals("User not found", e.getMessage());
        }
        verify(users).findById(userId);
    }

    @Test
    void deleteUser_UsertExists_Success() {
        User user = new User("bobbytan@gmail.com", "SuperSecurePassw0rd", "Bobby Tan");
        Long userId = 1L;
        when(users.findById(any(Long.class))).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(users).findById(userId);
    }
}
