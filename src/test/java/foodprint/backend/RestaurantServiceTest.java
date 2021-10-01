package foodprint.backend;

import foodprint.backend.dto.DiscountDTO;
import foodprint.backend.dto.RestaurantDTO;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.model.Discount;

import foodprint.backend.model.DiscountRepo;
import foodprint.backend.model.Food;
import foodprint.backend.model.FoodRepo;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.RestaurantRepo;
import foodprint.backend.service.RestaurantService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.jayway.jsonpath.Option;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RestaurantServiceTest {
    
    @Mock
    private RestaurantRepo repo;

    @Mock
    private FoodRepo foodRepo;

    @Mock
    private DiscountRepo discountRepo;

    @InjectMocks
    private RestaurantService restaurantService;

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

        when(repo.findById(any(Long.class))).thenReturn(Optional.empty());

        try {
            restaurantService.update(restaurantId, restaurant);
        } catch (NotFoundException e) {
            assertEquals("Restaurant not found", e.getMessage());
        }

        verify(repo).findById(restaurantId);
    }

    //Does not work yet
    // @Test
    // void deleteRestaurant_RestaurantFound_Return() throws NotFoundException{
    //     Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "pictures", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10);
    //     Long restaurantId = 1L;

    //     when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));

    //     restaurantService.delete(restaurantId);
        
    //     restaurantService.get(restaurantId);
    //     verify(repo).findById(restaurantId);
    // }

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
        //Optional<Restaurant> restaurant = Optional.empty();
        Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
        Long restaurantId = 1L;

        when (repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));

        Restaurant getRestaurant = restaurantService.get(restaurantId);

        assertNotNull(getRestaurant);
        verify(repo).findById(restaurantId);
    }

    // @Test
    // void getRestaurant_RestaurantDoesNotExist_ReturnError() {
    //     Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
    //     Long restaurantId = 1L;
    // }

    // @Test
    // void addFood_newFood_ReturnSavedFood() {
    //     Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
    //     Long restaurantId = 1L;
    //     Food food = new Food("Sashimi", 50.0, 0.0);

    //     when(repo.findByRestaurantId(restaurantId)).thenReturn(restaurant);
    //     when (foodRepo.saveAndFlush(food)).thenReturn(food);

    //     Food savedFood = restaurantService.addFood(restaurantId, food);
    //     assertNotNull(savedFood);
    //     verify(repo).findByRestaurantId(restaurantId);
    //     verify(foodRepo).saveAndFlush(food);
    // }

    //Does not work yet
    // @Test
    // void updateFood_FoodExist_ReturnUpdatedFood() {
    //     Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
    //     Long restaurantId = 1L;
    //     Food food = new Food("Sashimi", "Desc", "Pictures", 50.0, 0.0);
    //     Long foodId = 1L;
    //     Food updatedFood = new Food("Sashimi", "Desc", "Pictures", 25.0, 0.0);
    //     when(repo.findById(any(Long.class))).thenReturn(Optional.of(restaurant));
    //     when(foodRepo.saveAndFlush(any(Food.class))).thenReturn(food);

    //     Food savedFood = restaurantService.updateFood(restaurantId, foodId, food);

    //     assertNotNull(savedFood);
    //     verify(repo).findById(restaurantId);
    //     verify(foodRepo).saveAndFlush(food);
    // }


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

    //does not work yet
    // @Test
    // void updateDiscount_DiscountExist_ReturnUpdatedDiscount() {
    //     Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");
    //     Long restaurantId = 1L;
    //     Long discountId = 2L;
    //     Discount discount = new Discount("1 For 1", 30);

    //     when (repo.findByRestaurantId(any(Long.class))).thenReturn(restaurant);
    //     when (discountRepo.saveAndFlush(any(Discount.class))).thenReturn(discount);
        
    //     Discount updatedDiscount = restaurantService.updateDiscount(restaurantId, discountId, discount);

    //     assertNotNull(updatedDiscount);
    //     verify(repo).findByRestaurantId(restaurantId);
    //     verify(discountRepo).saveAndFlush(any(Discount.class));
    // }
}
