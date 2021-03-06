package foodprint.backend;

import foodprint.backend.dto.EditFoodDTO;
import foodprint.backend.dto.FoodDTO;
import foodprint.backend.dto.FoodIngredientQuantityDTO;
import foodprint.backend.exceptions.DeleteFailedException;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.exceptions.AlreadyExistsException;
import foodprint.backend.exceptions.BadRequestException;
import foodprint.backend.model.Discount;
import foodprint.backend.model.DiscountRepo;
import foodprint.backend.model.Food;
import foodprint.backend.model.FoodIngredientQuantity;
import foodprint.backend.model.FoodRepo;
import foodprint.backend.model.Ingredient;
import foodprint.backend.model.IngredientRepo;
import foodprint.backend.model.Picture;
import foodprint.backend.model.PictureRepo;
import foodprint.backend.model.Reservation;
import foodprint.backend.model.ReservationRepo;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.RestaurantRepo;
import foodprint.backend.model.User;
import foodprint.backend.service.PictureService;
import foodprint.backend.service.RestaurantService;
import foodprint.backend.model.Reservation.ReservationStatus;
import foodprint.backend.model.LineItem;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class RestaurantServiceTest {

    @Mock
    private RestaurantRepo repo;

    @Mock
    private FoodRepo foodRepo;

    @Mock
    private DiscountRepo discountRepo;

    @Mock
    private IngredientRepo ingredientRepo;

    @Mock
    private PictureRepo pictureRepo;

    @Mock
    private ReservationRepo reservationRepo;

    @InjectMocks
    private RestaurantService restaurantService;

    @InjectMocks
    private PictureService pictureService;

    private User user;
    private Restaurant restaurant;
    private Long restaurantId;
    private Food food;
    private Long foodId;
    private Ingredient ingredient;
    private List<String> restaurantCategories;
    private List<Reservation> reservationList;
    private List<LineItem> lineItems;
    private LineItem lineItem;
    private Reservation reservation;
    private List<Restaurant> allRestaurants;
    private List<Food> allFood;
    private Discount discount;
    private Long discountId;
    private List<Ingredient> ingredients;
    private Long ingredientId;
    private Picture pic;
    private List<Picture> picList;
    private Long pictureId;
    private List<FoodIngredientQuantityDTO> ingredientsDTOList;
    private LocalDateTime start;
    private LocalDate startDate;
    private LocalDateTime end;
    private LocalDate endDate;
    private EditFoodDTO editFoodDTO;
    private Set<FoodIngredientQuantity> foodIngreQuantitySet;
    private FoodIngredientQuantity foodIngredientQuantity;

    @BeforeEach
    void init() {
        restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10,
                restaurantCategories);
        restaurantId = 1L;
        lineItems = new ArrayList<LineItem>();
        reservation = new Reservation(user, LocalDateTime.now(), 5, true, LocalDateTime.now(),
                ReservationStatus.UNPAID, lineItems, restaurant);
        ingredientsDTOList = new ArrayList<>();
        food = new Food("sashimi", 10.0, 0.0);
        foodId = 1L;
        lineItem = new LineItem(food, reservation, 1);
        lineItems.add(lineItem);
        reservationList = new ArrayList<>();
        allRestaurants = new ArrayList<>();
        allRestaurants.add(restaurant);
        allFood = new ArrayList<>();
        allFood.add(food);
        ingredient = new Ingredient("Salmon");
        ingredients = new ArrayList<>();
        ingredients.add(ingredient);
        ingredientId = 1L;
        discount = new Discount("1 For 1", 30);
        discountId = 1L;
        pic = new Picture("image", "desc", "Path", "file", "www.file.com");
        pictureId = 2L;
        picList = new ArrayList<>();
        picList.add(pic);
        food.setRestaurant(restaurant);
        food.setPicture(pic);
        restaurant.setIngredients(ingredients);
        restaurant.setDiscount(discount);
        discount.setRestaurant(restaurant);
        restaurant.setAllFood(allFood);
        restaurant.setPicture(pic);
        reservationList = new ArrayList<>();
        reservation = new Reservation(user, LocalDateTime.now().plusDays(1), 5, true, LocalDateTime.now(),
                ReservationStatus.UNPAID, lineItems, restaurant);
        reservationList.add(reservation);
        startDate = LocalDate.now();
        endDate = LocalDate.now().plusDays(2);
        start = startDate.minusDays(1).atTime(0, 0);
        end = endDate.plusDays(1).atTime(0, 0);
        foodIngreQuantitySet = new HashSet<>();
        foodIngredientQuantity = new FoodIngredientQuantity(food, ingredient, 1);
        foodIngreQuantitySet.add(foodIngredientQuantity);
        food.setFoodIngredientQuantity(foodIngreQuantitySet);
        editFoodDTO = new EditFoodDTO();
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);
        ReflectionTestUtils.setField(ingredient, "ingredientId", ingredientId);
        ReflectionTestUtils.setField(food, "foodId", foodId);
        ReflectionTestUtils.setField(discount, "discountId", discountId);
        ReflectionTestUtils.setField(pic, "pictureId", pictureId);
    }
    // --------Restaurant-related Testing--------

    @Test
    void getAllRestaurants_RestaurantsExist_ReturnAllRestaurants() {
        allRestaurants.add(restaurant);

        when(repo.findAll()).thenReturn(allRestaurants);

        List<Restaurant> restaurants = restaurantService.getAllRestaurants();

        assertEquals(allRestaurants, restaurants);
        verify(repo).findAll();
    }

    @Test
    void addRestaurant_NewRestaurant_ReturnSavedRestaurant() {
        when(repo.saveAndFlush(any(Restaurant.class))).thenReturn(restaurant);

        Restaurant createdRestaurant = restaurantService.create(restaurant);

        assertEquals(restaurant, createdRestaurant);
        verify(repo).saveAndFlush(restaurant);
    }

    @Test
    void updateRestaurant_RestaurantFound_ReturnUpdatedRestaurant() {
        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));
        when(repo.saveAndFlush(any(Restaurant.class))).thenReturn(restaurant);

        Restaurant updatedRestaurant = restaurantService.update(restaurantId, restaurant);

        assertEquals(restaurant, updatedRestaurant);
        verify(repo).findById(restaurantId);
        verify(repo).saveAndFlush(restaurant);
    }

    @Test
    void updateRestaurant_RestaurantNotFound_ReturnException() {
        when(repo.findById(any(Long.class))).thenReturn(Optional.empty());

        String exceptionMsg = "";
        try {
            restaurantService.update(restaurantId, restaurant);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }
        assertEquals("Restaurant not found", exceptionMsg);
        verify(repo).findById(restaurantId);
    }

    @Test
    void deleteRestaurant_RestaurantFound_Success() {
        doNothing().when(repo).delete(any(Restaurant.class));
        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant)).thenReturn(Optional.empty());

        String exceptionMsg = "";
        try {
            restaurantService.delete(restaurantId);
        } catch (Exception e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("", exceptionMsg);
        verify(repo, times(2)).findById(restaurantId);
        verify(repo).delete(restaurant);
    }

    @Test
    void deleteRestaurant_RestaurantNotFound_ReturnException() {
        when(repo.findById(any(Long.class))).thenReturn(Optional.empty());

        String exceptionMsg = "";
        try {
            restaurantService.delete(restaurantId);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Restaurant not found", exceptionMsg);
        verify(repo).findById(restaurantId);
    }

    @Test
    void deleteRestaurant_RestaurantFoundButUnableToDelete_ReturnException() {
        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));
        doNothing().when(repo).delete(any(Restaurant.class));

        String exceptionMsg = "";
        try {
            restaurantService.delete(restaurantId);
        } catch (DeleteFailedException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Restaurant could not be deleted", exceptionMsg);
        verify(repo, times(2)).findById(restaurantId);
        verify(repo).delete(restaurant);
    }

    @Test
    void getRestaurant_RestaurantExists_ReturnRestaurant() {
        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));

        Restaurant getRestaurant = restaurantService.get(restaurantId);

        assertEquals(restaurant, getRestaurant);
        verify(repo).findById(restaurantId);
    }

    @Test
    void getRestaurant_RestaurantDoesNotExist_ReturnException() {
        when(repo.findById(any(Long.class))).thenReturn(Optional.empty());

        String exceptionMsg = "";
        try {
            restaurantService.get(restaurantId);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Restaurant not found", exceptionMsg);
        verify(repo).findById(restaurantId);
    }

    @Test
    void getCategories_CategoriesFound_ReturnListOfCategories() {
        when(repo.findAll()).thenReturn(allRestaurants);

        List<String> getCategories = restaurantService.getCategories();

        assertEquals(restaurantCategories, getCategories);
        verify(repo).findAll();
    }

    @Test
    void getRestaurants_RestaurantsRelatedToCategoriesFound_ReturnRestaurantsRelated() {
        String category = "Japanese";

        when(repo.findAll()).thenReturn(allRestaurants);

        List<Restaurant> getRestaurantsRelatedToCategory = restaurantService.getRestaurantsRelatedToCategory(category);

        assertEquals(allRestaurants, getRestaurantsRelatedToCategory);
        verify(repo).findAll();
    }

    // -----------Food-related Testing-----------

    @Test
    void addFood_newFoodHasIngredient_ReturnFood() {
        FoodDTO newFoodDTO = new FoodDTO();
        newFoodDTO.setFoodDesc("desc");
        newFoodDTO.setFoodName("Sushi");
        newFoodDTO.setFoodPrice(10.0);
        Food newFood = new Food(newFoodDTO.getFoodName(), newFoodDTO.getFoodDesc(), newFoodDTO.getFoodPrice(), 10.0);

        FoodIngredientQuantityDTO foodIngredientQuantityDTO = new FoodIngredientQuantityDTO();
        foodIngredientQuantityDTO.setIngredientId(ingredient.getIngredientId());
        foodIngredientQuantityDTO.setQuantity(2);
        ingredientsDTOList.add(foodIngredientQuantityDTO);
        newFoodDTO.setIngredientQuantityList(ingredientsDTOList);

        allFood.add(newFood);

        when(repo.findByRestaurantId(any(Long.class))).thenReturn(restaurant);
        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));
        when(foodRepo.saveAndFlush(any(Food.class))).thenReturn(newFood);

        restaurantService.create(restaurant);
        Food savedFood = restaurantService.addFood(restaurantId, newFoodDTO);

        assertEquals(newFood, savedFood);
        verify(repo).findById(restaurantId);
        verify(repo).findByRestaurantId(restaurantId);
        verify(foodRepo).saveAndFlush(any(Food.class));
    }

    @Test
    void addFood_NewFoodNoIngredient_ReturnBadRequest() {
        FoodDTO newFoodDTO = new FoodDTO();
        newFoodDTO.setFoodDesc("desc");
        newFoodDTO.setFoodName("Sushi");
        newFoodDTO.setFoodPrice(10.0);
        Food newFood = new Food(newFoodDTO.getFoodName(), newFoodDTO.getFoodDesc(), newFoodDTO.getFoodPrice(), 10.0);
        newFoodDTO.setIngredientQuantityList(ingredientsDTOList);

        allFood.add(newFood);

        restaurantService.create(restaurant);
        String exceptionMsg = "";
        try {
            restaurantService.addFood(restaurantId, newFoodDTO);
        } catch(BadRequestException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Food should have at least 1 ingredient", exceptionMsg);
    }

    @Test
    void updateFood_FoodExists_ReturnUpdatedFood() {
        when(foodRepo.findById(any(Long.class))).thenReturn(Optional.of(food));
        when(foodRepo.saveAndFlush(any(Food.class))).thenReturn(food);

        restaurantService.create(restaurant);
        Food rslt = restaurantService.editFood(restaurantId, foodId, editFoodDTO);

        assertEquals(food,rslt);
        verify(foodRepo).findById(foodId);
        verify(foodRepo).saveAndFlush(food);
    }

    @Test
    void updateFood_FoodDoesNotExist_ReturnException() {
        EditFoodDTO anotherEditedFood = new EditFoodDTO();
        Long anotherFoodId = 2L;

        when(foodRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        restaurantService.create(restaurant);
        String exceptionMsg = "";
        try {
            restaurantService.editFood(restaurantId, anotherFoodId, anotherEditedFood);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Food requested could not be found", exceptionMsg);
        verify(foodRepo).findById(anotherFoodId);
    }

    @Test
    void updateFood_FoodExistButInWrongRestaurant_ReturnException() {
        Long anotherRestaurantId = 2L;

        when(foodRepo.findById(any(Long.class))).thenReturn(Optional.of(food));

        restaurantService.create(restaurant);
        String exceptionMsg = "";
        try {
            restaurantService.editFood(anotherRestaurantId, foodId, editFoodDTO);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Food requested could not be found at this restaurant", exceptionMsg);
        verify(foodRepo).findById(foodId);
    }

    @Test
    void deleteFood_FoodExist_Success() {
        when(foodRepo.findById(any(Long.class)))
                .thenReturn(Optional.of(food))
                .thenReturn(Optional.empty());

        doNothing().when(foodRepo).delete(food);
        String exceptionMsg = "";
        try {
            restaurantService.deleteFood(restaurantId, foodId);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }
        
        assertEquals("", exceptionMsg);
        verify(foodRepo, times(2)).findById(foodId);
        verify(foodRepo).delete(food);
    }

    @Test
    void deleteFood_FoodDoNotExist_ReturnException() {
        Long anotherFoodId = 2L;

        when(foodRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        String exceptionMsg = "";
        try {
            restaurantService.deleteFood(restaurantId, anotherFoodId);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Food not found", exceptionMsg);
        verify(foodRepo).findById(anotherFoodId);
    }

    @Test
    void getFood_FoodExists_ReturnFood() {
        when(foodRepo.findById(any(Long.class))).thenReturn(Optional.of(food));

        restaurantService.create(restaurant);
        Food getFood = restaurantService.getFood(restaurantId, foodId);

        assertEquals(food, getFood);
        verify(foodRepo).findById(foodId);
    }

    @Test
    void getFood_FoodDoesNotExist_ReturnException() {
        when(foodRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        restaurantService.create(restaurant);
        String exceptionMsg = "";
        try {
            restaurantService.getFood(restaurantId, foodId);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Food not found", exceptionMsg);
        verify(foodRepo).findById(foodId);
    }

    @Test
    void getFood_FoodExistsInWrongRestaurant_ReturnException() {
        when(foodRepo.findById(any(Long.class))).thenReturn(Optional.of(food));

        String exceptionMsg = "";
        try {
            restaurantService.getFood(2L, foodId);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Food found but in incorrect restaurant", exceptionMsg);
        verify(foodRepo).findById(foodId);
    }

    @Test
    void calculateFoodNeededBetween_Success_ReturnMap() {
        when(reservationRepo.findByRestaurantAndDateBetween(any(Restaurant.class), any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(reservationList);

        Map<String, Integer> foodMap = restaurantService.calculateFoodNeededBetween(restaurant, startDate, endDate);

        assertTrue(foodMap.size() > 0);
        verify(reservationRepo).findByRestaurantAndDateBetween(restaurant, start, end);
    }

    // ---------Discount-related Testing---------
    @Test
    void createDiscount_NewDiscount_ReturnSavedDiscount() {
        Restaurant newRes = new Restaurant("New Restaurant", "Bencoolen");
        when(repo.findByRestaurantId(any(Long.class))).thenReturn(newRes);
        when(discountRepo.saveAndFlush(any(Discount.class))).thenReturn(discount);

        Discount savedDiscount = restaurantService.createDiscount(restaurantId, discount);

        assertEquals(discount, savedDiscount);
        verify(repo).findByRestaurantId(restaurantId);
        verify(discountRepo).saveAndFlush(any(Discount.class));
    }

    @Test
    void createDiscount_DiscountAlreadyExist_ReturnException() {
        when(repo.findByRestaurantId(any(Long.class))).thenReturn(restaurant);

        String exceptionMsg = "";
        try {
            restaurantService.createDiscount(restaurantId, discount);
        } catch (AlreadyExistsException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Discount already exist", exceptionMsg);
        verify(repo).findByRestaurantId(restaurantId);
    }

    @Test
    void updateDiscount_DiscountExists_ReturnUpdatedDiscount() {
        when(repo.findByRestaurantId(any(Long.class))).thenReturn(restaurant);
        when(discountRepo.saveAndFlush(any(Discount.class))).thenReturn(discount);

        Discount updatedDiscount = restaurantService.updateDiscount(restaurantId, discount);

        assertEquals(discount, updatedDiscount);
        verify(repo).findByRestaurantId(restaurantId);
        verify(discountRepo).saveAndFlush(discount);
    }

    @Test
    void updateDiscount_DiscountDoesNotExist_ReturnException() {
        Restaurant newRes = new Restaurant("New Restaurant", "Bencoolen");
        when(repo.findByRestaurantId(any(Long.class))).thenReturn(newRes);

        String exceptionMsg = "";
        try {
            restaurantService.updateDiscount(restaurantId, discount);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Discount not found", exceptionMsg);
        verify(repo).findByRestaurantId(restaurantId);
    }

    @Test
    void getDiscount_DiscountExistse_ReturnDiscount() {
        when(discountRepo.findById(any(Long.class))).thenReturn(Optional.of(discount));

        Discount getDiscount = restaurantService.getDiscount(discountId);

        assertEquals(discount, getDiscount);
        verify(discountRepo).findById(discountId);
    }

    @Test
    void getDiscount_DiscountDoesNotExist_ReturnException() {
        when(discountRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        String exceptionMsg = "";
        try {
            restaurantService.getDiscount(discountId);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Discount not found", exceptionMsg);
        verify(discountRepo).findById(discountId);
    }

    @Test
    void deleteDiscount_DiscountDoesNotExist_ReturnException() {
        Restaurant newRes = new Restaurant("New Restaurant", "Bencoolen");
        when(repo.findByRestaurantId(any(Long.class))).thenReturn(newRes);

        String exceptionMsg = "";
        try {
            restaurantService.deleteDiscount(restaurantId);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Discount not found", exceptionMsg);
        verify(repo).findByRestaurantId(restaurantId);
    }

    // --------Ingredient-related testing--------
    @Test
    void getAllRestaurantIngredients_RestaurantExistAndIngredientsExist_ReturnIngredients() {
        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));

        List<Ingredient> getIngredients = restaurantService.getAllRestaurantIngredients(restaurantId);

        assertEquals(ingredients, getIngredients);
        verify(repo).findById(restaurantId);
    }

    @Test
    void getAllRestaurantIngredients_RestaurantDoesNotExist_ReturnException() {
        when(repo.findById(any(Long.class))).thenReturn(Optional.empty());

        String exceptionMsg = "";
        try {
            restaurantService.getAllRestaurantIngredients(restaurantId);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Restaurant not found", exceptionMsg);
        verify(repo).findById(restaurantId);
    }

    @Test
    void updateRestaurantIngredient_IngredientFoundInCorrectRestaurantAndUpdated_ReturnIngredient() {
        when(ingredientRepo.findById(any(Long.class))).thenReturn(Optional.of(ingredient));
        when(ingredientRepo.saveAndFlush(any(Ingredient.class))).thenReturn(ingredient);

        restaurantService.create(restaurant);
        ingredient.setRestaurant(restaurant);

        Ingredient updateIngredient = restaurantService.updateIngredient(restaurantId, ingredientId, ingredient);

        assertEquals(ingredient, updateIngredient);
        verify(ingredientRepo).findById(ingredientId);
        verify(ingredientRepo).saveAndFlush(ingredient);
    }

    @Test
    void updateRestaurantIngredient_IngredientNotFound_ReturnException() {
        when(ingredientRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        restaurantService.create(restaurant);
        String exceptionMsg = "";
        try {
            restaurantService.updateIngredient(restaurantId, ingredientId, ingredient);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Ingredient requested could not be found", exceptionMsg);
        verify(ingredientRepo).findById(ingredientId);
    }

    @Test
    void updateRestaurantIngredient_IngredientFoundInWrongRestaurant_ReturnException() {
        when(ingredientRepo.findById(any(Long.class))).thenReturn(Optional.of(ingredient));
        Restaurant anotherRestaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10,
                10, restaurantCategories);
        Long anotherRestaurantId = 2L;
        ReflectionTestUtils.setField(anotherRestaurant, "restaurantId", anotherRestaurantId);

        restaurantService.create(restaurant);
        ingredient.setRestaurant(restaurant);
        String exceptionMsg = "";
        try {
            restaurantService.updateIngredient(anotherRestaurantId, ingredientId, ingredient);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Ingredient requested could not be found at this restaurant", exceptionMsg);
        verify(ingredientRepo).findById(ingredientId);
    }

    @Test
    void addRestaurantIngredient_RestaurantExist_ReturnIngredient() {
        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));
        when(ingredientRepo.saveAndFlush(any(Ingredient.class))).thenReturn(ingredient);
        when(repo.saveAndFlush(any(Restaurant.class))).thenReturn(restaurant);

        Ingredient addIngredient = restaurantService.addRestaurantIngredient(restaurantId, ingredient);

        assertEquals(ingredient, addIngredient);
        verify(repo).findById(restaurantId);
        verify(ingredientRepo).saveAndFlush(ingredient);
        verify(repo).saveAndFlush(restaurant);
    }

    @Test
    void addRestaurantIngredient_RestaurantDoesNotExist_ReturnException() {
        when(repo.findById(any(Long.class))).thenReturn(Optional.empty());

        String exceptionMsg = "";
        try {
            restaurantService.addRestaurantIngredient(restaurantId, ingredient);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Restaurant not found", exceptionMsg);
        verify(repo).findById(restaurantId);
    }

    @Test
    void deleteRestaurantIngredient_IngredientFoundInCorrectRestaurantAndDeleted_Success() {
        ingredient.setRestaurant(restaurant);
        when(ingredientRepo.findById(any(Long.class))).thenReturn(Optional.of(ingredient)).thenReturn(Optional.empty());
        doNothing().when(ingredientRepo).delete(any(Ingredient.class));
        
        String exceptionMsg = "";
        try {
            restaurantService.deleteRestaurantIngredient(restaurantId, ingredientId);
        } catch (Exception e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("", exceptionMsg);
        verify(ingredientRepo, times(2)).findById(ingredientId);
        verify(ingredientRepo).delete(ingredient);
    }

    @Test
    void deleteRestaurantIngredient_IngredientNotFound_ReturnException() {
        when(ingredientRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        String exceptionMsg = "";
        try {
            restaurantService.deleteRestaurantIngredient(restaurantId, ingredientId);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Ingredient requested could not be found", exceptionMsg);
        verify(ingredientRepo).findById(ingredientId);
    }

    @Test
    void deleteRestaurantIngredient_IngredientNotFoundInCorrectRestaurant_ReturnException() {
        when(ingredientRepo.findById(any(Long.class))).thenReturn(Optional.of(ingredient));

        Restaurant anotherRestaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10,
                10, restaurantCategories);
        Long anotherRestaurantId = 2L;
        ReflectionTestUtils.setField(anotherRestaurant, "restaurantId", anotherRestaurantId);
        ingredient.setRestaurant(restaurant);

        String exceptionMsg = "";
        try {
            restaurantService.deleteRestaurantIngredient(anotherRestaurantId, ingredientId);
        } catch (NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Ingredient requested could not be found at this restaurant", exceptionMsg);
        verify(ingredientRepo).findById(ingredientId);
    }

    @Test
    void calculateIngredientsNeededBetween_Success_ReturnMap() {
        when(reservationRepo.findByRestaurantAndDateBetween(any(Restaurant.class), any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(reservationList);

        Map<Ingredient, Integer> ingredientMap = restaurantService.calculateIngredientsNeededBetween(restaurant,
                startDate, endDate);

        assertTrue(ingredientMap.size() > 0);
        verify(reservationRepo).findByRestaurantAndDateBetween(restaurant, start, end);
    }

}
