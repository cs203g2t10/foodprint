package foodprint.backend;

import foodprint.backend.dto.AuthRequestDTO;
import foodprint.backend.dto.AuthResponseDTO;
import foodprint.backend.dto.EditFoodDTO;
import foodprint.backend.dto.IngredientDTO;
import foodprint.backend.model.FoodRepo;
import foodprint.backend.model.Ingredient;
import foodprint.backend.model.IngredientRepo;
import foodprint.backend.model.Picture;
import foodprint.backend.model.PictureRepo;
import foodprint.backend.model.ReservationRepo;
import foodprint.backend.dto.RestaurantDTO;
import foodprint.backend.model.Discount;
import foodprint.backend.model.DiscountRepo;
import foodprint.backend.model.Food;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.RestaurantRepo;
import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.persistence.DiscriminatorValue;

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


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DiscriminatorValue( "null" )
public class RestaurantIntegrationTest {

    @LocalServerPort
	private int port;
    
    private final String baseUrl = "http://localhost:";

    @Autowired
    private RestaurantRepo restaurants;

    @Autowired
    private FoodRepo foodRepo;

    @Autowired
    private DiscountRepo discountRepo;

    @Autowired
    private IngredientRepo ingredientRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PictureRepo pictureRepo;

    @Autowired
    private ReservationRepo reservationRepo;

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
        //newUser.setRoles("FP_MANAGER");
        newUser.setRoles("FP_ADMIN");
        newUser.setRegisteredOn(LocalDateTime.now());
        userRepo.saveAndFlush(newUser);
    }

    @AfterEach
    void tearDown() {
        restaurants.deleteAll();
        foodRepo.deleteAll();
        ingredientRepo.deleteAll();
        discountRepo.deleteAll();
        userRepo.deleteAll();
        pictureRepo.deleteAll();
        reservationRepo.deleteAll();
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
        restaurants.saveAndFlush(restaurant);

        ResponseEntity<Restaurant[]> responseEntity = testRestTemplate.getForEntity(
                createURLWithPort("/api/v1/restaurant"),
                Restaurant[].class);
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

		ResponseEntity<RestaurantDTO> responseEntity = testRestTemplate.getForEntity(
                createURLWithPort("/api/v1/restaurant/{restaurantId}"),
                RestaurantDTO.class, 
                savedRestaurant.getRestaurantId());
        
        assertEquals(200, responseEntity.getStatusCode().value());
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

        ResponseEntity<RestaurantDTO> responseEntity = testRestTemplate.getForEntity(
                createURLWithPort("/api/v1/restaurant/345"), 
                RestaurantDTO.class);
        
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

        ResponseEntity<Restaurant> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}"), 
                HttpMethod.PATCH, 
                entity, 
                Restaurant.class, 
                updatedRestaurant.getRestaurantId());
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
        var savedRestaurant = restaurants.saveAndFlush(restaurant);
        Long savedRestaurantId = savedRestaurant.getRestaurantId();
        restaurants.delete(restaurant);

        HttpEntity<Restaurant> entity = new HttpEntity<>(restaurant, headers);
        ResponseEntity<Restaurant> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}"), 
                HttpMethod.PATCH, 
                entity, 
                Restaurant.class, 
                savedRestaurantId);
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

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}"), 
                HttpMethod.DELETE, 
                new HttpEntity<>(headers), 
                Void.class, 
                currentRestaurantStored.getRestaurantId());
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteRestaurant_InvalidId_Failure() throws Exception{
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
        Long savedRestaurantId = savedRestaurant.getRestaurantId();
        restaurants.delete(restaurant);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}"), 
                HttpMethod.DELETE, 
                new HttpEntity<>(headers), 
                Void.class, 
                savedRestaurantId);
        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void getRestaurantCategories_Successful() {
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
        restaurants.saveAndFlush(restaurant);

        ResponseEntity<String[]> responseEntity = testRestTemplate.getForEntity(
                createURLWithPort("/api/v1/restaurant/categories"),
                String[].class);
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void getRestaurantRelatedToCategory_Successful() {
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

        ResponseEntity<RestaurantDTO[]> responseEntity = testRestTemplate.getForEntity(
                createURLWithPort("/api/v1/restaurant/categories/{category}"),
                RestaurantDTO[].class,
                savedRestaurant.getRestaurantCategory()
                );
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    /**
     * 
     * Food-related integration test
     * 
     */

    // @Test
    // public void createFood_Success() throws Exception{
    //     AuthRequestDTO loginRequest = new AuthRequestDTO();
    //     loginRequest.setEmail("bobby@gmail.com");
    //     loginRequest.setPassword("SuperSecurePassw0rd");
    //     AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

    //     HttpHeaders headers = new HttpHeaders();
    //     headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    //     headers.add("Authorization", "Bearer " + loginResponse.getToken());
    //     headers.add("Content-Type", "application/json");

    //     Food food = new Food();
    //     Ingredient ingredient = new Ingredient("Burger");
    //     List<FoodIngredientQuantityDTO> ingredientsDTOList = new ArrayList<>();
    //     FoodDTO foodDTO = new FoodDTO();
    //     FoodIngredientQuantityDTO ingredientQuantity = new FoodIngredientQuantityDTO();
    //     ingredientsDTOList.add(ingredientQuantity);
    //     foodDTO.setIngredientQuantityList(ingredientsDTOList);
    //     foodDTO.setFoodName("name");
    //     foodDTO.setFoodPrice(10.0);
    //     foodDTO.setFoodDesc("desc");
    //     FoodIngredientQuantity foodIngredientQuantity = new FoodIngredientQuantity(food, ingredient, ingredientQuantity.getQuantity());
    //     Set<FoodIngredientQuantity> foodIngredientQuantitySet = new HashSet<FoodIngredientQuantity>();
    //     foodIngredientQuantitySet.add(foodIngredientQuantity);
    //     food.setFoodIngredientQuantity(foodIngredientQuantitySet);
    //     food.setFoodDesc(foodDTO.getFoodDesc());
    //     food.setFoodName(foodDTO.getFoodName());
    //     food.setFoodPrice(foodDTO.getFoodPrice());
    //     List<String> restaurantCategories = new ArrayList<>();
    //     restaurantCategories.add("Japanese");
    //     restaurantCategories.add("Rice");
        
    //     Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
    //     var savedRestaurant = restaurants.saveAndFlush(restaurant);

    //     HttpEntity<FoodDTO> entity = new HttpEntity<FoodDTO>(foodDTO, headers);
    //     ResponseEntity<Food> responseEntity = testRestTemplate.exchange(
    //             createURLWithPort("/api/v1/restaurant/{restaurantId}/food"),
    //             HttpMethod.POST,
    //             entity,
    //             Food.class,
    //             savedRestaurant.getRestaurantId()
    //         );
    //     assertEquals(201, responseEntity.getStatusCode().value());
    // }

    @Test
    public void getAllFood_Successful() throws Exception{
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
        Food food = new Food("sashimi", 10.0, 0.0);
        food.setRestaurant(restaurant);
        var savedRestaurant = restaurants.saveAndFlush(restaurant);
        foodRepo.saveAndFlush(food);

        ResponseEntity<Food[]> responseEntity = testRestTemplate.getForEntity(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/food"), 
                Food[].class, 
                savedRestaurant.getRestaurantId());
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void getFood_Successful() {
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
        Food food = new Food("sashimi", 10.0, 0.0);
        food.setRestaurant(restaurant);
        var savedRestaurant = restaurants.saveAndFlush(restaurant);
        var savedFood = foodRepo.saveAndFlush(food);

        ResponseEntity<Food> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/food/{foodId}"), 
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                Food.class,
                savedRestaurant.getRestaurantId(),
                savedFood.getFoodId()
                );

        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void getFood_FoodNotFound_Failure() {
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
        Food food = new Food("sashimi", 10.0, 0.0);
        food.setRestaurant(restaurant);
        var savedRestaurant = restaurants.saveAndFlush(restaurant);
        var savedFood = foodRepo.saveAndFlush(food);
        Long foodId = savedFood.getFoodId();
        foodRepo.delete(food);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/food/{foodId}"), 
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Void.class,
                savedRestaurant.getRestaurantId(),
                foodId
                );

        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void getFood_FoodFoundInWrongRestaurant_Failure() {
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
        Restaurant anotherRestaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        Food food = new Food("sashimi", 10.0, 0.0);
        restaurants.saveAndFlush(anotherRestaurant);
        food.setRestaurant(anotherRestaurant);
        var savedRestaurant = restaurants.saveAndFlush(restaurant);
        var savedFood = foodRepo.saveAndFlush(food);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/food/{foodId}"), 
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Void.class,
                savedRestaurant.getRestaurantId(),
                savedFood.getFoodId()
                );

        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteFood_Successful() throws Exception{
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
        Food food = new Food("sashimi", 10.0, 0.0);
        food.setRestaurant(restaurant);
        var savedRestaurant = restaurants.saveAndFlush(restaurant);
        var savedFood = foodRepo.saveAndFlush(food);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/food/{foodId}"), 
                HttpMethod.DELETE, 
                new HttpEntity<Object>(headers), 
                Void.class,
                savedRestaurant.getRestaurantId(), 
                savedFood.getFoodId());
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteFood_InvalidFoodId_Failure() throws Exception{
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
        Food food = new Food("sashimi", 10.0, 0.0);
        var savedFood = foodRepo.saveAndFlush(food);
        var savedRestaurant = restaurants.saveAndFlush(restaurant);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/food/{foodId}"), 
                HttpMethod.DELETE, 
                new HttpEntity<Object>(headers), 
                Void.class,
                savedRestaurant.getRestaurantId(), 
                savedFood.getFoodId()
                );
        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteFood_InvalidRestaurantId_Failure() throws Exception{
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
        Food food = new Food("sashimi", 10.0, 0.0);
        var savedFood = foodRepo.saveAndFlush(food);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/food/{foodId}"), 
                HttpMethod.DELETE, 
                new HttpEntity<Object>(headers), 
                Void.class, 
                savedRestaurant.getRestaurantId(), 
                savedFood.getFoodId()
                );
        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void updateFood_Success() throws Exception{
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
        Food food = new Food("sashimi", 10.0, 0.0);
        food.setRestaurant(restaurant);
        var savedRestaurant = restaurants.saveAndFlush(restaurant);
        var savedFood = foodRepo.saveAndFlush(food);
        EditFoodDTO editFoodDTO = new EditFoodDTO();
        editFoodDTO.setFoodDesc("desc");
        editFoodDTO.setFoodName("name");

        HttpEntity<EditFoodDTO> entity = new HttpEntity<EditFoodDTO>(editFoodDTO, headers);
        ResponseEntity<Food> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/food/{foodId}"), 
                HttpMethod.PATCH, 
                entity, 
                Food.class,
                savedRestaurant.getRestaurantId(), 
                savedFood.getFoodId()
                );
        System.out.println(responseEntity.getStatusCode());
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void updateFood_InvalidFoodId_Failure() throws Exception{
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
        Food food = new Food("sashimi", 10.0, 0.0);
        food.setRestaurant(restaurant);
        var savedRestaurant = restaurants.saveAndFlush(restaurant);
        Restaurant anotherRestaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        Food anotherFood = new Food("sashimi", 10.0, 0.0);
        anotherFood.setRestaurant(anotherRestaurant);
        restaurants.saveAndFlush(anotherRestaurant);
        var savedAnotherFood = foodRepo.saveAndFlush(anotherFood);
        EditFoodDTO editFoodDTO = new EditFoodDTO();
        editFoodDTO.setFoodDesc("desc");
        editFoodDTO.setFoodName("name");

        HttpEntity<EditFoodDTO> entity = new HttpEntity<EditFoodDTO>(editFoodDTO, headers);
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/food/{foodId}"), 
                HttpMethod.PATCH, 
                entity, 
                Void.class,
                savedRestaurant.getRestaurantId(), 
                savedAnotherFood.getFoodId()
                );
        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void updateFood_InvalidRestaurantId_Failure() throws Exception{
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
        Food food = new Food("sashimi", 10.0, 0.0);
        food.setRestaurant(restaurant);
        restaurants.saveAndFlush(restaurant);
        var savedFood = foodRepo.saveAndFlush(food);
        Restaurant anotherRestaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        var savedAnotherRestaurant = restaurants.saveAndFlush(anotherRestaurant);
        EditFoodDTO editFoodDTO = new EditFoodDTO();
        editFoodDTO.setFoodDesc("desc");
        editFoodDTO.setFoodName("name");
        
        HttpEntity<EditFoodDTO> entity = new HttpEntity<EditFoodDTO>(editFoodDTO, headers);
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/api/v1/restaurant/{restaurantId}/food/{foodId}"), 
            HttpMethod.PATCH, 
            entity, 
            Void.class, 
            savedAnotherRestaurant.getRestaurantId(), 
            savedFood.getFoodId()
            );
        assertEquals(404, responseEntity.getStatusCode().value());
    }

    /**
     * 
     * Discount-related test
     * 
     */

    @Test
    public void createDiscount_Successful() throws Exception{
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
        Discount discount = new Discount("desc", 10);
        
        HttpEntity<Discount> entity = new HttpEntity<>(discount, headers);
        ResponseEntity<Discount> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/discount"),
                HttpMethod.POST,
                entity,
                Discount.class,
                savedRestaurant.getRestaurantId()
                );
        assertEquals(201, responseEntity.getStatusCode().value());
    }

    @Test
    public void createDiscount_DiscountAlreadyExist_Failure() throws Exception{
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
        Discount discount = new Discount("desc", 10);
        discount.setRestaurant(restaurant);
        var savedRestaurant = restaurants.saveAndFlush(restaurant);
        discountRepo.saveAndFlush(discount);
        
        HttpEntity<Discount> entity = new HttpEntity<>(discount, headers);
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/discount"),
                HttpMethod.POST,
                entity,
                Void.class,
                savedRestaurant.getRestaurantId()
                );

        assertEquals(400, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteDiscount_DiscountExist_Successful() throws Exception{
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
        Discount discount = new Discount("desc", 10);
        discount.setRestaurant(restaurant);
        var savedRestaurant = restaurants.saveAndFlush(restaurant);
        discountRepo.saveAndFlush(discount);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/discount"), 
                HttpMethod.DELETE, 
                new HttpEntity<>(headers), 
                Void.class,
                savedRestaurant.getRestaurantId()
                );

        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteDiscount_RestaurantNotFound_Failure() throws Exception{
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
        Long restaurantId = savedRestaurant.getRestaurantId();
        restaurants.delete(restaurant);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/discount"), 
                HttpMethod.DELETE, 
                new HttpEntity<>(headers), 
                Void.class,
                restaurantId
                );

        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteDiscount_DiscountNotFound_Failure() throws Exception{
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

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/discount"), 
                HttpMethod.DELETE, 
                new HttpEntity<>(headers), 
                Void.class,
                savedRestaurant.getRestaurantId()
                );

        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void updateDiscount_DiscountExist_Successful() throws Exception{
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
        Discount discount = new Discount("desc", 10);
        discount.setRestaurant(restaurant);
        var savedRestaurant = restaurants.saveAndFlush(restaurant);
        discountRepo.saveAndFlush(discount);

        HttpEntity<Discount> entity = new HttpEntity<>(discount, headers);
        ResponseEntity<Discount> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/discount"), 
                HttpMethod.PATCH, 
                entity, 
                Discount.class,
                savedRestaurant.getRestaurantId()
                );
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void updateDiscount_DiscountDoNotExist_Failure() throws Exception{
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
        Discount discount = new Discount("desc", 10);
        var savedRestaurant = restaurants.saveAndFlush(restaurant);

        HttpEntity<Discount> entity = new HttpEntity<>(discount, headers);
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/discount"), 
                HttpMethod.PATCH, 
                entity, 
                Void.class,
                savedRestaurant.getRestaurantId()
                );
        assertEquals(404, responseEntity.getStatusCode().value());
    }

    /**
     * 
     * Ingredient-related test
     * 
     */

     @Test
     public void createIngredient_Successful() throws Exception{
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
        IngredientDTO ingredientDTO = new IngredientDTO();
        ingredientDTO.setIngredientDesc("ingredientDesc");
        ingredientDTO.setIngredientName("ingredientName");
        ingredientDTO.setUnits("units");
        var savedRestaurant = restaurants.saveAndFlush(restaurant);

        HttpEntity<IngredientDTO> entity = new HttpEntity<>(ingredientDTO, headers);
        ResponseEntity<Ingredient> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/ingredient"),
                HttpMethod.POST,
                entity,
                Ingredient.class,
                savedRestaurant.getRestaurantId()
                );
        assertEquals(201, responseEntity.getStatusCode().value());
    }

    @Test
    public void createIngredient_RestaurantDoNotExist_Failure() throws Exception{
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
        IngredientDTO ingredientDTO = new IngredientDTO();
        ingredientDTO.setIngredientDesc("ingredientDesc");
        ingredientDTO.setIngredientName("ingredientName");
        ingredientDTO.setUnits("units");
        var savedRestaurant = restaurants.saveAndFlush(restaurant);
        Long restaurantId = savedRestaurant.getRestaurantId();
        restaurants.delete(savedRestaurant);

        HttpEntity<IngredientDTO> entity = new HttpEntity<>(ingredientDTO, headers);
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/ingredient"),
                HttpMethod.POST,
                entity,
                Void.class,
                restaurantId
                );

       assertEquals(404, responseEntity.getStatusCode().value());
   }

    @Test
    public void getAllIngredients_Successful() throws Exception{
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
        List<Ingredient> ingredientsList = new ArrayList<>();
        Ingredient ingredient = new Ingredient("ingredientName");
        ingredient.setIngredientDesc("ingredientDesc");
        ingredientsList.add(ingredient);
        restaurant.setIngredients(ingredientsList);
        var savedRestaurant = restaurants.saveAndFlush(restaurant);
        ingredientRepo.saveAndFlush(ingredient);

        ResponseEntity<Ingredient[]> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/ingredient/all"), 
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                Ingredient[].class,
                savedRestaurant.getRestaurantId()
                );
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void getAllIngredients_RestaurantDoNotExist_Failure() throws Exception{
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
        Long restaurantId = savedRestaurant.getRestaurantId();
        restaurants.delete(restaurant);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/ingredient/all"), 
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                Void.class,
                restaurantId
                );

        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void modifyIngredient_Successful() throws Exception{
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
        IngredientDTO ingredientDTO = new IngredientDTO();
        ingredientDTO.setIngredientDesc("ingredientDesc");
        ingredientDTO.setIngredientName("ingredientName");
        ingredientDTO.setUnits("1");
        Ingredient ingredient = new Ingredient("ingredientName");
        ingredient.setIngredientDesc("ingredientDesc");
        ingredient.setRestaurant(restaurant);
        var savedRestaurant = restaurants.saveAndFlush(restaurant);
        var savedIngredient = ingredientRepo.saveAndFlush(ingredient);


        HttpEntity<IngredientDTO> entity = new HttpEntity<>(ingredientDTO, headers);
        ResponseEntity<Ingredient> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/ingredient/{ingredientId}"),
                HttpMethod.PATCH, 
                entity, 
                Ingredient.class, 
                savedRestaurant.getRestaurantId(), 
                savedIngredient.getIngredientId()
                );

        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void modifyIngredient_IngredientNotFound_Failure() throws Exception{
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
        IngredientDTO ingredientDTO = new IngredientDTO();
        ingredientDTO.setIngredientDesc("ingredientDesc");
        ingredientDTO.setIngredientName("ingredientName");
        ingredientDTO.setUnits("1");
        Ingredient ingredient = new Ingredient("ingredientName");
        ingredient.setIngredientDesc("ingredientDesc");
        ingredient.setRestaurant(restaurant);
        var savedRestaurant = restaurants.saveAndFlush(restaurant);
        var savedIngredient = ingredientRepo.saveAndFlush(ingredient);
        Long savedIngredientId = savedIngredient.getIngredientId();
        ingredientRepo.delete(ingredient);

        HttpEntity<IngredientDTO> entity = new HttpEntity<>(ingredientDTO, headers);
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/ingredient/{ingredientId}"),
                HttpMethod.PATCH, 
                entity, 
                Void.class, 
                savedRestaurant.getRestaurantId(), 
                savedIngredientId
                );

        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void modifyIngredient_RestaurantNotFound_Failure() throws Exception{
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
        IngredientDTO ingredientDTO = new IngredientDTO();
        ingredientDTO.setIngredientDesc("ingredientDesc");
        ingredientDTO.setIngredientName("ingredientName");
        ingredientDTO.setUnits("1");
        Ingredient ingredient = new Ingredient("ingredientName");
        ingredient.setIngredientDesc("ingredientDesc");
        ingredient.setRestaurant(restaurant);
        restaurants.saveAndFlush(restaurant);
        var anotherSavedRestaurant = restaurants.saveAndFlush(new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories));
        var savedIngredient = ingredientRepo.saveAndFlush(ingredient);

        HttpEntity<IngredientDTO> entity = new HttpEntity<>(ingredientDTO, headers);
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/ingredient/{ingredientId}"),
                HttpMethod.PATCH, 
                entity, 
                Void.class, 
                anotherSavedRestaurant.getRestaurantId(), 
                savedIngredient.getIngredientId()
                );

        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteIngredient_Successful() throws Exception{
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
        IngredientDTO ingredientDTO = new IngredientDTO();
        ingredientDTO.setIngredientDesc("ingredientDesc");
        ingredientDTO.setIngredientName("ingredientName");
        ingredientDTO.setUnits("1");
        Ingredient ingredient = new Ingredient("ingredientName");
        ingredient.setIngredientDesc("ingredientDesc");
        ingredient.setRestaurant(restaurant);
        var savedRestaurant = restaurants.saveAndFlush(restaurant);
        var savedIngredient = ingredientRepo.saveAndFlush(ingredient);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/ingredient/{ingredientId}"),
                HttpMethod.DELETE, 
                new HttpEntity<Object>(headers),
                Void.class, 
                savedRestaurant.getRestaurantId(), 
                savedIngredient.getIngredientId()
                );
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteIngredient_InvalidIngredientId_Failure() throws Exception{
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
        IngredientDTO ingredientDTO = new IngredientDTO();
        ingredientDTO.setIngredientDesc("ingredientDesc");
        ingredientDTO.setIngredientName("ingredientName");
        ingredientDTO.setUnits("1");
        Ingredient ingredient = new Ingredient("ingredientName");
        ingredient.setIngredientDesc("ingredientDesc");
        ingredient.setRestaurant(restaurant);
        var savedRestaurant = restaurants.saveAndFlush(restaurant);
        var savedIngredient = ingredientRepo.saveAndFlush(ingredient);
        Long savedIngredientId = savedIngredient.getIngredientId();
        ingredientRepo.delete(ingredient);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/ingredient/{ingredientId}"),
                HttpMethod.DELETE, 
                new HttpEntity<Object>(headers),
                Void.class, 
                savedRestaurant.getRestaurantId(), 
                savedIngredientId
                );
        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteIngredient_InvalidRestaurantId_Failure() throws Exception{
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
        IngredientDTO ingredientDTO = new IngredientDTO();
        ingredientDTO.setIngredientDesc("ingredientDesc");
        ingredientDTO.setIngredientName("ingredientName");
        ingredientDTO.setUnits("1");
        Ingredient ingredient = new Ingredient("ingredientName");
        ingredient.setIngredientDesc("ingredientDesc");
        ingredient.setRestaurant(restaurant);
        restaurants.saveAndFlush(restaurant);
        var savedIngredient = ingredientRepo.saveAndFlush(ingredient);
        var anotherSavedRestaurant = restaurants.saveAndFlush(new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories));

        HttpEntity<Ingredient> entity = new HttpEntity<Ingredient>(ingredient, headers);
        ResponseEntity<Ingredient> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/ingredient/{ingredientId}"),
                HttpMethod.DELETE, 
                entity,
                Ingredient.class, 
                anotherSavedRestaurant.getRestaurantId(), 
                savedIngredient.getIngredientId()
                );
        assertEquals(404, responseEntity.getStatusCode().value());
    }

    /**
     * 
     * Picture-related testing 
     * 
     */

    @Test
    public void deleteRestaurantPicture_Successful() {
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
        Picture picture = new Picture("title", "description", "imagePath", "imageFileName", "url");
        restaurant.setPicture(picture);
        var savedRestaurant = restaurants.saveAndFlush(restaurant);
        pictureRepo.saveAndFlush(picture);

        HttpEntity<Picture> entity = new HttpEntity<>(null, headers);
        ResponseEntity<Picture> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/picture"),
                HttpMethod.DELETE,
                entity,
                Picture.class,
                savedRestaurant.getRestaurantId()
                );
        
        assertEquals(200, responseEntity.getStatusCode().value());
   }

    @Test
    public void deleteRestaurantPicture_PictureNotFound_Failure() {
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

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/picture"),
                HttpMethod.DELETE,
                new HttpEntity<Object>(headers),
                Void.class,
                savedRestaurant.getRestaurantId()
                );
        
        assertEquals(404, responseEntity.getStatusCode().value());
   }

   @Test
   public void deleteRestaurantPicture_RestaurantNotFound_Failure() {
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
       Long restaurantId = savedRestaurant.getRestaurantId();
       restaurants.delete(restaurant);

       ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
               createURLWithPort("/api/v1/restaurant/{restaurantId}/picture"),
               HttpMethod.DELETE,
               new HttpEntity<Object>(headers),
               Void.class,
               restaurantId
               );
       
       assertEquals(404, responseEntity.getStatusCode().value());
  }

    @Test
    public void deleteFoodPicture_Successful() {
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
        Food food = new Food("sashimi", 10.0, 0.0);
        Picture picture = new Picture("title", "description", "imagePath", "imageFileName", "url");
        pictureRepo.saveAndFlush(picture);
        food.setPicture(picture);
        food.setRestaurant(restaurant);
        var savedFood = foodRepo.saveAndFlush(food);

        ResponseEntity<Picture> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/food/{foodId}/picture"),
                HttpMethod.DELETE,
                new HttpEntity<Object>(headers),
                Picture.class,
                savedRestaurant.getRestaurantId(),
                savedFood.getFoodId()
                );
       
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteFoodPicture_FoodPictureNotFound_Failure() {
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
        Food food = new Food("sashimi", 10.0, 0.0);
        food.setRestaurant(restaurant);
        var savedFood = foodRepo.saveAndFlush(food);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/food/{foodId}/picture"),
                HttpMethod.DELETE,
                new HttpEntity<Object>(headers),
                Void.class,
                savedRestaurant.getRestaurantId(),
                savedFood.getFoodId()
                );
       
        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteFoodPicture_FoodNotFound_Failure() {
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
        Food food = new Food("sashimi", 10.0, 0.0);
        food.setRestaurant(restaurant);
        var savedFood = foodRepo.saveAndFlush(food);
        Long foodId = savedFood.getFoodId();
        foodRepo.delete(food);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/food/{foodId}/picture"),
                HttpMethod.DELETE,
                new HttpEntity<Object>(headers),
                Void.class,
                savedRestaurant.getRestaurantId(),
                foodId
                );
       
        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteFoodPicture_FoodFoundInWrongRestaurant_Failure() {
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
        Restaurant anotherRestaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        var savedRestaurant = restaurants.saveAndFlush(restaurant);
        restaurants.saveAndFlush(anotherRestaurant);
        Food food = new Food("sashimi", 10.0, 0.0);
        food.setRestaurant(anotherRestaurant);
        var savedFood = foodRepo.saveAndFlush(food);
        
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/restaurant/{restaurantId}/food/{foodId}/picture"),
                HttpMethod.DELETE,
                new HttpEntity<Object>(headers),
                Void.class,
                savedRestaurant.getRestaurantId(),
                savedFood.getFoodId()
                );
       
        assertEquals(404, responseEntity.getStatusCode().value());
  }
    
    private String createURLWithPort(String uri)
    {
        return baseUrl + port + uri;
    }
}
