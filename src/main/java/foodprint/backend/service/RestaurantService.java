package foodprint.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import foodprint.backend.model.Discount;
import foodprint.backend.model.DiscountRepo;
import foodprint.backend.dto.DiscountDTO;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.model.Food;
import foodprint.backend.model.FoodRepo;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.RestaurantRepo;

@Service
public class RestaurantService {

    private RestaurantRepo repo;

    private FoodRepo foodRepo;

    private DiscountRepo discountRepo;

    public RestaurantService(RestaurantRepo repo, FoodRepo foodRepo, DiscountRepo discountRepo) {
        this.repo = repo;
        this.foodRepo = foodRepo;
        this.discountRepo = discountRepo;
    }
    
    public List<Restaurant> getAllRestaurants() {
        List<Restaurant> restaurants = repo.findAll();
        return restaurants;
    }

    public Restaurant get(Long id) {
        Optional<Restaurant> restaurant = repo.findById(id);
        return restaurant.orElseThrow(() -> new NotFoundException("Restaurant not found"));
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

    /*
    *
    * Food related methods
    *
    */

    public Food addFood(Long restaurantId, Food food) {
        Restaurant restaurant = repo.findByRestaurantId(restaurantId);
        food.setRestaurant(restaurant);
        var savedFood = foodRepo.saveAndFlush(food);
        restaurant.getAllFood().add(savedFood);
        return savedFood;
    }

    public List<Food> getAllFood(Long restaurantId) {
        Restaurant restaurant = repo.findByRestaurantId(restaurantId);
        return restaurant.getAllFood();
    }

    public Food getFood(Long restaurantId, Long foodId) {
        Optional<Food> foodOpt = foodRepo.findById(foodId);
        Food food = foodOpt.orElseThrow(() -> new NotFoundException("Food not found"));
        if (food.getRestaurant().getRestaurantId() != restaurantId) {
            throw new NotFoundException("Food found but in incorrect restaurant");
        }
        return food;
    }

    /*
    *
    * Discount related methods
    *
    */

    // public List<Discount> getDiscounts(Long restaurantId) {
    //     Optional<Discount> discount = discountRepo.findById(discountId);
    //     return discount;
    // }

    public Discount addDiscount(Long restaurantId, DiscountDTO discount) {
        Restaurant restaurant = repo.findByRestaurantId(restaurantId);
        Discount Discount = new Discount();
        Discount.restaurant(restaurant)
                     .discountDescription(discount.getDiscountDescription())
                     .discountPercentage(discount.getDiscountPercentage());
        var savedDiscount = discountRepo.saveAndFlush(Discount);
        restaurant.getAllDiscount().add(savedDiscount);
        return savedDiscount;
    }

    public void deleteDiscount(Discount discount) {
        discountRepo.delete(discount);
    }

    public Discount updateDiscount(Discount currentDiscount, DiscountDTO updatedDiscount) {
        currentDiscount.setDiscountDescription(updatedDiscount.getDiscountDescription());
        currentDiscount.setDiscountPercentage(updatedDiscount.getDiscountPercentage());
        currentDiscount = discountRepo.saveAndFlush(currentDiscount);
        return currentDiscount;
    }

    public Discount getDiscount(Long discountId) {
        Optional<Discount> discount = discountRepo.findById(discountId);
        return discount.orElseThrow(() -> new NotFoundException("Discount not found"));
    }
}
