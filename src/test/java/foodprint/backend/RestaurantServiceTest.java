package foodprint.backend;

import foodprint.backend.model.DiscountRepo;
import foodprint.backend.model.FoodRepo;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.RestaurantRepo;
import foodprint.backend.service.RestaurantService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    // @Test
    // void getRestaurant_ExistingRestaurant_ReturnRestaurant() {
    //     Restaurant restaurant = new Restaurant("Sushi Tei", "Serangoon");

    //     when (repo.findByRestaurantId(anyLong())).thenReturn(restaurant);

    //     Restaurant getRestaurant = restaurantService.get(restaurant.getRestaurantId());

    //     assertNotNull(getRestaurant);
        
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
}
