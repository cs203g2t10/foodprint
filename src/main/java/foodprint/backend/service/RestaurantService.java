package foodprint.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import foodprint.backend.model.DiscountRepo;
import foodprint.backend.model.Food;
import foodprint.backend.model.FoodRepo;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.RestaurantRepo;

@Service
public class RestaurantService {

    private RestaurantRepo repo;

    private FoodRepo foodRepo;

    public RestaurantService(RestaurantRepo repo, FoodRepo foodRepo) {
        this.repo = repo;
        this.foodRepo = foodRepo;
    }
    
    public List<Restaurant> getAllRestaurants() {
        List<Restaurant> restaurants = repo.findAll();
        return restaurants;
    }

    public Optional<Restaurant> get(Long id) {
        Optional<Restaurant> restaurant = repo.findById(id);
        return restaurant;
    }

    public Restaurant update(Restaurant restaurant) {
        return repo.saveAndFlush(restaurant);
    }

    public Restaurant create(Restaurant restaurant) {
        return repo.saveAndFlush(restaurant);
    }

    public void delete(Restaurant restaurant) {
        repo.delete(restaurant);
        return;
    }

    public Page<Restaurant> search(Pageable page, String query) {
        return repo.findByRestaurantNameContains(page, query);
    }

    public Food addFood(Long restaurantId, Food food) {
        Restaurant restaurant = repo.findByRestaurantId(restaurantId);
        food.setRestaurant(restaurant);
        var savedFood = foodRepo.saveAndFlush(food);
        restaurant.getAllFood().add(savedFood);
        return savedFood;
    }

}
