package foodprint.backend.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import foodprint.backend.exceptions.AlreadyExistsException;
import foodprint.backend.exceptions.BadRequestException;
import foodprint.backend.exceptions.InvalidException;
import foodprint.backend.exceptions.MailException;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.RestaurantRepo;
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

    private RestaurantRepo restaurantRepo;

    private static final Set<String> VALID_ROLES_SET = Set.of("FP_ADMIN", "FP_MANAGER", "FP_USER", "FP_UNVERIFIED");

    @Autowired
    UserService(UserRepo repo, PasswordEncoder passwordEncoder, TokenRepo tokenRepo, EmailService emailService,
            RestaurantRepo restaurantRepo) {
        this.userRepo = repo;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepo = tokenRepo;
        this.emailService = emailService;
        this.restaurantRepo = restaurantRepo;
    }

    @PreAuthorize("hasAnyAuthority('FP_ADMIN')")
    public User createUser(User user) {
        userRepo.findByEmail(user.getEmail()).ifPresent(duplicateUser -> {
            throw new AlreadyExistsException("User with the same email already exists");
        });

        if (user.getId() != null) {
            userRepo.findById(user.getId()).ifPresent(duplicateUser -> {
                throw new AlreadyExistsException("User with the same ID already exists");
            });
        }

        String plaintextPassword = user.getPassword();
        String encodedPassword = passwordEncoder.encode(plaintextPassword);
        user.setPassword(encodedPassword);
        return userRepo.saveAndFlush(user);
    }

    @PreAuthorize("hasAnyAuthority('FP_ADMIN')")
    public List<User> getAllUsers() {
        return this.userRepo.findAll();
    }

    @PreAuthorize("hasAnyAuthority('FP_ADMIN')")
    public User getUser(Long id) {
        return this.userRepo.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User unprotectedGetUser(Long id) {
        return getUser(id);
    }

    public User updateUser(Long id, User updatedUser) {
        String email = updatedUser.getEmail();
        User existingUser = this.getUser(id);
        Optional<User> userOpt = this.userRepo.findByEmail(email);
        if (userOpt.isPresent() && !userOpt.get().equals(existingUser)) {
            throw new AlreadyExistsException("User with the same email already exists");
        }
        existingUser = updateUser(id, existingUser, updatedUser);
        return existingUser;
    }

    @PreAuthorize("hasAnyAuthority('FP_ADMIN') OR #existingUser.email == authentication.name")
    public User updateUser(Long id, @Param("existingUser") User existingUser, User updatedUser) {

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
            String[] roles = updatedUser.getRoles().split(",");
            Set<String> filteredRoles = new HashSet<>();

            for (String role : roles) {
                role = role.strip().toUpperCase();
                if (VALID_ROLES_SET.contains(role)) {
                    filteredRoles.add(role);
                }
            }

            if (filteredRoles.isEmpty()) {
                throw new BadRequestException("No roles specified");
            }

            existingUser.setRoles(String.join(",", filteredRoles));
        }

        if (updatedUser.getLastLogin() != null) {
            existingUser.setLastLogin(updatedUser.getLastLogin());
        }

        if (updatedUser.getRegisteredOn() != null) {
            existingUser.setRegisteredOn(updatedUser.getRegisteredOn());
        }
        return this.userRepo.saveAndFlush(existingUser);
    }

    @PreAuthorize("hasAnyAuthority('FP_ADMIN')")
    public void deleteUser(Long id) {
        User existingUser = this.getUser(id);
        this.userRepo.deleteById(existingUser.getId());
    }

    @PreAuthorize("hasAnyAuthority('FP_ADMIN')")
    public Page<User> searchUsers(Pageable page, String emailSearch) {
        if (emailSearch == null) {
            return userRepo.findAll(page);
        }

        return userRepo.findByEmail(page, emailSearch);
    }

    // --------------------------- PASSWORD RESET ---------------------------------

    public void requestPasswordReset(String email) {
        User user = userRepo.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));

        Token token = new Token(Token.PASSWORD_RESET_REQUEST_TOKEN, user);
        tokenRepo.save(token);

        // Craft and send the email
        String emailBody = String.format("Hi %s, \n\n"
                + "You have requested for a password reset! Please use this link to set a new password http://foodprint.works/resetpassword?token=%s \n\n"
                + "This verification token will expire in 48 hours. You can safely ignore this email if you did not request for it. \n\n"
                + "Regards,\nFoodprint Support", user.getFirstName(), token.getToken());

        try {
            emailService.sendSimpleEmail(user, "Foodprint Password Reset", emailBody);
        } catch (MailException e) {
            tokenRepo.delete(token); // Rollback
            throw new MailException(e.getMessage());
        }
    }

    public void checkPasswordReset(String tok) {
        Token token = tokenRepo.findByToken(tok)
                .orElseThrow(() -> new NotFoundException("The specified token was not found"));
        if (!token.isValid() || token.getType() != Token.PASSWORD_RESET_REQUEST_TOKEN) {
            throw new InvalidException("Token invalid");
        }
    }

    public void doPasswordReset(String tok, String password) {
        Token token = tokenRepo.findByToken(tok)
                .orElseThrow(() -> new NotFoundException("The specified token was not found"));
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

    public void addFavouriteRestaurant(User user, Long restaurantId) {
        Restaurant restaurant = getRestaurantById(restaurantId);
        Set<Restaurant> favouriteRestaurants = new HashSet<>();
        if (user.getFavouriteRestaurants() != null) {
            favouriteRestaurants = user.getFavouriteRestaurants();
        }
        if (!favouriteRestaurants.add(restaurant)) {
            throw new AlreadyExistsException("Favourite restaurant already exists.");
        }
        user.setFavouriteRestaurants(favouriteRestaurants);
        userRepo.saveAndFlush(user);
    }

    public void deleteFavouriteRestaurant(User user, Long restaurantId) {
        Restaurant restaurant = getRestaurantById(restaurantId);
        Set<Restaurant> favouriteRestaurants = user.getFavouriteRestaurants();
        if (favouriteRestaurants == null || !favouriteRestaurants.contains(restaurant)) {
            throw new NotFoundException("Favourite restaurant not found.");
        } 
        favouriteRestaurants.remove(restaurant);
        user.setFavouriteRestaurants(favouriteRestaurants);
        userRepo.saveAndFlush(user);
    }

    public Restaurant getRestaurantById(Long restaurantId) {
        return restaurantRepo.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));
    }
}
