package foodprint.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import foodprint.backend.config.AuthHelper;
import foodprint.backend.exceptions.AlreadyExistsException;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.RestaurantRepo;
import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;
import foodprint.backend.service.UserService;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    
    @Mock
    private UserRepo users;

    @Mock
    private RestaurantRepo restaurants;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private User user;
    private Long userId;
    private Restaurant restaurant;
    private List<String> restaurantCategories;
    private Set<Restaurant> favouriteRestaurants;
    private Long restaurantId;
    private List<User> userList;

    @BeforeEach
    void init() {
        user = new User("bobbytan@gmail.com", "SuperSecurePassw0rd", "Bobby Tan");
        userId = 1L;
        ReflectionTestUtils.setField(user, "id", userId);
        userList = new ArrayList<>();
        restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        restaurantId = 1L;
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);
        favouriteRestaurants = new HashSet<Restaurant>();
        user.setFavouriteRestaurants(favouriteRestaurants);
    }
    
    @Test
    void addUser_NewEmail_ReturnUser() {
        when(users.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(users.saveAndFlush(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode("SuperSecurePassw0rd")).thenReturn("$2a$12$uaTxLl9sPzGbIozqCB0wcuKjmmsZNW2mswGw5VRdsU4XFWs9Se7Uq");

        User savedUser = userService.createUser(user);

        assertEquals(user, savedUser);
        verify(users).findByEmail(user.getEmail());
        verify(passwordEncoder).encode("SuperSecurePassw0rd");
        verify(users).saveAndFlush(user);
    }

    @Test
    void addUser_SameEmail_ReturnException() {
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));

        String exceptionMsg = "";
        try {
            userService.createUser(user);
        } catch (AlreadyExistsException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("User with the same email already exists", exceptionMsg);
        verify(users).findByEmail(user.getEmail());
    }

    @Test
    void addUser_SameId_ReturnException() {
        when(users.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(users.findById(any(Long.class))).thenReturn(Optional.of(user));

        String exceptionMsg = "";
        try {
            userService.createUser(user);
        } catch (AlreadyExistsException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("User with the same ID already exists", exceptionMsg);
        verify(users).findByEmail(user.getEmail());
        verify(users).findById(userId);
    }

    @Test
    void getUsers_ReturnUsers() {
        userList.add(user);
        when(users.findAll()).thenReturn(userList);

        List<User> retrievedUsers = userService.getAllUsers();

        verify(users).findAll();
        assertEquals(userList, retrievedUsers);
    }

    @Test
    void getUser_NonExistentId_ReturnException() {
        when(users.findById(any(Long.class))).thenReturn(Optional.empty());

        String exceptionMsg = "";
        try {
            userService.getUser(userId);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("User not found", exceptionMsg);
        verify(users).findById(userId);
    }

    @Test
    void getUser_ExistingId_ReturnUser() {
        when(users.findById(any(Long.class))).thenReturn(Optional.of(user));

        User getUser = userService.getUser(userId);

        assertEquals(user, getUser);
        verify(users).findById(userId);
    }

    @Test
    void protectedGetUser_CorrectUserAndFound_ReturnUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(AuthHelper.getCurrentUser()).thenReturn(user);
        when(users.findById(any(Long.class))).thenReturn(Optional.of(user));

        User getUserProtected = userService.protectedGetUser(userId);

        assertEquals(user, getUserProtected);
        verify(users).findById(userId);
    }

    @Test
    void protectedGetUser_UserNotFound_ReturnException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(AuthHelper.getCurrentUser()).thenReturn(user);
        when(users.findById(any(Long.class))).thenReturn(Optional.empty());

        String exceptionMsg = "";
        try {
            userService.protectedGetUser(userId);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("User not found", exceptionMsg);
        verify(users).findById(userId);
    }

    @Test
    void protectedGetUser_CurrentUserAccessingOtherUser_ReturnException() {
        Long anotherUserId = 2L;
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(AuthHelper.getCurrentUser()).thenReturn(user);

        String exceptionMsg = "";
        try {
            userService.protectedGetUser(anotherUserId);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Unable to retrieve user.", exceptionMsg);
    }

    @Test
    void unprotectedGetUser_ExistingId_ReturnUser() {
        when(users.findById(any(Long.class))).thenReturn(Optional.of(user));

        User getUserUnprotected = userService.unprotectedGetUser(userId);

        assertEquals(user, getUserUnprotected);
        verify(users).findById(userId);
    }

    @Test
    void unprotectedGetUser_NonExistentId_ReturnUser() {
        when(users.findById(any(Long.class))).thenReturn(Optional.empty());

        String exceptionMsg = "";
        try {
            userService.unprotectedGetUser(userId);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("User not found", exceptionMsg);
        verify(users).findById(userId);
    }

    @Test
    void updateUser_UserNotFound_ReturnException(){
        when(users.findById(any(Long.class))).thenReturn(Optional.empty());

        String exceptionMsg = "";
        try {
            userService.updateUser(userId, user);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("User not found", exceptionMsg);
        verify(users).findById(userId);
    }

    @Test
    void updateUser_UserUpdatesToSameEmail_ReturnUser() {
        when(users.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        when(users.saveAndFlush(any(User.class))).thenReturn(user);

        User updatedUser = userService.updateUser(userId, user);

        assertEquals(user, updatedUser);
        verify(users).findById(userId);
        verify(users).findByEmail(user.getEmail());
        verify(users).saveAndFlush(user);
    }
    
    @Test
    void updateUser_NewEmail_ReturnUser() {
        when(users.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(users.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("SuperSecurePassw0rd")).thenReturn("$2a$12$uaTxLl9sPzGbIozqCB0wcuKjmmsZNW2mswGw5VRdsU4XFWs9Se7Uq");
        when(users.saveAndFlush(any(User.class))).thenReturn(user);

        User updatedUser = userService.updateUser(userId, user);

        assertEquals(user, updatedUser);
        verify(users).findByEmail(user.getEmail());
        verify(users).findById(userId);
        verify(passwordEncoder).encode("SuperSecurePassw0rd");
        verify(users).saveAndFlush(user);
    }

    @Test
    void updateUser_EmailTaken_ReturnException() {
        User anotherUser = new User("testing@gmail.com", "SuperSecurePassw0rd", "Testing Tan");

        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(anotherUser));
        when(users.findById(any(Long.class))).thenReturn(Optional.of(user));

        String exceptionMsg = "";
        try {
            userService.updateUser(userId, user);
        } catch (AlreadyExistsException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("User with the same email already exists", exceptionMsg);
        verify(users).findByEmail(user.getEmail());
        verify(users).findById(userId);
    }

    @Test
    void deleteUser_UserDoesNotExist_ReturnException() {
        when(users.findById(any(Long.class))).thenReturn(Optional.empty());

        String exceptionMsg = "";
        try {
            userService.deleteUser(userId);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("User not found", exceptionMsg);
        verify(users).findById(userId);
    }

    @Test
    void deleteUser_UserExists_Success() {
        when(users.findById(any(Long.class))).thenReturn(Optional.of(user));

        String exceptionMsg = "";
        try {
            userService.deleteUser(userId);
        } catch (Exception e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("", exceptionMsg);
        verify(users).findById(userId);
    }

    @Test
    void addFavouriteRestaurant_RestaurantAdded_Success() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(AuthHelper.getCurrentUser()).thenReturn(user);
        when(restaurants.findById(any(Long.class))).thenReturn(Optional.of(restaurant));
        when(users.saveAndFlush(any(User.class))).thenReturn(user);

        String exceptionMsg = "";
        try {
            userService.addFavouriteRestaurant(user, restaurantId);
        } catch (Exception e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("", exceptionMsg);
        verify(restaurants).findById(restaurantId);
        verify(users).saveAndFlush(user);
    }

    @Test
    void addFavouriteRestaurant_RestaurantNotFound_ReturnException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(AuthHelper.getCurrentUser()).thenReturn(user);
        when(restaurants.findById(any(Long.class))).thenReturn(Optional.empty());

        String exceptionMsg = "";
        try {
            userService.addFavouriteRestaurant(user, restaurantId);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Restaurant not found", exceptionMsg);
        verify(restaurants).findById(restaurantId);
    }

    @Test
    void addFavouriteRestaurant_FavouriteRestaurantAlreadyExist_ReturnException() {
        favouriteRestaurants.add(restaurant);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(AuthHelper.getCurrentUser()).thenReturn(user);
        when(restaurants.findById(any(Long.class))).thenReturn(Optional.of(restaurant));
        
        String exceptionMsg = "";
        try {
            userService.addFavouriteRestaurant(user, restaurantId);
        } catch (AlreadyExistsException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Favourite restaurant already exists.", exceptionMsg);
        verify(restaurants).findById(restaurantId);
    }

    @Test
    void deleteFavouriteRestaurant_FavouriteRestaurantFoundAndDeleted_Success() {
        favouriteRestaurants.add(restaurant);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(AuthHelper.getCurrentUser()).thenReturn(user);
        when(restaurants.findById(any(Long.class))).thenReturn(Optional.of(restaurant));
        when(users.saveAndFlush(any(User.class))).thenReturn(user);

        String exceptionMsg = "";
        try {
            userService.deleteFavouriteRestaurant(user, restaurantId);
        } catch (Exception e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("", exceptionMsg);
        verify(restaurants).findById(restaurantId);
        verify(users).saveAndFlush(user);
    }

    @Test
    void deleteFavouriteRestaurant_RestaurantNotFound_ReturnException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(AuthHelper.getCurrentUser()).thenReturn(user);
        when(restaurants.findById(any(Long.class))).thenReturn(Optional.empty());

        String exceptionMsg = "";
        try {
            userService.deleteFavouriteRestaurant(user, restaurantId);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Restaurant not found", exceptionMsg);
        verify(restaurants).findById(restaurantId);
    }

    @Test
    void deleteFavouriteRestaurant_RestaurantNotFoundInFavourites_ReturnException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(AuthHelper.getCurrentUser()).thenReturn(user);
        when(restaurants.findById(any(Long.class))).thenReturn(Optional.of(restaurant));

        String exceptionMsg = "";
        try {
            userService.deleteFavouriteRestaurant(user, restaurantId);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Favourite restaurant not found.", exceptionMsg);
        verify(restaurants).findById(restaurantId);
    }
}
