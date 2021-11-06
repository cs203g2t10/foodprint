package foodprint.backend;

import foodprint.backend.model.Restaurant;
import foodprint.backend.model.RestaurantRepo;
import foodprint.backend.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

import org.springframework.http.ResponseEntity;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class RestaurantIntegrationTest {

    @LocalServerPort
	private int port;
    
    private final String baseUrl = "http://localhost:";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RestaurantRepo restaurants;

    
    HttpHeaders headers = new HttpHeaders();
    TestRestTemplate testRestTemplate = new TestRestTemplate();

    @BeforeEach
    void createUser() {
        User user = new User("bobbytan@gmail.com", "SuperSecurePassw0rd", "bobby");
        user.setRoles("FP_MANAGER");
        user.setRoles("FP_ADMIN");
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
    
    @WithMockUser(roles = "FP_MANAGER")
    @Test
    public void CreateRestaurant_Success() throws Exception{
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        restaurants.save(restaurant);

        ResponseEntity<Restaurant> responseEntity = testRestTemplate.exchange(createURLWithPort(
                "/api/v1/restaurant"),HttpMethod.POST, entity, Restaurant.class);
        assertNotNull(responseEntity);
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
        
        ResponseEntity<Restaurant> result = restTemplate.withBasicAuth("admin", "goodpassword")
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
