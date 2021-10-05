package foodprint.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import foodprint.backend.exceptions.AlreadyExistsException;
import foodprint.backend.exceptions.InvalidException;
import foodprint.backend.exceptions.MailException;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.model.Token;
import foodprint.backend.model.TokenRepo;
import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;

@Service
public class UserService {

    private UserRepo userRepo;

    private PasswordEncoder passwordEncoder;

    private TokenRepo tokenRepo;

    private EmailService emailService;

    @Autowired
    UserService(UserRepo repo, PasswordEncoder passwordEncoder, TokenRepo tokenRepo, EmailService emailService) {
        this.userRepo = repo;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepo = tokenRepo;
        this.emailService = emailService;
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
        Optional<User> existingUserByEmail = userRepo.findByEmail(updatedUser.getEmail());
        if (existingUserByEmail.isPresent()) {
            throw new AlreadyExistsException("User with the same email already exists");
        }

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

    // --------------------------- PASSWORD RESET ---------------------------------

    public void requestPasswordReset(String email) {

        User user = userRepo.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));

        Token token = new Token(Token.PASSWORD_RESET_REQUEST_TOKEN, user);
        tokenRepo.save(token);

        // Craft and send the email
        String emailBody = String.format(
            "Hi %s, \n\n" +    
            "You have requested for a password reset! Please use this link to set a new password http://foodprint.works/auth/resetpwd/%s \n\n" +
            "This verification token will expire in 48 hours. You can safely ignore this email if you did not request for it. \n\n" +
            "Regards,\nFoodprint Support",
            user.getFirstName(),
            token.getToken()
        );

        try {
            emailService.sendSimpleEmail(user, "Foodprint Password Reset", emailBody);
        } catch (MailException e) {
            tokenRepo.delete(token); // Rollback
            throw new MailException(e.getMessage());
        }
    }

    /*
    public void checkPasswordReset(String tok) {

        Token token = tokenRepo.findByToken(tok);

        if (token == null) {
            throw new NotFoundException("Token not found");
        }

        if (!token.isValid() || token.getType() != Token.PASSWORD_RESET_REQUEST_TOKEN) {
            throw new InvalidException("Token invalid");
        }
    }

    public void doPasswordReset(String tok, String password) {

        // if (!password.matches(pwdRegex)) {
        //     return PasswordResetResult.PASSWORD_REQ_UNMET;
        // } do via dto and controller

        Token token = tokenRepo.findByToken(tok);

        if (token == null) {
            throw new NotFoundException("Token not found");
        }

        User user = token.getRequestor();

        if (!token.isValid() || token.getType() != Token.PASSWORD_RESET_REQUEST_TOKEN) {
            throw new InvalidException("Token invalid");
        }

        String encodedPassword = passwordEncoder.encode(password);
        
        user.setPassword(encodedPassword);
        token.setUsed(true);

        tokenRepo.save(token);
        userRepo.save(user);
    }
    */

}
