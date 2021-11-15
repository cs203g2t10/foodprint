package foodprint.backend;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.DiscriminatorValue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import foodprint.backend.dto.AdminUserDTO;
import foodprint.backend.dto.AuthRequestDTO;
import foodprint.backend.dto.AuthResponseDTO;
import foodprint.backend.dto.FavouriteRestaurantDTO;
import foodprint.backend.dto.ManagerRequestDTO;
import foodprint.backend.dto.UpdateUserDTO;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.RestaurantRepo;
import foodprint.backend.model.Token;
import foodprint.backend.model.TokenRepo;
import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;

import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
    "foodprint.email.server=smtp.ethereal.email",
    "foodprint.email.port=587",
    "foodprint.email.address=kianna.larson20@ethereal.email",
    "FOODPRINT_EMAIL_PASSWORD=vZzzeNPqxvDpZfKp9z"
})
@ActiveProfiles("test")
@DiscriminatorValue( "null" )
public class UserIntegrationTest {
    
    @LocalServerPort
	private int port;
    
    private final String baseUrl = "http://localhost:";

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RestaurantRepo restaurantRepo;

    @Autowired
    private TokenRepo tokenRepo;

    private User newUser;
    private User anotherUser;
    
    HttpHeaders headers = new HttpHeaders();
    TestRestTemplate testRestTemplate = new TestRestTemplate();
    
    @BeforeEach
    void createUser() {
        Optional<User> user = userRepo.findByEmail("bobby@admin.com");
        if (!user.isEmpty()) {
            userRepo.delete(user.get());
        }
        String encodedPassword = new BCryptPasswordEncoder().encode("SuperSecurePassw0rd");
        newUser = new User("bobby@admin.com", encodedPassword, "bobby");
        newUser.setRoles("FP_ADMIN");
        newUser.setRegisteredOn(LocalDateTime.now());
        userRepo.saveAndFlush(newUser);
        anotherUser = new User("bobby@user.com", encodedPassword, "bobby tan");
        // anotherUser.setRoles("FP_USER");
        anotherUser.setRegisteredOn(LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        userRepo.deleteAll();
        tokenRepo.deleteAll();
        restaurantRepo.deleteAll();
    }

    @Test
    public void createUser_Successful() {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@admin.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        AdminUserDTO user = new AdminUserDTO("firstName", "lastName", "user@gmail.com", "SuperSecurePassw0rd", "FP_USER", LocalDateTime.now(), LocalDateTime.now());
        HttpEntity<AdminUserDTO> entity = new HttpEntity<>(user, headers);
        
        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/user"),
                HttpMethod.POST,
                entity,
                User.class
                );
        assertEquals(201, responseEntity.getStatusCode().value());
    }

    @Test
    public void createUser_SameEmailFound_Failure() {
        userRepo.saveAndFlush(anotherUser);
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@admin.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        AdminUserDTO user = new AdminUserDTO("firstName", "lastName", "bobby@user.com", "SuperSecurePassw0rd", "FP_USER", LocalDateTime.now(), LocalDateTime.now());
        HttpEntity<AdminUserDTO> entity = new HttpEntity<>(user, headers);
        
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/user"),
                HttpMethod.POST,
                entity,
                Void.class
                );
        assertEquals(409, responseEntity.getStatusCode().value());
    }

    @Test
    public void getUser_Successful() {
        var savedUser = userRepo.saveAndFlush(anotherUser);
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@admin.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/user/{id}"),
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                User.class,
                savedUser.getId()
                );
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void getUser_UserNotFound_Failure() {
        var savedUser = userRepo.saveAndFlush(anotherUser);
        Long userId = savedUser.getId();
        userRepo.delete(anotherUser);
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@admin.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/user/{id}"),
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                Void.class,
                userId
                );
        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void updateUser_Successful() {
        var savedUser = userRepo.saveAndFlush(anotherUser);
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@user.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        UpdateUserDTO updateUser = new UpdateUserDTO("bobby", "tan", "bobby@user.com", "SuperSecurePassw0rd", "SuperSecurePassw0r");
        HttpEntity<UpdateUserDTO> entity = new HttpEntity<>(updateUser, headers);

        ResponseEntity<UpdateUserDTO> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/user/{id}"),
                HttpMethod.PATCH,
                entity,
                UpdateUserDTO.class,
                savedUser.getId()
                );
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void updateUser_UserNotFound_Failure() {
        var savedUser = userRepo.saveAndFlush(anotherUser);
        Long userId = savedUser.getId();
        userRepo.delete(anotherUser);
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@admin.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        UpdateUserDTO updateUser = new UpdateUserDTO("firstName", "lastName", "bobby@me.com", "SuperSecurePassw0rd", "SuperSecurePassw0r");
        HttpEntity<UpdateUserDTO> entity = new HttpEntity<>(updateUser, headers);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/user/{id}"),
                HttpMethod.PATCH,
                entity,
                Void.class,
                userId
                );
        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void updateUserAdmin_Successful() {
        var savedUser = userRepo.saveAndFlush(anotherUser);
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@admin.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        AdminUserDTO updateUser = new AdminUserDTO("firstName", "lastName", "bobby@user.com", "SuperSecurePassw0rd", "FP_USER", LocalDateTime.now(), LocalDateTime.now());
        HttpEntity<AdminUserDTO> entity = new HttpEntity<>(updateUser, headers);
        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/user/admin/{id}"),
                HttpMethod.PATCH,
                entity,
                User.class,
                savedUser.getId()
                );
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteUser_Successful() {
        var savedUser = userRepo.saveAndFlush(anotherUser);
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@admin.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/user/{id}"),
                HttpMethod.DELETE,
                new HttpEntity<Object>(headers),
                User.class,
                savedUser.getId()
                );

        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteUser_DeletingTheLoggedInUser_Failure() {
        var savedUser = userRepo.saveAndFlush(anotherUser);
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@user.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/user/{id}"),
                HttpMethod.DELETE,
                new HttpEntity<Object>(headers),
                User.class,
                savedUser.getId()
                );

        assertEquals(400, responseEntity.getStatusCode().value());
    }

    @Test
    public void makeManager_Successful() {
        var savedUser = userRepo.saveAndFlush(anotherUser);
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@admin.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);
    
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        var savedRestaurant = restaurantRepo.saveAndFlush(restaurant);

        ManagerRequestDTO managerRequest = new ManagerRequestDTO();
        managerRequest.setRestaurantId(savedRestaurant.getRestaurantId());
        managerRequest.setUserId(savedUser.getId());

        HttpEntity<ManagerRequestDTO> entity = new HttpEntity<>(managerRequest, headers);
        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/user/manager"),
                HttpMethod.POST,
                entity,
                User.class
                );
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void makeManager_UserAlreadyManager_Failure() {
        anotherUser.setRoles("FP_MANAGER");
        var savedUser = userRepo.saveAndFlush(anotherUser);
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@admin.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);
    
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        var savedRestaurant = restaurantRepo.saveAndFlush(restaurant);

        ManagerRequestDTO managerRequest = new ManagerRequestDTO();
        managerRequest.setRestaurantId(savedRestaurant.getRestaurantId());
        managerRequest.setUserId(savedUser.getId());

        HttpEntity<ManagerRequestDTO> entity = new HttpEntity<>(managerRequest, headers);
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/user/manager"),
                HttpMethod.POST,
                entity,
                Void.class
                );
        assertEquals(400, responseEntity.getStatusCode().value());
    }

    @Test
    public void makeManager_RestaurantNotFound_Failure() {
        var savedUser = userRepo.saveAndFlush(anotherUser);
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@admin.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);
    
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        var savedRestaurant = restaurantRepo.saveAndFlush(restaurant);
        var restaurantId = savedRestaurant.getRestaurantId();
        restaurantRepo.delete(restaurant);

        ManagerRequestDTO managerRequest = new ManagerRequestDTO();
        managerRequest.setRestaurantId(restaurantId);
        managerRequest.setUserId(savedUser.getId());

        HttpEntity<ManagerRequestDTO> entity = new HttpEntity<>(managerRequest, headers);
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/user/manager"),
                HttpMethod.POST,
                entity,
                Void.class
                );

        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void resetPassword_Successful() {
        userRepo.saveAndFlush(anotherUser);
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@user.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);
    
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        Token token = new Token(2, anotherUser);
        var savedToken = tokenRepo.saveAndFlush(token);
        
        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/api/v1/user/auth/resetpwd/{token}"),
            HttpMethod.GET,
            new HttpEntity<Object>(headers),
            String.class,
            savedToken.getToken()
            );
        
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void resetPassword_TokenNotFound_Failure() {
        userRepo.saveAndFlush(anotherUser);
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@user.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);
    
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        Token token = new Token(2, anotherUser);
        var savedToken = tokenRepo.saveAndFlush(token);
        tokenRepo.delete(token);
        
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/api/v1/user/auth/resetpwd/{token}"),
            HttpMethod.GET,
            new HttpEntity<Object>(headers),
            Void.class,
            savedToken.getToken()
            );
        
        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void resetPassword_InvalidTokenType_Failure() {
        userRepo.saveAndFlush(anotherUser);
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@user.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);
    
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        Token token = new Token(1, anotherUser);
        var savedToken = tokenRepo.saveAndFlush(token);
        
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/api/v1/user/auth/resetpwd/{token}"),
            HttpMethod.GET,
            new HttpEntity<Object>(headers),
            Void.class,
            savedToken.getToken()
            );
        assertEquals(400, responseEntity.getStatusCode().value());
    }

    @Test
    public void favouriteRestaurant_Successful() {
        userRepo.saveAndFlush(anotherUser);
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@user.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);
    
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        var savedRestaurant = restaurantRepo.saveAndFlush(restaurant);

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/user/favourite/{restaurantId}"),
                HttpMethod.POST,
                new HttpEntity<Object>(headers),
                String.class,
                savedRestaurant.getRestaurantId()
                );

        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void favouriteRestaurant_FavouriteRestaurantAlreadyExist_Failure() {
        userRepo.saveAndFlush(anotherUser);
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@user.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);
    
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        var savedRestaurant = restaurantRepo.saveAndFlush(restaurant);
        Set<Restaurant> favRestaurant = new HashSet<>();
        favRestaurant.add(restaurant);
        anotherUser.setFavouriteRestaurants(favRestaurant);
        userRepo.saveAndFlush(anotherUser);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/user/favourite/{restaurantId}"),
                HttpMethod.POST,
                new HttpEntity<Object>(headers),
                Void.class,
                savedRestaurant.getRestaurantId()
                );

        assertEquals(400, responseEntity.getStatusCode().value());
    }

    @Test
    public void favouriteRestaurant_RestaurantNotFound_Failure() {
        userRepo.saveAndFlush(anotherUser);
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@user.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);
    
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        var savedRestaurant = restaurantRepo.saveAndFlush(restaurant);
        Long restaurantId = savedRestaurant.getRestaurantId();
        restaurantRepo.delete(restaurant);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/user/favourite/{restaurantId}"),
                HttpMethod.POST,
                new HttpEntity<Object>(headers),
                Void.class,
                restaurantId
                );

        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void getAllFavouriteRestaurantsOfCurrectUser_Successful() {
        userRepo.saveAndFlush(anotherUser);
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@user.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);
    
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        restaurantRepo.saveAndFlush(restaurant);
        Set<Restaurant> favRestaurant = new HashSet<>();
        favRestaurant.add(restaurant);
        anotherUser.setFavouriteRestaurants(favRestaurant);
        userRepo.saveAndFlush(anotherUser);

        ResponseEntity<FavouriteRestaurantDTO[]> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/user/favouriteRestaurants"),
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                FavouriteRestaurantDTO[].class
                );
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteFavouriteRestaurant_Successful() {
        userRepo.saveAndFlush(anotherUser);
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@user.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);
    
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        var savedRestaurant = restaurantRepo.saveAndFlush(restaurant);
        Set<Restaurant> favRestaurant = new HashSet<>();
        favRestaurant.add(restaurant);
        anotherUser.setFavouriteRestaurants(favRestaurant);
        userRepo.saveAndFlush(anotherUser);

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/user/favourite/{restaurantId}"),
                HttpMethod.DELETE,
                new HttpEntity<Object>(headers),
                String.class,
                savedRestaurant.getRestaurantId()
                );
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteFavouriteRestaurant_RestaurantNotFoundInFavourites_Failure() {
        userRepo.saveAndFlush(anotherUser);
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@user.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);
    
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        Restaurant anotherRestaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        restaurantRepo.saveAndFlush(restaurant);
        var anotherSavedRestaurant = restaurantRepo.saveAndFlush(anotherRestaurant);
        Set<Restaurant> favRestaurant = new HashSet<>();
        favRestaurant.add(restaurant);
        anotherUser.setFavouriteRestaurants(favRestaurant);
        userRepo.saveAndFlush(anotherUser);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/user/favourite/{restaurantId}"),
                HttpMethod.DELETE,
                new HttpEntity<Object>(headers),
                Void.class,
                anotherSavedRestaurant.getRestaurantId()
                );
        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteFavouriteRestaurant_UserHaveNoFavourites_Failure() {
        userRepo.saveAndFlush(anotherUser);
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@user.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);
    
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        var savedRestaurant = restaurantRepo.saveAndFlush(restaurant);
        Set<Restaurant> favRestaurant = new HashSet<>();
        anotherUser.setFavouriteRestaurants(favRestaurant);
        userRepo.saveAndFlush(anotherUser);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/user/favourite/{restaurantId}"),
                HttpMethod.DELETE,
                new HttpEntity<Object>(headers),
                Void.class,
                savedRestaurant.getRestaurantId()
                );
        assertEquals(404, responseEntity.getStatusCode().value());
    }

    private String createURLWithPort(String uri)
    {
        return baseUrl + port + uri;
    }
}
