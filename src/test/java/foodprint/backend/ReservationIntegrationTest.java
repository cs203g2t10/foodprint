package foodprint.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.persistence.DiscriminatorValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import foodprint.backend.dto.AuthRequestDTO;
import foodprint.backend.dto.AuthResponseDTO;
import foodprint.backend.dto.CreateReservationDTO;
import foodprint.backend.dto.LineItemDTO;
import foodprint.backend.dto.ReservationDTO;
import foodprint.backend.model.Food;
import foodprint.backend.model.FoodRepo;
import foodprint.backend.model.LineItem;
import foodprint.backend.model.LineItemRepo;
import foodprint.backend.model.Picture;
import foodprint.backend.model.PictureRepo;
import foodprint.backend.model.Reservation;
import foodprint.backend.model.ReservationRepo;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.RestaurantRepo;
import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;
import foodprint.backend.model.Reservation.ReservationStatus;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DiscriminatorValue( "null" )
public class ReservationIntegrationTest {
    
    @LocalServerPort
	private int port;
    
    private final String baseUrl = "http://localhost:";

    @Autowired
    private ReservationRepo reservationRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RestaurantRepo restaurantRepo;

    @Autowired
    private PictureRepo pictureRepo;

    @Autowired
    private LineItemRepo lineItemRepo;

    @Autowired
    private FoodRepo foodRepo;
    
    private User newUser;
    private User admin;

    HttpHeaders headers = new HttpHeaders();
    TestRestTemplate testRestTemplate = new TestRestTemplate();

    @BeforeEach
    void createUser() {
        Optional<User> user = userRepo.findByEmail("bobby@gmail.com");
        if (!user.isEmpty()) {
            userRepo.delete(user.get());
        }
        String encodedPassword = new BCryptPasswordEncoder().encode("SuperSecurePassw0rd");
        newUser = new User("bobby@gmail.com", encodedPassword, "bobby");
        newUser.setRoles("FP_USER");
        newUser.setRegisteredOn(LocalDateTime.now());
        admin = new User("bobby@admin.com", encodedPassword, "bobby");
        admin.setRoles("FP_ADMIN");
        admin.setRegisteredOn(LocalDateTime.now());
        userRepo.saveAndFlush(newUser);
        userRepo.saveAndFlush(admin);
    }

    @AfterEach
    void tearDown() {
        reservationRepo.deleteAll();
        lineItemRepo.deleteAll();
        userRepo.deleteAll();
        restaurantRepo.deleteAll();
        foodRepo.deleteAll();
    }

    @Test
    public void getReservation_UserAndReservationFound_Successful() throws Exception{
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@gmail.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        Picture picture = new Picture("title", "description", "imagePath", "imageFileName", "url");
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        restaurant.setPicture(picture);
        restaurantRepo.saveAndFlush(restaurant);
        pictureRepo.saveAndFlush(picture);

        Reservation reservation = new Reservation(newUser, LocalDateTime.now(), 1, true, LocalDateTime.now().plusDays(1), ReservationStatus.PAID, restaurant);
        var savedReservation = reservationRepo.saveAndFlush(reservation);

        ResponseEntity<ReservationDTO> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/reservation/{reservationId}"),
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                ReservationDTO.class,
                savedReservation.getReservationId()
                );
        assertEquals(responseEntity.getBody().getReservationId(), savedReservation.getReservationId());
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void getReservation_ReservationNotFound_Failure() throws Exception{
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@gmail.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        Picture picture = new Picture("title", "description", "imagePath", "imageFileName", "url");
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        restaurant.setPicture(picture);
        restaurantRepo.saveAndFlush(restaurant);

        Reservation reservation = new Reservation(newUser, LocalDateTime.now(), 1, true, LocalDateTime.now().plusDays(1), ReservationStatus.PAID, restaurant);
        var savedReservation = reservationRepo.saveAndFlush(reservation);
        Long reservationId = savedReservation.getReservationId();
        reservationRepo.delete(reservation);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/reservation/{reservationId}"),
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                Void.class,
                reservationId
                );
        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void getAllReservationByUser_UserAndReservationFound_Success() throws Exception{
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@gmail.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);
        System.out.println(loginResponse.getToken());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        Picture picture = new Picture("title", "description", "imagePath", "imageFileName", "url");
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        restaurant.setPicture(picture);
        restaurantRepo.saveAndFlush(restaurant);

        Reservation reservation = new Reservation(newUser, LocalDateTime.now(), 1, true, LocalDateTime.now().plusDays(1), ReservationStatus.PAID, restaurant);
        reservationRepo.saveAndFlush(reservation);

        ResponseEntity<ReservationDTO[]> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/reservation/all"),
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                ReservationDTO[].class
                );
                //check size
        assertEquals(responseEntity.getBody().length, reservationRepo.count());
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    // @Test
    // public void getAllReservationByUser_Failure() throws Exception{
    //     AuthRequestDTO loginRequest = new AuthRequestDTO();
    //     loginRequest.setEmail("bobby@gmail.com");
    //     loginRequest.setPassword("SuperSecurePassw0rd");
    //     AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);
    //     System.out.println(loginResponse.getToken());

    //     HttpHeaders headers = new HttpHeaders();
    //     headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    //     headers.add("Authorization", "Bearer " + loginResponse.getToken());
    //     headers.add("Content-Type", "application/json");

    //     Picture picture = new Picture("title", "description", "imagePath", "imageFileName", "url");
    //     List<String> restaurantCategories = new ArrayList<>();
    //     restaurantCategories.add("Japanese");
    //     restaurantCategories.add("Rice");
    //     Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
    //     restaurant.setPicture(picture);
    //     restaurantRepo.saveAndFlush(restaurant);

    //     Reservation reservation = new Reservation(newUser, LocalDateTime.now(), 1, true, LocalDateTime.now().plusDays(1), ReservationStatus.PAID, restaurant);
    //     reservationRepo.saveAndFlush(reservation);

    //     ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
    //             createURLWithPort("/api/v1/reservation/all"),
    //             HttpMethod.GET,
    //             new HttpEntity<Object>(headers),
    //             Void.class
    //             );

    //     assertEquals(200, responseEntity.getStatusCode().value());
    // }

    @Test
    public void getUserUpcomingReservations_UserFound_Successful() throws Exception{
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@gmail.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        Picture picture = new Picture("title", "description", "imagePath", "imageFileName", "url");
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        restaurant.setPicture(picture);
        restaurantRepo.saveAndFlush(restaurant);

        Reservation reservation = new Reservation(newUser, LocalDateTime.now().plusDays(5), 1, true, LocalDateTime.now(), ReservationStatus.PAID, restaurant);
        Reservation reservation2 = new Reservation(newUser, LocalDateTime.now().minusDays(5), 1, true, LocalDateTime.now().minusDays(15), ReservationStatus.PAID, restaurant);
        reservationRepo.saveAndFlush(reservation);
        reservationRepo.saveAndFlush(reservation2);

        ResponseEntity<ReservationDTO[]> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/reservation/upcoming"),
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                ReservationDTO[].class
                );
        assertEquals(1, responseEntity.getBody().length);
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void getUserPastReservations_UserFound_Successful() throws Exception{
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@gmail.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        Picture picture = new Picture("title", "description", "imagePath", "imageFileName", "url");
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        restaurant.setPicture(picture);
        restaurantRepo.saveAndFlush(restaurant);

        Reservation reservation = new Reservation(newUser, LocalDateTime.now().plusDays(5), 1, true, LocalDateTime.now(), ReservationStatus.PAID, restaurant);
        Reservation reservation2 = new Reservation(newUser, LocalDateTime.now().minusDays(5), 1, true, LocalDateTime.now().minusDays(15), ReservationStatus.PAID, restaurant);
        reservationRepo.saveAndFlush(reservation);
        reservationRepo.saveAndFlush(reservation2);

        ResponseEntity<ReservationDTO[]> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/reservation/past"),
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                ReservationDTO[].class
                );
        assertEquals(1, responseEntity.getBody().length);
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void getAllReservation_ReservationSlotsFound_Successful() {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@admin.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        Picture picture = new Picture("title", "description", "imagePath", "imageFileName", "url");
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        restaurant.setPicture(picture);
        restaurantRepo.saveAndFlush(restaurant);

        Food food = new Food("Salmon", 10.0, 0.0);
        food.setFoodDesc("foodDesc");
        foodRepo.saveAndFlush(food);
        List<LineItem> list = new ArrayList<>();
        LineItem lineItem = new LineItem();
        lineItem.setFood(food);
        lineItem.setQuantity(1);
        lineItemRepo.saveAndFlush(lineItem);
        Reservation reservation = new Reservation(newUser, LocalDateTime.now(), 1, true, LocalDateTime.now().plusDays(1), ReservationStatus.PAID,list, restaurant);
        reservationRepo.saveAndFlush(reservation);

        ResponseEntity<Reservation[]> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/api/v1/reservation/admin/all"),
            HttpMethod.GET,
            new HttpEntity<Object>(headers),
            Reservation[].class
            );
        assertEquals(responseEntity.getBody().length, reservationRepo.count());
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void createReservationDTO_UserFoundAndReservationCreated_Successful() {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@gmail.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        Picture picture = new Picture("title", "description", "imagePath", "imageFileName", "url");
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 9, 10, 12, 12, 9, 10, 12, 12, restaurantCategories);
        restaurant.setPicture(picture);
        var savedRestaurant = restaurantRepo.saveAndFlush(restaurant);

        Food food = new Food("Salmon", 10.0, 0.0);
        food.setFoodDesc("foodDesc");
        food.setRestaurant(restaurant);
        var savedFood = foodRepo.saveAndFlush(food);

        List<LineItemDTO> lineItemDTOList = new ArrayList<>();
        LineItemDTO lineItemDTO = new LineItemDTO(savedFood.getFoodId(), 1000);
        lineItemDTOList.add(lineItemDTO);
        CreateReservationDTO createReservationDTO = new CreateReservationDTO();
        createReservationDTO.setDate(LocalDateTime.of(LocalDate.now().plusDays(14), LocalTime.NOON.minusHours(2)));
        createReservationDTO.setIsVaccinated(true);
        createReservationDTO.setLineItems(lineItemDTOList);
        createReservationDTO.setPax(1);
        createReservationDTO.setRestaurantId(savedRestaurant.getRestaurantId());
        createReservationDTO.setStatus(ReservationStatus.PAID);

        HttpEntity<CreateReservationDTO> entity = new HttpEntity<>(createReservationDTO, headers);
        ResponseEntity<ReservationDTO> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/api/v1/reservation"),
            HttpMethod.POST,
            entity,
            ReservationDTO.class
            );
        assertEquals(responseEntity.getBody().getLineItems().get(0).getQuantity(), lineItemDTOList.get(0).getQuantity());
        assertEquals(201, responseEntity.getStatusCode().value());
    }

    @Test
    public void createReservationDTO_DateOfReservationNotAllowed_Failure() {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@gmail.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        Picture picture = new Picture("title", "description", "imagePath", "imageFileName", "url");
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 9, 10, 12, 12, 9, 10, 12, 12, restaurantCategories);
        restaurant.setPicture(picture);
        var savedRestaurant = restaurantRepo.saveAndFlush(restaurant);

        Food food = new Food("Salmon", 10.0, 0.0);
        food.setFoodDesc("foodDesc");
        food.setRestaurant(restaurant);
        var savedFood = foodRepo.saveAndFlush(food);

        List<LineItemDTO> lineItemDTOList = new ArrayList<>();
        LineItemDTO lineItemDTO = new LineItemDTO(savedFood.getFoodId(), 1);
        lineItemDTOList.add(lineItemDTO);
        CreateReservationDTO createReservationDTO = new CreateReservationDTO();
        createReservationDTO.setDate(LocalDateTime.now());
        createReservationDTO.setIsVaccinated(true);
        createReservationDTO.setLineItems(lineItemDTOList);
        createReservationDTO.setPax(1);
        createReservationDTO.setRestaurantId(savedRestaurant.getRestaurantId());
        createReservationDTO.setStatus(ReservationStatus.PAID);

        HttpEntity<CreateReservationDTO> entity = new HttpEntity<>(createReservationDTO, headers);
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/api/v1/reservation"),
            HttpMethod.POST,
            entity,
            Void.class
            );
        assertEquals(406, responseEntity.getStatusCode().value());
    }

    @Test
    public void updateReservationDTO_ReservationFound_Successful() {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@gmail.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        Picture picture = new Picture("title", "description", "imagePath", "imageFileName", "url");
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        restaurant.setPicture(picture);
        var savedRestaurant = restaurantRepo.saveAndFlush(restaurant);

        Food food = new Food("Salmon", 10.0, 0.0);
        food.setFoodDesc("foodDesc");
        food.setRestaurant(restaurant);
        var savedFood = foodRepo.saveAndFlush(food);
        Reservation reservation = new Reservation(newUser, LocalDateTime.now(), 1, true, LocalDateTime.now().plusDays(1), ReservationStatus.PAID, restaurant);
        var savedReservation = reservationRepo.saveAndFlush(reservation);

        List<LineItemDTO> lineItemDTOList = new ArrayList<>();
        LineItemDTO lineItemDTO = new LineItemDTO(savedFood.getFoodId(), 1);
        lineItemDTOList.add(lineItemDTO);
        CreateReservationDTO createReservationDTO = new CreateReservationDTO();
        createReservationDTO.setDate(LocalDateTime.now().plusDays(1));
        createReservationDTO.setIsVaccinated(true);
        createReservationDTO.setLineItems(lineItemDTOList);
        createReservationDTO.setPax(1);
        createReservationDTO.setRestaurantId(savedRestaurant.getRestaurantId());
        createReservationDTO.setStatus(ReservationStatus.PAID);
        HttpEntity<CreateReservationDTO> entity = new HttpEntity<>(createReservationDTO, headers);

        ResponseEntity<ReservationDTO> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/api/v1/reservation/{reservationId}"),
            HttpMethod.PATCH,
            entity,
            ReservationDTO.class,
            savedReservation.getReservationId()
            );
        assertEquals(responseEntity.getBody().getReservationId(), savedReservation.getReservationId());
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void updateReservationDTO_ReservationNotFound_Failure() {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@gmail.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        Picture picture = new Picture("title", "description", "imagePath", "imageFileName", "url");
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        restaurant.setPicture(picture);
        var savedRestaurant = restaurantRepo.saveAndFlush(restaurant);

        Food food = new Food("Salmon", 10.0, 0.0);
        food.setFoodDesc("foodDesc");
        food.setRestaurant(restaurant);
        var savedFood = foodRepo.saveAndFlush(food);
        Reservation reservation = new Reservation(newUser, LocalDateTime.now(), 1, true, LocalDateTime.now().plusDays(1), ReservationStatus.PAID, restaurant);
        var savedReservation = reservationRepo.saveAndFlush(reservation);
        reservationRepo.delete(reservation);

        List<LineItemDTO> lineItemDTOList = new ArrayList<>();
        LineItemDTO lineItemDTO = new LineItemDTO(savedFood.getFoodId(), 1);
        lineItemDTOList.add(lineItemDTO);
        CreateReservationDTO createReservationDTO = new CreateReservationDTO();
        createReservationDTO.setDate(LocalDateTime.now().plusDays(1));
        createReservationDTO.setIsVaccinated(true);
        createReservationDTO.setLineItems(lineItemDTOList);
        createReservationDTO.setPax(1);
        createReservationDTO.setRestaurantId(savedRestaurant.getRestaurantId());
        createReservationDTO.setStatus(ReservationStatus.PAID);
        HttpEntity<CreateReservationDTO> entity = new HttpEntity<>(createReservationDTO, headers);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/api/v1/reservation/{reservationId}"),
            HttpMethod.PATCH,
            entity,
            Void.class,
            savedReservation.getReservationId()
            );

        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void getAllReservationByRestaurant_RestaurantAndReservationsFound_Successful() {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@admin.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        Picture picture = new Picture("title", "description", "imagePath", "imageFileName", "url");
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        restaurant.setPicture(picture);
        var savedRestaurant = restaurantRepo.saveAndFlush(restaurant);

        Food food = new Food("Salmon", 10.0, 0.0);
        food.setFoodDesc("foodDesc");
        foodRepo.saveAndFlush(food);
        List<LineItem> list = new ArrayList<>();
        LineItem lineItem = new LineItem();
        lineItem.setFood(food);
        lineItem.setQuantity(1);
        lineItemRepo.saveAndFlush(lineItem);
        Reservation reservation = new Reservation(newUser, LocalDateTime.now(), 1, true, LocalDateTime.now().plusDays(1), ReservationStatus.PAID,list, restaurant);
        reservationRepo.saveAndFlush(reservation);

        ResponseEntity<Reservation[]> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/api/v1/reservation/admin/all/{restaurantId}"),
            HttpMethod.GET,
            new HttpEntity<Object>(headers),
            Reservation[].class,
            savedRestaurant.getRestaurantId()
            );
            assertEquals(responseEntity.getBody().length, reservationRepo.count());
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void getAllReservationByRestaurant_RestaurantNotFound_Failure() {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@admin.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        Picture picture = new Picture("title", "description", "imagePath", "imageFileName", "url");
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        restaurant.setPicture(picture);
        var savedRestaurant = restaurantRepo.saveAndFlush(restaurant);
        Long restaurantId = savedRestaurant.getRestaurantId();
        restaurantRepo.delete(restaurant);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/api/v1/reservation/admin/all/{restaurantId}"),
            HttpMethod.GET,
            new HttpEntity<Object>(headers),
            Void.class,
            restaurantId
            );

        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    void getAllAvailableSlotsByDateAndRestaurant_SlotsFound_Successful(){
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@gmail.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");

        Picture picture = new Picture("title", "description", "imagePath", "imageFileName", "url");
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        restaurant.setPicture(picture);
        var savedRestaurant = restaurantRepo.saveAndFlush(restaurant);

        ResponseEntity<LocalDateTime[]> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/reservation/slots/{restaurantId}"),
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                LocalDateTime[].class,
                savedRestaurant.getRestaurantId()
                );
        assertTrue(LocalDateTime.now().isBefore(responseEntity.getBody()[0]));
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    private String createURLWithPort(String uri)
    {
        return baseUrl + port + uri;
    }
}
