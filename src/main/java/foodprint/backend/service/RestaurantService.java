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

    public void deleteFood(Long restaurantId, Long foodId) {
        Restaurant restaurant = get(restaurantId);
        List<Food> allFood = restaurant.getAllFood();
        for (Food food : allFood) {
            if (food.getFoodId().equals(foodId)) {
                foodRepo.delete(food);
                return;
            }
        }
        throw new NotFoundException("Food not found");
    }

    public Food updateFood(Long restaurantId, Long foodId, Food updatedFood) {
        Restaurant restaurant = get(restaurantId);
        List<Food> allFood = restaurant.getAllFood();
        for (Food food : allFood) {
            if (food.getFoodId().equals(foodId)) {
                food.setFoodName(updatedFood.getFoodName());
                food.setFoodDesc(updatedFood.getFoodDesc());
                food.setFoodDiscount(updatedFood.getFoodDiscount());
                food.setFoodPrice(updatedFood.getFoodPrice());
                food.setPicturesPath(updatedFood.getPicturesPath());
                food = foodRepo.saveAndFlush(food);
                return food;
            }
        }
        throw new NotFoundException("Food not found");
    }
    
    public Page<Food> searchFood(Pageable page, String query) {
        return foodRepo.findByFoodNameContains(page,query);
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

    public void deleteDiscount(Long restaurantId, Long discountId) {
        Restaurant restaurant = get(restaurantId);
        List<Discount> allDiscounts = restaurant.getAllDiscount();
        for (Discount discount : allDiscounts) {
            if (discount.getDiscountId().equals(discountId)) {
                discountRepo.delete(discount);
                return;
            }
        }
        throw new NotFoundException("Discount not found");
    }

    public Discount updateDiscount(Long restaurantId, Long discountId, Discount discount) {
        Restaurant restaurant = get(restaurantId);
        List<Discount> allDiscounts = restaurant.getAllDiscount();
        for (Discount currentDiscount : allDiscounts) {
            if (currentDiscount.getDiscountId().equals(discountId)) {
                currentDiscount.setDiscountDescription(discount.getDiscountDescription());
                currentDiscount.setDiscountPercentage(discount.getDiscountPercentage());
                currentDiscount = discountRepo.saveAndFlush(currentDiscount);
                return currentDiscount;
            }
        }
        throw new NotFoundException("Discount not found");
    }

    public Discount getDiscount(Long discountId) {
        Optional<Discount> discount = discountRepo.findById(discountId);
        return discount.orElseThrow(() -> new NotFoundException("Discount not found"));
    }
}
