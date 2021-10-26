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
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.RestaurantRepo;
import foodprint.backend.service.PictureService;
import foodprint.backend.service.RestaurantService;

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
import java.util.ArrayList;
import java.util.Optional;

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

    //-------Restaurant-related Testing---------\

    @Test
    void getAllRestaurant_RestaurantsExist_ReturnAllRestaurant() {
        Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
        List<Restaurant> allRestaurants = new ArrayList<>();
        allRestaurants.add(restaurant);

        when(repo.findAll()).thenReturn(allRestaurants);

        List<Restaurant> restaurants = restaurantService.getAllRestaurants();

        assertNotNull(restaurants);
        verify(repo).findAll();
    }

    @Test
    void addRestaurant_NewRestaurant_ReturnSavedRestaurant() {
        Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");

        when(repo.saveAndFlush(any(Restaurant.class))).thenReturn(restaurant);

        Restaurant createdRestaurant = restaurantService.create(restaurant);

        assertNotNull(createdRestaurant);
        verify(repo).saveAndFlush(restaurant);
    }

    @Test
    void updateRestaurant_RestaurantFound_ReturnUpdatedRestaurant() {
        //Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Casual dining");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        Long restaurantId = 1L;

        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));
        when(repo.saveAndFlush(any(Restaurant.class))).thenReturn(restaurant);

        //Restaurant createdRestaurant = restaurantService.create(restaurant);
        Restaurant updatedRestaurant = restaurantService.update(restaurantId, restaurant);

        assertNotNull(updatedRestaurant);
        verify(repo).findById(restaurantId);
        verify(repo).saveAndFlush(restaurant);
    }

    @Test
    void updateRestaurant_RestaurantNotFound_ReturnError() {
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        Long restaurantId = 1L;

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
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Casual dining");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        Long restaurantId = 1L;
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);

        doNothing().when(repo).delete(any(Restaurant.class));
        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant)).thenReturn(Optional.empty());

        restaurantService.create(restaurant);
        restaurantService.delete(restaurantId);

        //restaurantService.get(restaurantId);
        assertNotNull(restaurant);
        verify(repo, times(2)).findById(restaurantId);
        verify(repo).delete(restaurant);
    }

    @Test
    void deleteRestaurant_RestaurantNotFound_ReturnError() {
        //Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "pictures", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10);
        Long restaurantId = 1L;
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
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Casual dining");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        Long restaurantId = 1L;
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
        Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
        Long restaurantId = 1L;

        when (repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));

        Restaurant getRestaurant = restaurantService.get(restaurantId);

        assertNotNull(getRestaurant);
        verify(repo).findById(restaurantId);
    }

    @Test
    void getRestaurant_RestaurantDoesNotExist_ReturnError() {
        //Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
        Long restaurantId = 1L;

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
        List<Restaurant> allRestaurants = new ArrayList<>();
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Casual dining");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        allRestaurants.add(restaurant);

        when(repo.findAll()).thenReturn(allRestaurants);

        List<String> getCategories = restaurantService.getCategories();
        assertNotNull(getCategories);
        verify(repo).findAll();
    }

    @Test
    void getRestaurants_RestaurantsRelatedToCategoriesFound_ReturnRestaurantsRelated() {
        List<Restaurant> allRestaurants = new ArrayList<>();
        List<String> restaurantCategories = new ArrayList<>();
        String category = "Japanese";
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Casual dining");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        allRestaurants.add(restaurant);

        when(repo.findAll()).thenReturn(allRestaurants);
        
        List<Restaurant> getRestaurantsRelatedToCategory = restaurantService.getRestaurantsRelatedToCategory(category);
        assertNotNull(getRestaurantsRelatedToCategory);
        verify(repo).findAll();
    }

    // @Test
    // void search_RestaurantsFound_ReturnSearchResults() {
    //     Restaurant restaurant = new Restaurant("Sushi Tei", "Desc","Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10);
    //     repo.saveAndFlush(restaurant);
    //     List<String> sort = new ArrayList<>();
    //     sort.add(restaurant.getRestaurantName());
    //     Pageable page = new Pageable(1, 1, sort);
    //     String query = "Sushi";
    //     Page<Restaurant> result = new Page

    //     when(repo.findByRestaurantNameContains(any(Pageable.class), any(String.class))).thenReturn()
    // }

    //------------Food-related Testing---------------

    @Test
    void addFood_newFood_ReturnSavedFood() {
        Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
        Long restaurantId = 1L;
        List<String> pictures = new ArrayList<>();
        pictures.add("picture");
        Food newFood = new Food("Sushi", 30.0, 10.0);
        FoodDTO newFoodDTO = new FoodDTO();
        List<FoodIngredientQuantityDTO> list = new ArrayList<>(); 
        newFoodDTO.setIngredientQuantityList(list);
        Ingredient ingredient = new Ingredient("Salmon");
        List<Food> allFood = new ArrayList<>();
        allFood.add(newFood);
        FoodIngredientQuantityDTO ingredients = new FoodIngredientQuantityDTO();
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);
        ReflectionTestUtils.setField(ingredient, "ingredientId", 1L);
        ReflectionTestUtils.setField(ingredients, "ingredientId", 1L);
        
        when(repo.findByRestaurantId(any(Long.class))).thenReturn(restaurant);
        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));
        when(foodRepo.saveAndFlush(any(Food.class))).thenReturn(newFood);

        restaurantService.create(restaurant);
        restaurant.setAllFood(allFood);
        Food savedFood = restaurantService.addFood(restaurantId, newFoodDTO);

        assertNotNull(savedFood);
        verify(repo).findById(restaurantId);
        verify(repo).findByRestaurantId(restaurantId);
        verify(foodRepo).saveAndFlush(any(Food.class));
    }

    @Test
    void updateFood_FoodExist_ReturnUpdatedFood() {
        Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
        Food food = new Food("Sashimi", 50.0, 0.0);
        //Food anotherFood2 = new Food("Sashimi", "Salmon slices","pictures", 40.0, 0.0);
        Long restaurantId = 1L;
        Long foodId = 1L;
        List<Food> allFood = new ArrayList<>();
        allFood.add(food);
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);
        ReflectionTestUtils.setField(food, "foodId", foodId);

        when(foodRepo.findById(any(Long.class))).thenReturn(Optional.of(food));
        when(foodRepo.saveAndFlush(any(Food.class))).thenReturn(food);

        restaurantService.create(restaurant);
        restaurant.setAllFood(allFood);
        food.setRestaurant(restaurant);
        restaurantService.updateFood(restaurantId, foodId, food);

        verify(foodRepo).findById(foodId);
        verify(foodRepo).saveAndFlush(food);
    }

    @Test
    void updateFood_FoodDoNotExist_Return() {
        Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
        Food food = new Food("Sashimi", 50.0, 0.0);
        Food anotherFood = new Food("Sushi", 20.0, 10.0);
        Long restaurantId = 1L;
        Long foodId = 1L;
        Long anotherFoodId = 2L;
        List<Food> allFood = new ArrayList<>();
        allFood.add(food);
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);
        ReflectionTestUtils.setField(food, "foodId", foodId);

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
        Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
        Food food = new Food("Sashimi", 50.0, 0.0);
        Long restaurantId = 1L;
        Long anotherRestaurantId = 2L;
        Long foodId = 1L;
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);
        ReflectionTestUtils.setField(food, "foodId", foodId);

        when(foodRepo.findById(any(Long.class))).thenReturn(Optional.of(food));

        restaurantService.create(restaurant);
        food.setRestaurant(restaurant);
        try {
            restaurantService.updateFood(anotherRestaurantId, foodId, food);
        } catch (NotFoundException e) {
            assertEquals("Food requested could not be found at this restaurant", e.getMessage());
        }

        verify(foodRepo).findById(foodId);
    }

    @Test
    void deleteFood_FoodExist_Return() {
        Food food = new Food("Sashimi", 50.0, 0.0);
        Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
        Long restaurantId = 1L;
        Long foodId = 1L;
        List<Food> allFood = new ArrayList<>();
        allFood.add(food);
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);
        ReflectionTestUtils.setField(food, "foodId", foodId);

        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));

        restaurantService.create(restaurant);
        restaurant.setAllFood(allFood);
        restaurantService.deleteFood(restaurantId, foodId);

        verify(repo).findById(restaurantId);
    }

    @Test
    void deleteFood_FoodDoNotExist_ReturnError() {
        Food food = new Food("Sashimi", 50.0, 0.0);
        Long foodId = 1L;
        Long anotherFoodId = 2L;
        Long restaurantId = 1L;
        Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
        List<Food> allFood = new ArrayList<>();
        allFood.add(food);
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);
        ReflectionTestUtils.setField(food, "foodId", foodId);

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
        Food food = new Food("Sashimi", 50.0, 0.0);
        Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
        Long foodId = 1L;
        Long restaurantId = 1L;
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);
        
        when(foodRepo.findById(any(Long.class))).thenReturn(Optional.of(food));

        restaurantService.create(restaurant);
        food.setRestaurant(restaurant);
        Food getFood = restaurantService.getFood(restaurantId, foodId);

        assertNotNull(getFood);
        verify(foodRepo).findById(foodId);
    }

    @Test
    void getFood_FoodDoNotExist_ReturnError() {
        Long restaurantId = 1L;
        Long foodId = 1L;
        Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);

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
        Food food = new Food("Sashimi", 50.0, 0.0);
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Casual dining");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc","Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        Long foodId = 1L;
        Long restaurantId = 1L;
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);
        ReflectionTestUtils.setField(food, "foodId", foodId);

        when(foodRepo.findById(any(Long.class))).thenReturn(Optional.of(food));

        food.setRestaurant(restaurant);
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
        Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
        Long restaurantId = 1L;
        Discount discount = new Discount("1 For 1", 50);

        when (repo.findByRestaurantId(any(Long.class))).thenReturn(restaurant);
        when (discountRepo.saveAndFlush(any(Discount.class))).thenReturn(discount);

        Discount savedDiscount = restaurantService.addDiscount(restaurantId, discount);

        assertNotNull(savedDiscount);
        verify(repo).findByRestaurantId(restaurantId);
        verify(discountRepo).saveAndFlush(any(Discount.class));
    }

    @Test
    void updateDiscount_DiscountExist_ReturnUpdatedDiscount() {
        Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
        Long restaurantId = 1L;
        Long discountId = 2L;
        Discount discount = new Discount("1 For 1", 30);
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);
        ReflectionTestUtils.setField(discount, "discountId", discountId);

        when(repo.findByRestaurantId(any(Long.class))).thenReturn(restaurant);
        when(discountRepo.findById(any(Long.class))).thenReturn(Optional.of(discount));
        when(discountRepo.saveAndFlush(any(Discount.class))).thenReturn(discount);

        restaurantService.addDiscount(restaurantId, discount);
        discount.setRestaurant(restaurant);
        Discount updatedDiscount = restaurantService.updateDiscount(restaurantId, discountId, discount);

        assertNotNull(updatedDiscount);
        verify(discountRepo).findById(discountId);
        verify(discountRepo).saveAndFlush(discount);
    }

    @Test
    void updateDiscount_DiscountDoNotExist_ReturnError() {
        //Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
        Long discountId = 1L;
        Long restaurantId = 2L;
        Discount discount = new Discount("1 For 1", 30);

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
        Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
        Long discountId = 1L;
        Long restaurantId = 2L;
        Discount discount = new Discount("1 For 1", 30);

        when(repo.findByRestaurantId(any(Long.class))).thenReturn(restaurant);
        when(discountRepo.findById(any(Long.class))).thenReturn(Optional.of(discount));

        restaurantService.addDiscount(restaurantId, discount);
        discount.setRestaurant(restaurant);

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
        Discount discount = new Discount("Buy 1 get 1 free", 50);
        Long discountId = 1L;

        when(discountRepo.findById(any(Long.class))).thenReturn(Optional.of(discount));

        Discount getDiscount = restaurantService.getDiscount(discountId);

        assertNotNull(getDiscount);
        verify(discountRepo).findById(discountId);
    }

    @Test
    void getDiscount_DiscountDoNotExist_ReturnError() {
        //Discount discount = new Discount("Buy 1 get 1 free", 50);
        Long discountId = 1L;

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
        Discount discount = new Discount("1 For 1", 50);
        Long discountId = 1L;
        Long restaurantId = 1L;
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Casual dining");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc","Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);
        
        when(repo.findByRestaurantId(any(Long.class))).thenReturn(restaurant);
        when(discountRepo.existsById(any(Long.class))).thenReturn(true);
        when(discountRepo.getById(any(Long.class))).thenReturn(discount);

        restaurantService.addDiscount(restaurantId, discount);
        discount.setRestaurant(restaurant);
        restaurantService.deleteDiscount(restaurantId, discountId);

        assertNotNull(discount);
        verify(discountRepo).getById(discountId);
        verify(discountRepo).existsById(discountId);
        verify(repo).findByRestaurantId(restaurantId);
    }

    @Test
    void deleteDiscount_DiscountDoNotExist_ReturnError() {
        Long discountId = 1L;
        Long restaurantId = 1L;

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
        Discount discount = new Discount("1 For 1", 50);
        Long discountId = 1L;
        Long restaurantId = 1L;
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Casual dining");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc","Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        Restaurant anotherRestaurant = new Restaurant("Sushi Tei", "Desc","Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);
        ReflectionTestUtils.setField(anotherRestaurant,"restaurantId", 2L);
        ReflectionTestUtils.setField(discount, "discountId", discountId);

        when(discountRepo.existsById(any(Long.class))).thenReturn(true);
        when(discountRepo.getById(any(Long.class))).thenReturn(discount);

        discount.setRestaurant(restaurant);
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
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Casual dining");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        Long restaurantId = 1L;
        List<Ingredient> ingredients = new ArrayList<>();
        Ingredient ingredient = new Ingredient("Salmon");
        ingredients.add(ingredient);

        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));

        restaurant.setIngredients(ingredients);
        List<Ingredient> getIngredients  = restaurantService.getAllRestaurantIngredients(restaurantId);

        assertNotNull(getIngredients);
        verify(repo).findById(restaurantId);
    }

    @Test
    void getAllRestaurantIngredients_RestaurantDoNotExist_ReturnError() {
        Long restaurantId = 1L;

        when(repo.findById(any(Long.class))).thenReturn(Optional.empty());

        try {
            restaurantService.getAllRestaurantIngredients(restaurantId);
        } catch(NotFoundException e) {
            assertEquals("Restaurant not found", e.getMessage());
        }

        verify(repo).findById(restaurantId);
    }

    @Test
    void addRestaurantIngredient_RestaurantExist_ReturnIngredient() {
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Casual dining");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        Long restaurantId = 1L;
        List<Ingredient> ingredients = new ArrayList<>();
        Ingredient ingredient = new Ingredient("Salmon");
        restaurant.setIngredients(ingredients);

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
        Long restaurantId = 1L;
        Ingredient ingredient = new Ingredient("Salmon");

        when(repo.findById(any(Long.class))).thenReturn(Optional.empty());

        try {
            restaurantService.addRestaurantIngredient(restaurantId, ingredient);
        } catch(NotFoundException e) {
            assertEquals("Restaurant not found", e.getMessage());
        }

        verify(repo).findById(restaurantId);
    }

    //--------Picture-related testing---------
    @Test
    void pictureInRestaurant_pictureExist_ReturnTrue() {
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Casual dining");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        Long restaurantId = 1L;
        Picture pic = new Picture("image", "desc", "Path", "file", "www.file.com");
        Long pictureId = 2L;
        ReflectionTestUtils.setField(pic, "pictureId", pictureId);
        List<Picture> picList = new ArrayList<>();
        picList.add(pic);
        restaurant.setPictures(picList);

        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));

        Boolean pictureExist = restaurantService.pictureInRestaurant(restaurantId, pictureId);

        assertTrue(pictureExist);
        verify(repo).findById(restaurantId);
    }

    @Test
    void pictureInRestaurant_pictureDoNotExist_ReturnFalse() {
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        Long restaurantId = 1L;
        Picture pic = new Picture("image", "desc", "Path", "file", "www.file.com");
        Long pictureId = 2L;
        ReflectionTestUtils.setField(pic, "pictureId", pictureId);
        List<Picture> picList = new ArrayList<>();
        picList.add(pic);
        restaurant.setPictures(picList);
        Long anotherPictureId = 3L;

        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));

        Boolean pictureDoNotExist = restaurantService.pictureInRestaurant(restaurantId, anotherPictureId);

        assertFalse(pictureDoNotExist);
        verify(repo).findById(restaurantId);
    }

    @Test
    void pictureInFood_FoodPicExist_ReturnTrue() {
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Casual dining");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        Long restaurantId = 1L;
        Food food = new Food("Sashimi", 50.0, 0.0);
        Long foodId = 2L;
        food.setRestaurant(restaurant);
        Long pictureId = 3L;
        Picture pic = new Picture("image", "desc", "Path", "file", "www.file.com");
        List<Picture> picList = new ArrayList<>();
        picList.add(pic);
        food.setPictures(picList);
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);
        ReflectionTestUtils.setField(pic, "pictureId", pictureId);

        when(foodRepo.findById(any(Long.class))).thenReturn(Optional.of(food));

        Boolean foodExist = restaurantService.pictureInFood(restaurantId, foodId, pictureId);
        assertTrue(foodExist);
        verify(foodRepo).findById(foodId);
    }

    @Test
    void pictureInFood_FoodPicDoNotExist_ReturnFalse() {
        List<String> restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Casual dining");
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        Long restaurantId = 1L;
        Food food = new Food("Sashimi", 50.0, 0.0);
        Long foodId = 2L;
        food.setRestaurant(restaurant);
        Long pictureId = 3L;
        Picture pic = new Picture("image", "desc", "Path", "file", "www.file.com");
        List<Picture> picList = new ArrayList<>();
        picList.add(pic);
        food.setPictures(picList);
        Long anotherPictureId = 4L;
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);
        ReflectionTestUtils.setField(pic, "pictureId", pictureId);

        when(foodRepo.findById(any(Long.class))).thenReturn(Optional.of(food));

        Boolean foodPicDoNotExist = restaurantService.pictureInFood(restaurantId, foodId, anotherPictureId);
        assertFalse(foodPicDoNotExist);
        verify(foodRepo).findById(foodId);
    }

    //Integration testing
    // @Test
    // void getFoodPictureById_FoodPictureFoundInRestaurant_ReturnFoodPicture() {
    //     Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10);
    //     Long restaurantId = 1L;
    //     Food food = new Food("Sashimi", 50.0, 0.0);
    //     Long foodId = 2L;
    //     Long pictureId = 3L;
    //     Picture pic = new Picture("image", "desc", "Path", "file", "www.file.com");
    //     List<Picture> picList = new ArrayList<>();
    //     picList.add(pic);
    //     food.setPictures(picList);
    //     food.setRestaurant(restaurant);
    //     FileStore fileStore = new FileStore(amazonS3);
    //     ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);
    //     ReflectionTestUtils.setField(pic, "pictureId", pictureId);
    //     ReflectionTestUtils.setField(food, "foodId", foodId);

    //     when(foodRepo.findById(any(Long.class))).thenReturn(Optional.of(food));
    //     when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.of(pic));

    //     PictureService pictureService = new PictureService(fileStore, pictureRepo);
    //     String foodPic = restaurantService.getFoodPictureById(restaurantId, foodId, pictureId);
    //     assertNotNull(foodPic);
    //     verify(foodRepo).findById(foodId);
    // }
}
