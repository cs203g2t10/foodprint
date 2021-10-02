package foodprint.backend;

import foodprint.backend.dto.DiscountDTO;
import foodprint.backend.dto.FoodDTO;
import foodprint.backend.dto.FoodIngredientQuantityDTO;
import foodprint.backend.dto.RestaurantDTO;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.model.Discount;

import foodprint.backend.model.DiscountRepo;
import foodprint.backend.model.Food;
import foodprint.backend.model.FoodIngredientQuantity;
import foodprint.backend.model.FoodRepo;
import foodprint.backend.model.Ingredient;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.RestaurantRepo;
import foodprint.backend.service.RestaurantService;
import javassist.tools.reflect.Reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.jayway.jsonpath.Option;

import org.aopalliance.intercept.Invocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySources;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class RestaurantServiceTest {

    RestaurantService restaurantService1 = mock(RestaurantService.class);
    
    @Mock
    private RestaurantRepo repo;

    @Mock
    private FoodRepo foodRepo;

    @Mock
    private DiscountRepo discountRepo;

    @InjectMocks
    private RestaurantService restaurantService;

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
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "pictures", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10);
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
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "pictures", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10);
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
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "pictures", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10);
        Long restaurantId = 1L;
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);

        // repo.saveAndFlush(restaurant);
        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant)).thenReturn(Optional.empty());

        restaurantService.create(restaurant);
        restaurantService.delete(restaurantId);

        //restaurantService.get(restaurantId);
        assertNotNull(restaurant);
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
    }

    //------------Food-related Testing---------------

    @Test
    void getAllFood_FoodExist_ReturnAllFood() {
        Restaurant restaurant = new Restaurant("Sushi Tei","Serangoon");
        Long restaurantId = 1L;
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);
        Food food = new Food("Sushi", 30.0, 5.0);
        List<Food> allFood = new ArrayList<>();
        allFood.add(food);

        when(repo.findByRestaurantId(any(Long.class))).thenReturn(restaurant);

        restaurant.setAllFood(allFood);
        List<Food> everything = restaurantService.getAllFood(restaurantId);

        assertNotNull(everything);
        verify(repo).findByRestaurantId(restaurantId);
    }

    @Test
    void addFood_newFood_ReturnSavedFood() {
        Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
        Long restaurantId = 1L;
        List<String> pictures = new ArrayList<>();
        pictures.add("picture");
        Food newFood = new Food("Sushi", 30.0, 10.0);
        newFood.setPicturesPath(pictures);
        FoodDTO newFoodDTO = new FoodDTO();
        List<FoodIngredientQuantityDTO> list = new ArrayList<>(); 
        newFoodDTO.setIngredientQuantityList(list);
        Ingredient ingredient = new Ingredient("Salmon");
        List<Food> allFood = new ArrayList<>();
        allFood.add(newFood);
        FoodIngredientQuantityDTO ingredients = new FoodIngredientQuantityDTO();
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);
        ReflectionTestUtils.setField(ingredient, "ingredientId", 1);
        ReflectionTestUtils.setField(ingredients, "ingredientId", 1);
        
        when(repo.findByRestaurantId(any(Long.class))).thenReturn(restaurant);
        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));
        when(foodRepo.saveAndFlush(any(Food.class))).thenReturn(newFood);
        when(repo.saveAndFlush(any(Restaurant.class))).thenReturn(restaurant);

        restaurantService.create(restaurant);
        restaurant.setAllFood(allFood);
        Food savedFood = restaurantService.addFood(restaurantId, newFoodDTO);

        assertNotNull(savedFood);
        verify(repo).findById(restaurantId);
        verify(repo).findById(restaurantId);
    }

    @Test
    void updateFood_FoodExist_ReturnUpdatedFood() {
        Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
        Food food = new Food("Sashimi", 50.0, 0.0);
        Food anotherFood2 = new Food("Sashimi", "Salmon slices","pictures", 40.0, 0.0);
        Long restaurantId = 1L;
        Long foodId = 1L;
        List<Food> allFood = new ArrayList<>();
        allFood.add(food);
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);
        ReflectionTestUtils.setField(food, "foodId", foodId);

        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));

        restaurantService.create(restaurant);
        restaurant.setAllFood(allFood);
        Food updatedFood = restaurantService.updateFood(restaurantId, foodId, anotherFood2);

        verify(repo).findById(restaurantId);
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

        when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));

        restaurantService.create(restaurant);
        restaurant.setAllFood(allFood);
        try {
            Food updatedFood = restaurantService.updateFood(restaurantId, anotherFoodId, anotherFood);
        } catch(NotFoundException e) {
            assertEquals("Food not found", e.getMessage());
        }

        verify(repo).findById(restaurantId);
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
            Food getFood = restaurantService.getFood(restaurantId, foodId);
        } catch(NotFoundException e) {
            assertEquals("Food not found", e.getMessage());
        }

        verify(foodRepo).findById(foodId);
    }


    //----------Discount-related Testing-----------
    @Test
    void addDiscount_newDiscount_ReturnSavedDiscount() {
        Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
        Long restaurantId = 1L;
        DiscountDTO discountdto = new DiscountDTO(restaurantId, "1 For 1", 50);
        Discount discount = new Discount("1 For 1", 50);

        when (repo.findByRestaurantId(any(Long.class))).thenReturn(restaurant);
        when (discountRepo.saveAndFlush(any(Discount.class))).thenReturn(discount);

        Discount savedDiscount = restaurantService.addDiscount(restaurantId, discountdto);

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
        DiscountDTO discountdto = new DiscountDTO(restaurantId, "1 for 1", 30);
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);
        ReflectionTestUtils.setField(discount, "discountId", discountId);

        when(repo.findByRestaurantId(any(Long.class))).thenReturn(restaurant);
        when(discountRepo.saveAndFlush(any(Discount.class))).thenReturn(discount);

        restaurantService.addDiscount(restaurantId, discountdto);
        Discount updatedDiscount = restaurantService.updateDiscount(restaurantId, discountId, discount);

        assertNotNull(updatedDiscount);
        verify(repo, times(2)).findByRestaurantId(restaurantId);
        verify(discountRepo).saveAndFlush(discount);
    }

    @Test
    void updateDiscount_DiscountDoNotExist_ReturnError() {
        Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
        Long discountId = 1L;
        Long restaurantId = 2L;
        Discount discount = new Discount("1 For 1", 30);

        when(repo.findByRestaurantId(any(Long.class))).thenReturn(restaurant);

        try {
            restaurantService.updateDiscount(restaurantId, discountId, discount);
        } catch (NotFoundException e) {
            assertEquals("Discount not found", e.getMessage());
        }

        verify(repo).findByRestaurantId(restaurantId);
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
        DiscountDTO discountdto = new DiscountDTO(restaurantId,discount.getDiscountDescription(), discount.getDiscountPercentage());
        Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "pictures", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10);
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);
        
        when(repo.findByRestaurantId(any(Long.class))).thenReturn(restaurant);
        when(discountRepo.existsById(any(Long.class))).thenReturn(true);
        when(discountRepo.getById(any(Long.class))).thenReturn(discount);

        restaurantService.addDiscount(restaurantId, discountdto);
        discount.setRestaurant(restaurant);
        restaurantService.deleteDiscount(restaurantId, discountId);

        assertNotNull(discount);
        verify(discountRepo).delete(discount);
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
}
