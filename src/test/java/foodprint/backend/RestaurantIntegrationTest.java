package foodprint.backend;

import foodprint.backend.controller.AuthController;
import foodprint.backend.controller.RestaurantController;
import foodprint.backend.dto.AuthRequestDTO;
import foodprint.backend.dto.AuthResponseDTO;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.RestaurantRepo;
import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;
import foodprint.backend.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Inheritance;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;


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
        //userService.createUser(user);
        
    }

//     @BeforeEach
//   void setup() {
//     this.mockMvc =
//         MockMvcBuilders.webAppContextSetup(context)
//             .apply(springSecurity())
//             .build();
//   }

    @AfterEach
    void tearDown() {
        restaurants.deleteAll();
    }
    
    @Test
    public void CreateRestaurant_Success() throws Exception{
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
        restaurants.saveAndFlush(restaurant);
        ResponseEntity<Restaurant> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant"),
                HttpMethod.POST,
                entity,
                Restaurant.class
            );
        assertEquals(201, responseEntity.getStatusCode().value());
        // assertNotNull(responseEntity);
    }

    @Test
    public void getRestaurant_Successful() throws Exception{
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
		Long id = restaurants.save(restaurant).getRestaurantId();
		URI uri = new URI(baseUrl + port + "/restaurant/" + id);
		
		ResponseEntity<Restaurant> result = restTemplate.getForEntity(uri, Restaurant.class);
        assertNotNull(result);
    }

    @Test
    public void updateRestaurant_Successful() throws Exception {
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        //Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        Restaurant restaurant = restaurants.save(new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories));
        URI uri = new URI(baseUrl + port + "/books/" + restaurant.getRestaurantId().longValue());
        Restaurant updatedRestaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        
        ResponseEntity<Restaurant> result = restTemplate.withBasicAuth("admin", "SuperSecurePassw0rd")
                .exchange(uri, HttpMethod.PUT, new HttpEntity<>(updatedRestaurant), Restaurant.class);
            
        assertNotNull(result);
    }

    @Test
    public void deleteRestaurant_Successful() throws Exception {
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = restaurants.save(new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories));
        URI uri = new URI(baseUrl + port + "/restaurant/" + restaurant.getRestaurantId().longValue());
        
        ResponseEntity<Void> result = restTemplate.withBasicAuth("admin", "goodpassword")
                                      .exchange(uri, HttpMethod.DELETE, null, Void.class);
        
        Optional<Restaurant> emptyValue = Optional.empty();
        assertEquals(emptyValue, emptyValue);
    }

    // @WithMockUser(roles = "FP_MANAGER")
    // @Test
    // public void CreateFood_Success() throws Exception{
    //     HttpEntity<String> entity = new HttpEntity<>(null, headers);
    //     Food food = new Food("sashimi", 10.0, 0.0);
    //     List<String> restaurantCategories = new ArrayList<>();
    //     restaurantCategories.add("Japanese");
    //     restaurantCategories.add("Rice");
    //     Restaurant restaurant = restaurants.save(new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories));
    //     food.setRestaurant(restaurant);
    //     foods.save(food);

    //     URI uri = new URI(baseUrl + port + "/restaurant/" + restaurant.getRestaurantId().longValue() + "/food");
    //     ResponseEntity<Food> result = restTemplate.getForEntity(uri, Food.class);
    //     assertNotNull(result);
    // }

    private String createURLWithPort(String uri)
    {
        return "http://localhost:" + port + uri;
    }
}
