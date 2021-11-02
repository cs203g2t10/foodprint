package foodprint.backend;

import foodprint.backend.dto.FoodDTO;
import foodprint.backend.dto.FoodIngredientQuantityDTO;
import foodprint.backend.exceptions.DeleteFailedException;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.model.Discount;
import foodprint.backend.model.DiscountRepo;
import foodprint.backend.model.Food;

import foodprint.backend.model.FoodRepo;
import foodprint.backend.model.Ingredient;
import foodprint.backend.model.IngredientRepo;
import foodprint.backend.model.Picture;
import foodprint.backend.model.PictureRepo;
import foodprint.backend.model.Reservation;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.RestaurantRepo;
import foodprint.backend.model.User;
import foodprint.backend.service.PictureService;
import foodprint.backend.service.RestaurantService;
import foodprint.backend.model.Reservation.ReservationStatus;
import foodprint.backend.model.LineItem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

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
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<Reservation> reservationList;
    private List<LineItem> lineItems;
    private LineItem lineItem;
    private Reservation reservation;
    private Long reservationId;
    private List<Restaurant> allRestaurants;
    private List<String> pictures;
    private List<Food> allFood;
    private Discount discount;
    private Long discountId;
    private List<Ingredient> ingredients;
    private Long ingredientId;
    private Picture pic;
    private List<Picture> picList;
    private Long pictureId;
    private List<FoodIngredientQuantityDTO> ingredientsDTOList;

    @BeforeEach
    void init() {
        restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        restaurantId = 1L;
        lineItems = new ArrayList<LineItem>();
        reservation = new Reservation(user, LocalDateTime.now(), 5, true, LocalDateTime.now(), ReservationStatus.ONGOING, lineItems, restaurant);
        reservationId = 1L;
        ingredientsDTOList = new ArrayList<>();
        food = new Food("sashimi", 10.0, 0.0);
        foodId = 1L;
        lineItem = new LineItem(food, reservation, 1);
        lineItems.add(lineItem);
        startTime = reservation.getDate();
        endTime = startTime.plusHours(1);
        reservationList = new ArrayList<>();
        allRestaurants = new ArrayList<>();
        allRestaurants.add(restaurant);
        pictures = new ArrayList<>();
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
        food.setRestaurant(restaurant);
        food.setPicture(pic);
        restaurant.setIngredients(ingredients);
        discount.setRestaurant(restaurant);
        restaurant.setAllFood(allFood);
        restaurant.setPicture(pic);
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

        assertNotNull(restaurants);
        verify(repo).findAll();
    }

    @Test
    void addRestaurant_NewRestaurant_ReturnSavedRestaurant() {
        when(repo.saveAndFlush(any(Restaurant.class))).thenReturn(restaurant);

        Restaurant createdRestaurant = restaurantService.create(restaurant);

        assertNotNull(createdRestaurant);
        verify(repo).saveAndFlush(restaurant);
    }

    @Test
    void updateRestaurant_RestaurantFound_ReturnUpdatedRestaurant() {
        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));
        when(repo.saveAndFlush(any(Restaurant.class))).thenReturn(restaurant);

        Restaurant updatedRestaurant = restaurantService.update(restaurantId, restaurant);

        assertNotNull(updatedRestaurant);
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
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);

        doNothing().when(repo).delete(any(Restaurant.class));
        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant)).thenReturn(Optional.empty());

        restaurantService.create(restaurant);
        restaurantService.delete(restaurantId);

        assertNotNull(restaurant);
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
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);

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

        assertNotNull(getRestaurant);
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
        assertNotNull(getCategories);
        verify(repo).findAll();
    }

    @Test
    void getRestaurants_RestaurantsRelatedToCategoriesFound_ReturnRestaurantsRelated() {
        String category = "Japanese";

        when(repo.findAll()).thenReturn(allRestaurants);
        
        List<Restaurant> getRestaurantsRelatedToCategory = restaurantService.getRestaurantsRelatedToCategory(category);
        assertNotNull(getRestaurantsRelatedToCategory);
        verify(repo).findAll();
    }

    //------------Food-related Testing---------------

    @Test
    void addFood_newFood_ReturnSavedFood() {
        Food newFood = new Food("Sushi", 30.0, 10.0);
        FoodDTO newFoodDTO = new FoodDTO();
        newFoodDTO.setIngredientQuantityList(ingredientsDTOList);
        allFood.add(newFood);
        
        when(repo.findByRestaurantId(any(Long.class))).thenReturn(restaurant);
        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));
        when(foodRepo.saveAndFlush(any(Food.class))).thenReturn(food);

        restaurantService.create(restaurant);
        Food savedFood = restaurantService.addFood(restaurantId, newFoodDTO);

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
        restaurantService.updateFood(restaurantId, foodId, food);

        verify(foodRepo).findById(foodId);
        verify(foodRepo).saveAndFlush(food);
    }

    @Test
    void updateFood_FoodDoNotExist_Return() {
        Food anotherFood = new Food("Sushi", 20.0, 10.0);
        Long anotherFoodId = 2L;

        when(foodRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        restaurantService.create(restaurant);
        try {
            restaurantService.updateFood(restaurantId, anotherFoodId, anotherFood);
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
            restaurantService.updateFood(anotherRestaurantId, foodId, food);
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

        assertNotNull(getFood);
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

    //----------Discount-related Testing-----------
    @Test
    void addDiscount_newDiscount_ReturnSavedDiscount() {
        when (repo.findByRestaurantId(any(Long.class))).thenReturn(restaurant);
        when (discountRepo.saveAndFlush(any(Discount.class))).thenReturn(discount);

        Discount savedDiscount = restaurantService.addDiscount(restaurantId, discount);

        assertNotNull(savedDiscount);
        verify(repo).findByRestaurantId(restaurantId);
        verify(discountRepo).saveAndFlush(any(Discount.class));
    }

    @Test
    void updateDiscount_DiscountExist_ReturnUpdatedDiscount() {
        when(repo.findByRestaurantId(any(Long.class))).thenReturn(restaurant);
        when(discountRepo.findById(any(Long.class))).thenReturn(Optional.of(discount));
        when(discountRepo.saveAndFlush(any(Discount.class))).thenReturn(discount);

        restaurantService.addDiscount(restaurantId, discount);
        Discount updatedDiscount = restaurantService.updateDiscount(restaurantId, discountId, discount);

        assertNotNull(updatedDiscount);
        verify(discountRepo).findById(discountId);
        verify(discountRepo).saveAndFlush(discount);
    }

    @Test
    void updateDiscount_DiscountDoNotExist_ReturnError() {
        when(discountRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        try {
            restaurantService.updateDiscount(restaurantId, discountId, discount);
        } catch (NotFoundException e) {
            assertEquals("Discount could not be found", e.getMessage());
        }

        verify(discountRepo).findById(discountId);
    }

    @Test
    void updateDiscount_DiscountExistButInWrongRestaurant_ReturnError() {
        when(repo.findByRestaurantId(any(Long.class))).thenReturn(restaurant);
        when(discountRepo.findById(any(Long.class))).thenReturn(Optional.of(discount));

        restaurantService.addDiscount(restaurantId, discount);

        try {
            restaurantService.updateDiscount(4L, discountId, discount);
        } catch (NotFoundException e) {
            assertEquals("Discount found but in incorrect restaurant", e.getMessage());
        }

        verify(repo).findByRestaurantId(restaurantId);
        verify(discountRepo).findById(discountId);
    }

    @Test
    void getDiscount_DiscountExist_ReturnDiscount() {
        when(discountRepo.findById(any(Long.class))).thenReturn(Optional.of(discount));

        Discount getDiscount = restaurantService.getDiscount(discountId);

        assertNotNull(getDiscount);
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
    void deleteDiscount_DiscountExist_Return() {
        when(repo.findByRestaurantId(any(Long.class))).thenReturn(restaurant);
        when(discountRepo.existsById(any(Long.class))).thenReturn(true);
        when(discountRepo.getById(any(Long.class))).thenReturn(discount);

        restaurantService.addDiscount(restaurantId, discount);
        restaurantService.deleteDiscount(restaurantId, discountId);

        assertNotNull(discount);
        verify(discountRepo).getById(discountId);
        verify(discountRepo).existsById(discountId);
        verify(repo).findByRestaurantId(restaurantId);
    }

    @Test
    void deleteDiscount_DiscountDoNotExist_ReturnError() {
        when(discountRepo.existsById(any(Long.class))).thenReturn(false);

        try {
            restaurantService.deleteDiscount(restaurantId, discountId);
        } catch (NotFoundException e) {
            assertEquals("Discount not found", e.getMessage());
        }

        verify(discountRepo).existsById(discountId);
    }

    @Test
    void deleteDiscount_DiscountFoundInWrongRestaurant_ReturnError() {
        Restaurant anotherRestaurant = new Restaurant("Sushi Tei", "Desc","Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        ReflectionTestUtils.setField(anotherRestaurant,"restaurantId", 2L);

        when(discountRepo.existsById(any(Long.class))).thenReturn(true);
        when(discountRepo.getById(any(Long.class))).thenReturn(discount);

        try {
            restaurantService.deleteDiscount(2L, discountId);
        } catch(NotFoundException e) {
            assertEquals("Discount found but not in correct restaurant", e.getMessage());
        }

        verify(discountRepo).existsById(discountId);
        verify(discountRepo).getById(discountId);
    }

    //-------Ingredient-related testing---------
    @Test
    void getAllRestaurantIngredients_RestaurantExistAndIngredientsExist_ReturnIngredients() {
        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));

        List<Ingredient> getIngredients  = restaurantService.getAllRestaurantIngredients(restaurantId);

        assertNotNull(getIngredients);
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

        assertNotNull(updateIngredient);
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
        
        assertNotNull(addIngredient);
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

    //--------Picture-related testing---------
    @Test
    void pictureInRestaurant_pictureExist_ReturnTrue() {
        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));

        Boolean pictureExist = restaurantService.pictureInRestaurant(restaurantId, pictureId);

        assertTrue(pictureExist);
        verify(repo).findById(restaurantId);
    }

    @Test
    void pictureInRestaurant_pictureDoNotExist_ReturnFalse() {
        Long anotherPictureId = 3L;

        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));

        Boolean pictureDoNotExist = restaurantService.pictureInRestaurant(restaurantId, anotherPictureId);

        assertFalse(pictureDoNotExist);
        verify(repo).findById(restaurantId);
    }

    @Test
    void pictureInFood_FoodPicExist_ReturnTrue() {
        when(foodRepo.findById(any(Long.class))).thenReturn(Optional.of(food));

        Boolean foodExist = restaurantService.pictureInFood(restaurantId, foodId, pictureId);
        assertTrue(foodExist);
        verify(foodRepo).findById(foodId);
    }

    @Test
    void pictureInFood_FoodPicDoNotExist_ReturnFalse() {
        Long anotherPictureId = 4L;

        when(foodRepo.findById(any(Long.class))).thenReturn(Optional.of(food));

        Boolean foodPicDoNotExist = restaurantService.pictureInFood(restaurantId, foodId, anotherPictureId);
        assertFalse(foodPicDoNotExist);
        verify(foodRepo).findById(foodId);
    }
}
