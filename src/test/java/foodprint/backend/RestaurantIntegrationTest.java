package foodprint.backend;

import foodprint.backend.controller.AuthController;
import foodprint.backend.controller.RestaurantController;
import foodprint.backend.dto.AuthRequestDTO;
import foodprint.backend.dto.AuthResponseDTO;
import foodprint.backend.dto.RestaurantDTO;
import foodprint.backend.model.Food;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.RestaurantRepo;
import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;
import foodprint.backend.service.RestaurantService;
import foodprint.backend.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.persistence.DiscriminatorValue;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DiscriminatorValue( "null" )
public class RestaurantIntegrationTest {

    @LocalServerPort
	private int port;
    
    private final String baseUrl = "http://localhost:";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RestaurantRepo restaurants;

    @Autowired
    private AuthController authController;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepo userRepo;

    //@MockBean
    @Autowired
    private RestaurantController restaurantController;

    @Autowired
    private RestaurantService restaurantService;

    HttpHeaders headers = new HttpHeaders();
    TestRestTemplate testRestTemplate = new TestRestTemplate();

    @BeforeEach
    void createUser() {
        Optional<User> user = userRepo.findByEmail("bobby@gmail.com");
        if (!user.isEmpty()) {
            userRepo.delete(user.get());
        }
        String encodedPassword = new BCryptPasswordEncoder().encode("SuperSecurePassw0rd");
        User newUser = new User("bobby@gmail.com", encodedPassword, "bobby");
        newUser.setRoles("FP_MANAGER");
        newUser.setRoles("FP_ADMIN");
        ReflectionTestUtils.setField(newUser, "id", 1L);
        newUser.setRegisteredOn(LocalDateTime.now());
        userRepo.saveAndFlush(newUser);
    }

    @AfterEach
    void tearDown() {
        restaurants.deleteAll();
    }
    
    @Test
    public void createRestaurant_Success() throws Exception{
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@gmail.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        System.out.println(loginResponse.getStatus());
        headers.add("Content-Type", "application/json");
        
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        ReflectionTestUtils.setField(restaurant, "restaurantId", 1L);
        if (!restaurants.findById(1L).isEmpty()) {
            restaurants.delete(restaurant);
        }
        HttpEntity<Restaurant> entity = new HttpEntity<>(restaurant, headers);
        ResponseEntity<Restaurant> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant"),
                HttpMethod.POST,
                entity,
                Restaurant.class
            );
        assertEquals(201, responseEntity.getStatusCode().value());
    }

    @Test
    public void getAllRestaurant_Successful() throws Exception {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@gmail.com");
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
        Long restaurantId = 1L;
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);
        restaurants.saveAndFlush(restaurant);

        ResponseEntity<Restaurant[]> responseEntity = testRestTemplate.getForEntity(createURLWithPort("/api/v1/restaurant"), Restaurant[].class);
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void getRestaurant_Successful() throws Exception{
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@gmail.com");
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
        var savedRestaurant = restaurants.saveAndFlush(restaurant);

		ResponseEntity<RestaurantDTO> responseEntity = testRestTemplate.getForEntity(createURLWithPort("/api/v1/restaurant/{restaurantId}"),RestaurantDTO.class, savedRestaurant.getRestaurantId());
        
        assertEquals(200, responseEntity.getStatusCode().value());
        assertEquals(1.0, restaurants.count());
    }

    @Test
    public void getRestaurant_InvalidId_Failure() {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@gmail.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        //HttpEntity<RestaurantDTO> entity = new HttpEntity<>(restaurantDto, headers);
        ResponseEntity<RestaurantDTO> responseEntity = testRestTemplate.getForEntity(createURLWithPort("/api/v1/restaurant/345"), RestaurantDTO.class);
        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void updateRestaurant_Successful() throws Exception {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@gmail.com");
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
        var updatedRestaurant = restaurants.saveAndFlush(restaurant);
        HttpEntity<Restaurant> entity = new HttpEntity<>(restaurant, headers);

        ResponseEntity<Restaurant> responseEntity = testRestTemplate.exchange(createURLWithPort("/api/v1/restaurant/{restaurantId}"), HttpMethod.PATCH, entity, Restaurant.class, updatedRestaurant.getRestaurantId());
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void updateRestaurant_InvalidId_Failure() {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@gmail.com");
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

        HttpEntity<Restaurant> entity = new HttpEntity<>(restaurant, headers);
        ResponseEntity<Restaurant> responseEntity = testRestTemplate.exchange(createURLWithPort("/api/v1/restaurant/{restaurantId}"), HttpMethod.PATCH, entity, Restaurant.class, 345L);
        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteRestaurant_Successful() throws Exception {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@gmail.com");
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
        var currentRestaurantStored = restaurants.saveAndFlush(restaurant);

        HttpEntity<Restaurant> entity = new HttpEntity<>(restaurant, headers);

        ResponseEntity<Restaurant> responseEntity = testRestTemplate.exchange(createURLWithPort("/api/v1/restaurant/{restaurantId}"), HttpMethod.DELETE, entity, Restaurant.class, currentRestaurantStored.getRestaurantId());
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteRestaurant_InvalidId_Failure() {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@gmail.com");
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
        HttpEntity<Restaurant> entity = new HttpEntity<>(restaurant, headers);

        ResponseEntity<Restaurant> responseEntity = testRestTemplate.exchange(createURLWithPort("/api/v1/restaurant/{restaurantId}"), HttpMethod.DELETE, entity, Restaurant.class, 2L);
        assertEquals(404, responseEntity.getStatusCode().value());
    }

    /**
     * 
     * Food-related integration test
     * 
     */

    private String createURLWithPort(String uri)
    {
        return baseUrl + port + uri;
    }
}
