package foodprint.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import foodprint.backend.exceptions.AlreadyExistsException;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;

@Service
public class UserService {
    
    private UserRepo userRepo;

    private PasswordEncoder passwordEncoder;

    @Autowired
    UserService(UserRepo repo, PasswordEncoder passwordEncoder) {
        this.userRepo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user) {
        Optional<User> existingUserByEmail = userRepo.findByEmail(user.getEmail());
        Optional<User> existingUserById = userRepo.findById(user.getId());
        if (existingUserById.isPresent()) {
            throw new AlreadyExistsException("User with the same ID already exists");
        }
        if (existingUserByEmail.isPresent()) {
            throw new AlreadyExistsException("User with the same email already exists");
        }
        String plaintextPassword = user.getPassword();
        String encodedPassword = passwordEncoder.encode(plaintextPassword);
        user.setPassword(encodedPassword);
        return userRepo.saveAndFlush(user);
    }

    public List<User> getAllUsers() {
        return this.userRepo.findAll();
    }

    public User getUser(Long id) {
        User user = this.userRepo.findById(id).orElseThrow(() ->  new NotFoundException("User not found"));
        return user;
    }

    public User updateUser(Long id, User updatedUser) {
        User existingUser = this.getUser(id);
        existingUser = updateUser(id, existingUser, updatedUser);
        return existingUser;
    }
    
    @PreAuthorize("hasAnyAuthority('FP_ADMIN') OR #existingUser.email == authentication.name")
    public User updateUser(Long id, @Param("existingUser") User existingUser,  User updatedUser) {
        
        if (updatedUser.getEmail() != null) {
            existingUser.setEmail(updatedUser.getEmail());
        }

        if (updatedUser.getFirstName() != null) {
            existingUser.setFirstName(updatedUser.getFirstName());
        }

        if (updatedUser.getLastName() != null) {
            existingUser.setLastName(updatedUser.getLastName());
        }

        if (updatedUser.getPassword() != null) {
            String plaintextPassword = updatedUser.getPassword();
            String encodedPassword = passwordEncoder.encode(plaintextPassword);
            existingUser.setPassword(encodedPassword);
        }

        if (updatedUser.getRoles() != null) {
            existingUser.setRoles(updatedUser.getRoles().replace(" ", ""));
        }

        return this.userRepo.saveAndFlush(existingUser);
    }

    public void deleteUser(Long id) {
        User existingUser = this.getUser(id);
        this.userRepo.deleteById(existingUser.getId());
    }

}
