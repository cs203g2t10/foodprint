package foodprint.backend.service;

import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import foodprint.backend.model.Discount;
import foodprint.backend.model.DiscountRepo;
import foodprint.backend.dto.DiscountDTO;
import foodprint.backend.dto.FoodDTO;
import foodprint.backend.dto.FoodIngredientQuantityDTO;
import foodprint.backend.exceptions.DeleteFailedException;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.model.Food;
import foodprint.backend.model.FoodRepo;
import foodprint.backend.model.Ingredient;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.RestaurantRepo;
import foodprint.backend.model.IngredientRepo;
import foodprint.backend.model.FoodIngredientQuantity;

@Service
public class RestaurantService {

    private RestaurantRepo repo;

    private FoodRepo foodRepo;

    private DiscountRepo discountRepo;

    private IngredientRepo ingredientRepo;


    public RestaurantService(RestaurantRepo repo, FoodRepo foodRepo, DiscountRepo discountRepo, IngredientRepo ingredientRepo) {
        this.repo = repo;
        this.foodRepo = foodRepo;
        this.discountRepo = discountRepo;
        this.ingredientRepo = ingredientRepo;
    }
    
    public List<Restaurant> getAllRestaurants() {
        List<Restaurant> restaurants = repo.findAll();
        return restaurants;
    }

    public Restaurant get(Long id) {
        Optional<Restaurant> restaurant = repo.findById(id);
        return restaurant.orElseThrow(() -> new NotFoundException("Restaurant not found"));
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public Restaurant update(Long id, Restaurant updatedRestaurant) {

        Restaurant currentRestaurant = this.get(id);

        if (updatedRestaurant.getRestaurantDesc() != null) {
            currentRestaurant.setRestaurantDesc(updatedRestaurant.getRestaurantDesc());
        }
        if (updatedRestaurant.getRestaurantName() != null) {
            currentRestaurant.setRestaurantName(updatedRestaurant.getRestaurantName());
        }
        if (updatedRestaurant.getRestaurantName() != null) {
            currentRestaurant.setRestaurantName(updatedRestaurant.getRestaurantName());
        }
        if (updatedRestaurant.getPicturesPath() != null) {
            currentRestaurant.setRestaurantLocation(updatedRestaurant.getRestaurantLocation());
        }
        if (updatedRestaurant.getPicturesPath() != null) {
            currentRestaurant.setPicturesPath(updatedRestaurant.getPicturesPath());
        }
        if (updatedRestaurant.getRestaurantTableCapacity() != null) {
            currentRestaurant.setRestaurantTableCapacity(updatedRestaurant.getRestaurantTableCapacity());
        }
        if (updatedRestaurant.getRestaurantWeekdayClosingHour() != null) {
            currentRestaurant.setRestaurantWeekdayClosingHour(updatedRestaurant.getRestaurantWeekdayClosingHour());
        }
        if (updatedRestaurant.getRestaurantWeekdayOpeningHour() != null) {
            currentRestaurant.setRestaurantWeekdayOpeningHour(updatedRestaurant.getRestaurantWeekdayOpeningHour());
        }
        if (updatedRestaurant.getRestaurantWeekendClosingHour() != null) {
            currentRestaurant.setRestaurantWeekendClosingHour(updatedRestaurant.getRestaurantWeekendClosingHour());
        }
        if (updatedRestaurant.getRestaurantWeekendOpeningHour() != null) {
            currentRestaurant.setRestaurantWeekendOpeningHour(updatedRestaurant.getRestaurantWeekendOpeningHour());
        }
        if (updatedRestaurant.getRestaurantWeekdayClosingMinutes() != null) {
            currentRestaurant.setRestaurantWeekdayClosingMinutes(updatedRestaurant.getRestaurantWeekdayClosingMinutes());
        }
        if (updatedRestaurant.getRestaurantWeekdayOpeningMinutes() != null) {
            currentRestaurant.setRestaurantWeekdayOpeningMinutes(updatedRestaurant.getRestaurantWeekdayOpeningMinutes());
        }
        if (updatedRestaurant.getRestaurantWeekendClosingMinutes() != null) {
            currentRestaurant.setRestaurantWeekendClosingMinutes(updatedRestaurant.getRestaurantWeekendClosingMinutes());
        }
        if (updatedRestaurant.getRestaurantWeekendOpeningMinutes() != null) {
            currentRestaurant.setRestaurantWeekendOpeningMinutes(updatedRestaurant.getRestaurantWeekendOpeningMinutes());
        }

        return repo.saveAndFlush(currentRestaurant);
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public Restaurant create(Restaurant restaurant) {
        return repo.saveAndFlush(restaurant);
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public void delete(Long id) {
        Restaurant restaurant = this.get(id);
        repo.delete(restaurant);
        try { 
            this.get(id);
            throw new DeleteFailedException("Restaurant could not be deleted");
        } catch (NotFoundException ex) {
            return;
        }
    }

    public Page<Restaurant> search(Pageable page, String query) {
        return repo.findByRestaurantNameContains(page, query);
    }

    /*
    *
    * Food related methods
    *
    */

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public Food addFood(Long restaurantId, FoodDTO foodDTO) {
        Restaurant restaurant = repo.findByRestaurantId(restaurantId);

        Food newFood = new Food(foodDTO.getFoodName(), foodDTO.getFoodPrice(), 0.00);
        newFood.setPicturesPath(new ArrayList<String>());

        List<Ingredient> ingredients = getAllRestaurantIngredients(restaurantId);
        Set<FoodIngredientQuantity> foodIngredientQuantity = new HashSet<>();
        List<FoodIngredientQuantityDTO> ingredientQuantity = foodDTO.getIngredientQuantityList();

        for (FoodIngredientQuantityDTO entry : ingredientQuantity) {
            Ingredient newIngredient = null;
            for(Ingredient ingredient : ingredients) {
                if(ingredient.getIngredientId() == entry.getIngredientId()) {
                    newIngredient = ingredient;
                }
            }

            if (newIngredient == null) {
                throw new NotFoundException("restaurant does not have the ingredient");
            } 

            FoodIngredientQuantity newFoodIngredientQuantity = new FoodIngredientQuantity(newFood, newIngredient, entry.getQuantity());
            foodIngredientQuantity.add(newFoodIngredientQuantity);
        }

        newFood.setFoodIngredientQuantity(foodIngredientQuantity);

        newFood.setRestaurant(restaurant);
        var savedFood = foodRepo.saveAndFlush(newFood);
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

    @PreAuthorize("hasAnyAuthority('FP_USER')")
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

    @PreAuthorize("hasAnyAuthority('FP_USER')")
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
    
    @PreAuthorize("hasAnyAuthority('FP_USER')")
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

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public Discount addDiscount(Long restaurantId, DiscountDTO discount) {
        Restaurant restaurant = repo.findByRestaurantId(restaurantId);
        Discount Discount = new Discount();
        Discount.restaurant(restaurant)
                     .discountDescription(discount.getDiscountDescription())
                     .discountPercentage(discount.getDiscountPercentage());
        var savedDiscount = discountRepo.saveAndFlush(Discount);
        restaurant.getDiscount().add(savedDiscount);
        return savedDiscount;
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public void deleteDiscount(Long restaurantId, Long discountId) {
        if (discountRepo.existsById(discountId)) {
            Discount discount = discountRepo.getById(discountId);
            if (discount.getRestaurant().getRestaurantId().equals(restaurantId)) {
                discountRepo.delete(discount);
                return;
            } else {
                throw new NotFoundException("Discount found but not in correct restaurant");
            }
        } else {
            throw new NotFoundException("Discount not found");
        }
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public Discount updateDiscount(Long restaurantId, Long discountId, Discount discount) {
        Restaurant restaurant = repo.findByRestaurantId(restaurantId);
        List<Discount> allDiscounts = restaurant.getDiscount();
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

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public Discount getDiscount(Long discountId) {
        Optional<Discount> discount = discountRepo.findById(discountId);
        return discount.orElseThrow(() -> new NotFoundException("Discount not found"));
    }

    /*
    *
    * Ingredient related methods
    *
    */
    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public List<Ingredient> getAllRestaurantIngredients(Long restaurantId) {
        Restaurant restaurant = get(restaurantId);
        if (restaurant == null) {
            throw new NotFoundException("Restaurant does not exist");
        }
        return restaurant.getIngredients();
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public Ingredient addRestaurantIngredient(Long restaurantId, Ingredient newIngredient) {
        Restaurant restaurant = get(restaurantId);
        if (restaurant == null) {
            throw new NotFoundException("Restaurant does not exist");
        }
        List<Ingredient> ingredients = restaurant.getIngredients();
        ingredients.add(newIngredient);
        restaurant.setIngredients(ingredients);
        newIngredient.setRestaurant(restaurant);
        ingredientRepo.saveAndFlush(newIngredient);
        repo.saveAndFlush(restaurant);
        return newIngredient;
    }
  


    // @PreAuthorize("hasAnyAuthority('FP_USER')")
    // public HashMap<Ingredient, Integer> calculateIngredientsNeeded(Long restaurantId) {
    //     HashMap<Ingredient, Integer> map = new HashMap<>();
    //     Restaurant restaurant = repo.findByRestaurantId(restaurantId);
    //     if (restaurant == null) {
    //         throw new NotFoundException("Restaurant does not exist");
    //     }

    //     List<Reservation> reservations = reservationRepo.findByRestaurant(restaurant);
    //     Iterator<Reservation> reservationItr = reservations.iterator();
    //     while (reservationItr.hasNext()) {
    //         Reservation reservation = reservationItr.next();
    //         List<LineItem> lineItems = reservation.getLineItems();
            
    //         Iterator<LineItem> lineItr = lineItems.iterator();
    //         while (lineItr.hasNext()) {
    //             LineItem lineItem = lineItr.next();
    //             Food food = lineItem.getFood();
    //             if (map.containsKey(food)) {
    //                 food.getIngredients();
    //             } 
    //         }
    //     }

    //     return null;
    // }
}
