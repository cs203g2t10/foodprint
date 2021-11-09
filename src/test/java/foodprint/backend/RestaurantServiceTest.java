package foodprint.backend;

import foodprint.backend.dto.EditFoodDTO;
import foodprint.backend.dto.FoodDTO;
import foodprint.backend.dto.FoodIngredientQuantityDTO;
import foodprint.backend.exceptions.DeleteFailedException;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.exceptions.AlreadyExistsException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        restaurantId = 1L;
        lineItems = new ArrayList<LineItem>();
        reservation = new Reservation(user, LocalDateTime.now(), 5, true, LocalDateTime.now(), ReservationStatus.ONGOING, lineItems, restaurant);
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
        ingredientsDTOList = new ArrayList<>();
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
        reservation = new Reservation(user, LocalDateTime.now(), 5, true, LocalDateTime.now(), ReservationStatus.ONGOING, lineItems, restaurant);
        reservationList.add(reservation);
        startDate = LocalDate.now();
        endDate = LocalDate.now().plusDays(2);
        start = startDate.minusDays(1).atTime(0, 0);
        end = endDate.plusDays(1).atTime(0,0);
        foodIngreQuantitySet = new HashSet<>();
        foodIngredientQuantity = new FoodIngredientQuantity(food, ingredient, 1);
        foodIngreQuantitySet.add(foodIngredientQuantity);
        editFoodDTO = new EditFoodDTO();
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);
        ReflectionTestUtils.setField(ingredient, "ingredientId", ingredientId);
        ReflectionTestUtils.setField(food, "foodId", foodId);
        ReflectionTestUtils.setField(discount, "discountId", discountId);
        ReflectionTestUtils.setField(pic, "pictureId", pictureId);
    }
    //-------Restaurant-related Testing---------\

    @Test
    void getAllRestaurant_RestaurantsExist_ReturnAllRestaurant() {
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
    void updateRestaurant_RestaurantNotFound_ReturnError() {
        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));

        try {
            restaurantService.update(restaurantId, restaurant);
        } catch (NotFoundException e) {
            assertEquals("Restaurant not found", e.getMessage());
        }

        verify(repo).findById(restaurantId);
    }

    @Test
    void deleteRestaurant_RestaurantFound_Return() {
        doNothing().when(repo).delete(any(Restaurant.class));
        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant)).thenReturn(Optional.empty());

        restaurantService.create(restaurant);
        restaurantService.delete(restaurantId);

        verify(repo, times(2)).findById(restaurantId);
        verify(repo).delete(restaurant);
    }

    @Test
    void deleteRestaurant_RestaurantNotFound_ReturnError() {
        when(repo.findById(any(Long.class))).thenReturn(Optional.empty());

        try {
            restaurantService.delete(restaurantId);
        } catch (NotFoundException e) {
            assertEquals("Restaurant not found", e.getMessage());
        }

        verify(repo).findById(restaurantId);
    }

    @Test
    void deleteRestaurant_RestaurantFoundButUnableToDelete_ReturnError() {
        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));
        doNothing().when(repo).delete(any(Restaurant.class));

        try {
            restaurantService.delete(restaurantId);
        } catch(DeleteFailedException e) {
            assertEquals("Restaurant could not be deleted", e.getMessage());
        }

        verify(repo, times(2)).findById(restaurantId);
        verify(repo).delete(restaurant);
    }

    @Test
    void getRestaurant_ExistingRestaurant_ReturnRestaurant() {
        when (repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));

        Restaurant getRestaurant = restaurantService.get(restaurantId);

        assertEquals(restaurant, getRestaurant);
        verify(repo).findById(restaurantId);
    }

    @Test
    void getRestaurant_RestaurantDoesNotExist_ReturnError() {
        when(repo.findById(any(Long.class))).thenReturn(Optional.empty());

        try {
            restaurantService.get(restaurantId);
        } catch (NotFoundException e) {
            assertEquals("Restaurant not found", e.getMessage());
        }

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

    //------------Food-related Testing---------------

    @Test
    void addFood_newFood_ReturnSavedFood() {
        FoodDTO newFoodDTO = new FoodDTO();
        newFoodDTO.setFoodDesc("desc");
        newFoodDTO.setFoodName("Sushi");
        newFoodDTO.setFoodPrice(10.0);
        newFoodDTO.setIngredientQuantityList(ingredientsDTOList);
        Food newFood = new Food(newFoodDTO.getFoodName(), newFoodDTO.getFoodDesc(), newFoodDTO.getFoodPrice(), 10.0);
        allFood.add(newFood);
        
        when(repo.findByRestaurantId(any(Long.class))).thenReturn(restaurant);
        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));
        when(foodRepo.saveAndFlush(any(Food.class))).thenReturn(food);

        restaurantService.create(restaurant);
        Food savedFood = restaurantService.addFood(restaurantId, newFoodDTO);

        //assertSame(savedFood, (Food)(newFoodDTO));
        assertNotNull(savedFood);
        verify(repo).findById(restaurantId);
        verify(repo).findByRestaurantId(restaurantId);
        verify(foodRepo).saveAndFlush(any(Food.class));
    }

    @Test
    void updateFood_FoodExist_ReturnUpdatedFood() {
        when(foodRepo.findById(any(Long.class))).thenReturn(Optional.of(food));
        when(foodRepo.saveAndFlush(any(Food.class))).thenReturn(food);

        restaurantService.create(restaurant);
        restaurantService.editFood(restaurantId, foodId, editFoodDTO);

        verify(foodRepo).findById(foodId);
        verify(foodRepo).saveAndFlush(food);
    }

    @Test
    void updateFood_FoodDoNotExist_Return() {
        EditFoodDTO anotherEditedFood = new EditFoodDTO();
        Long anotherFoodId = 2L;

        when(foodRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        restaurantService.create(restaurant);
        try {
            restaurantService.editFood(restaurantId, anotherFoodId, anotherEditedFood);
        } catch(NotFoundException e) {
            assertEquals("Food requested could not be found", e.getMessage());
        }

        verify(foodRepo).findById(anotherFoodId);
    }

    @Test
    void updateFood_FoodExistButInWrongRestaurant_ReturnError() {
        Long anotherRestaurantId = 2L;

        when(foodRepo.findById(any(Long.class))).thenReturn(Optional.of(food));

        restaurantService.create(restaurant);
        try {
            restaurantService.editFood(anotherRestaurantId, foodId, editFoodDTO);
        } catch (NotFoundException e) {
            assertEquals("Food requested could not be found at this restaurant", e.getMessage());
        }

        verify(foodRepo).findById(foodId);
    }

    @Test
    void deleteFood_FoodExist_Return() {
        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));

        restaurantService.create(restaurant);
        restaurantService.deleteFood(restaurantId, foodId);

        verify(repo).findById(restaurantId);
    }

    @Test
    void deleteFood_FoodDoNotExist_ReturnError() {
        Long anotherFoodId = 2L;

        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));

        restaurantService.create(restaurant);
        restaurant.setAllFood(allFood);
        try {
            restaurantService.deleteFood(restaurantId, anotherFoodId);
        } catch (NotFoundException e) {
            assertEquals("Food not found", e.getMessage());
        }

        verify(repo).findById(restaurantId);
    }

    @Test
    void getFood_FoodExist_ReturnFood() {
        when(foodRepo.findById(any(Long.class))).thenReturn(Optional.of(food));

        restaurantService.create(restaurant);
        Food getFood = restaurantService.getFood(restaurantId, foodId);

        assertEquals(food, getFood);
        verify(foodRepo).findById(foodId);
    }

    @Test
    void getFood_FoodDoNotExist_ReturnError() {
        when(foodRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        restaurantService.create(restaurant);
        try {
            restaurantService.getFood(restaurantId, foodId);
        } catch(NotFoundException e) {
            assertEquals("Food not found", e.getMessage());
        }

        verify(foodRepo).findById(foodId);
    }

    @Test
    void getFood_FoodExistInWrongRestaurant_ReturnError() {
        when(foodRepo.findById(any(Long.class))).thenReturn(Optional.of(food));

        try {
            restaurantService.getFood(2L, foodId);
        } catch(NotFoundException e) {
            assertEquals("Food found but in incorrect restaurant", e.getMessage());
        }
        verify(foodRepo).findById(foodId);
    }

    @Test
    void calculateFoodNeededBetween_Successful_ReturnMap() {
        when(reservationRepo.findByRestaurantAndDateBetween(any(Restaurant.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(reservationList);

        Map<String, Integer> foodMap = restaurantService.calculateFoodNeededBetween(restaurant, startDate, endDate);
        
        assertNotNull(foodMap);
        verify(reservationRepo).findByRestaurantAndDateBetween(restaurant, start, end);
    }

    //----------Discount-related Testing-----------
    @Test
    void createDiscount_newDiscount_ReturnSavedDiscount() {
        Restaurant newRes = new Restaurant("New Restaurant", "Bencoolen");
        when (repo.findByRestaurantId(any(Long.class))).thenReturn(newRes);
        when (discountRepo.saveAndFlush(any(Discount.class))).thenReturn(discount);

        Discount savedDiscount = restaurantService.createDiscount(restaurantId, discount);

        assertEquals(discount, savedDiscount);
        verify(repo).findByRestaurantId(restaurantId);
        verify(discountRepo).saveAndFlush(any(Discount.class));
    }

    @Test
    void createDiscount_DiscountAlreadyExist_ReturnError() {
        when(repo.findByRestaurantId(any(Long.class))).thenReturn(restaurant);

        try {
            restaurantService.createDiscount(restaurantId, discount);
        } catch (AlreadyExistsException e) {
            assertEquals("Discount already exist", e.getMessage());
        }

        verify(repo).findByRestaurantId(restaurantId);
    }

    @Test
    void updateDiscount_DiscountExist_ReturnUpdatedDiscount() {
        when(repo.findByRestaurantId(any(Long.class))).thenReturn(restaurant);
        when(discountRepo.saveAndFlush(any(Discount.class))).thenReturn(discount);

        Discount updatedDiscount = restaurantService.updateDiscount(restaurantId, discount);

        assertEquals(discount, updatedDiscount);
        verify(repo).findByRestaurantId(restaurantId);
        verify(discountRepo).saveAndFlush(discount);
    }

    @Test
    void updateDiscount_DiscountDoNotExist_ReturnError() {
        Restaurant newRes = new Restaurant("New Restaurant", "Bencoolen");
        when(repo.findByRestaurantId(any(Long.class))).thenReturn(newRes);

        try {
            restaurantService.updateDiscount(restaurantId, discount);
        } catch (NotFoundException e) {
            assertEquals("Discount not found", e.getMessage());
        }

        verify(repo).findByRestaurantId(restaurantId);
    }

    @Test
    void getDiscount_DiscountExist_ReturnDiscount() {
        when(discountRepo.findById(any(Long.class))).thenReturn(Optional.of(discount));

        Discount getDiscount = restaurantService.getDiscount(discountId);

        assertEquals(discount, getDiscount);
        verify(discountRepo).findById(discountId);
    }

    @Test
    void getDiscount_DiscountDoNotExist_ReturnError() {
        when(discountRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        try {
            restaurantService.getDiscount(discountId);
        } catch (NotFoundException e){
            assertEquals("Discount not found", e.getMessage());
        }    

        verify(discountRepo).findById(discountId);
    }

    @Test
    void deleteDiscount_DiscountDoNotExist_ReturnError() {
        Restaurant newRes = new Restaurant("New Restaurant", "Bencoolen");
        when(repo.findByRestaurantId(any(Long.class))).thenReturn(newRes);

        try {
            restaurantService.deleteDiscount(restaurantId);
        } catch (NotFoundException e) {
            assertEquals("Discount not found", e.getMessage());
        }

        verify(repo).findByRestaurantId(restaurantId);
    }

    //-------Ingredient-related testing---------
    @Test
    void getAllRestaurantIngredients_RestaurantExistAndIngredientsExist_ReturnIngredients() {
        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));

        List<Ingredient> getIngredients  = restaurantService.getAllRestaurantIngredients(restaurantId);

        assertEquals(ingredients, getIngredients);
        verify(repo).findById(restaurantId);
    }

    @Test
    void getAllRestaurantIngredients_RestaurantDoNotExist_ReturnError() {
        when(repo.findById(any(Long.class))).thenReturn(Optional.empty());

        try {
            restaurantService.getAllRestaurantIngredients(restaurantId);
        } catch(NotFoundException e) {
            assertEquals("Restaurant not found", e.getMessage());
        }

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
    void updateRestaurantIngredient_IngredientNotFound_ReturnError() {
        when(ingredientRepo.findById(any(Long.class))).thenReturn(Optional.empty());
        
        restaurantService.create(restaurant);
        try {
            restaurantService.updateIngredient(restaurantId, ingredientId, ingredient);
        } catch (NotFoundException e) {
            assertEquals("Ingredient requested could not be found", e.getMessage());
        }

        verify(ingredientRepo).findById(ingredientId);
    }

    @Test
    void updateRestaurantIngredient_IngredientFoundInWrongRestaurant_ReturnError() {
        when(ingredientRepo.findById(any(Long.class))).thenReturn(Optional.of(ingredient));
        Restaurant anotherRestaurant = new Restaurant("Sushi Tei", "Desc","Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        Long anotherRestaurantId = 2L;
        ReflectionTestUtils.setField(anotherRestaurant, "restaurantId", anotherRestaurantId);
        
        restaurantService.create(restaurant);
        ingredient.setRestaurant(restaurant);
        try {
            restaurantService.updateIngredient(anotherRestaurantId, ingredientId, ingredient);
        } catch (NotFoundException e) {
            assertEquals("Ingredient requested could not be found at this restaurant", e.getMessage());
        }

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
    void addRestaurantIngredient_RestaurantDoNotExist_ReturnError() {
        when(repo.findById(any(Long.class))).thenReturn(Optional.empty());

        try {
            restaurantService.addRestaurantIngredient(restaurantId, ingredient);
        } catch(NotFoundException e) {
            assertEquals("Restaurant not found", e.getMessage());
        }

        verify(repo).findById(restaurantId);
    }

    @Test
    void deleteRestaurantIngredient_IngredientFoundInCorrectRestaurantAndDeleted_Return() {
        when(ingredientRepo.findById(any(Long.class))).thenReturn(Optional.of(ingredient));
        doNothing().when(ingredientRepo).delete(any(Ingredient.class));

        ingredient.setRestaurant(restaurant);
        restaurantService.deleteRestaurantIngredient(restaurantId, ingredientId);

        verify(ingredientRepo).findById(ingredientId);
        verify(ingredientRepo).delete(ingredient);
    }

    @Test
    void deleteRestaurantIngredient_IngredientNotFound_ReturnError() {
        when(ingredientRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        try {
            restaurantService.deleteRestaurantIngredient(restaurantId, ingredientId);
        } catch (NotFoundException e) {
            assertEquals("Ingredient requested could not be found", e.getMessage());
        }

        verify(ingredientRepo).findById(ingredientId);
    }

    @Test
    void deleteRestaurantIngredient_IngredientNotFoundInCorrectRestaurant_ReturnError() {
        when(ingredientRepo.findById(any(Long.class))).thenReturn(Optional.of(ingredient));

        Restaurant anotherRestaurant = new Restaurant("Sushi Tei", "Desc","Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        Long anotherRestaurantId = 2L;
        ReflectionTestUtils.setField(anotherRestaurant,"restaurantId", anotherRestaurantId);
        ingredient.setRestaurant(restaurant);
        
        try {
            restaurantService.deleteRestaurantIngredient(anotherRestaurantId, ingredientId);
        } catch (NotFoundException e) {
            assertEquals("Ingredient requested could not be found at this restaurant", e.getMessage());
        }

        verify(ingredientRepo).findById(ingredientId);
    }

    @Test
    void calculateIngredientsNeededBetween_Successful_ReturnMap() {
        when(reservationRepo.findByRestaurantAndDateBetween(any(Restaurant.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(reservationList);

        Map<Ingredient, Integer> ingredientMap = restaurantService.calculateIngredientsNeededBetween(restaurant, startDate, endDate);

        assertNotNull(ingredientMap);
        verify(reservationRepo).findByRestaurantAndDateBetween(restaurant, start, end);
    }

}
