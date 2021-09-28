package foodprint.backend.controller;

import java.util.*;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import foodprint.backend.model.FoodIngredientQuantity;
import foodprint.backend.model.Ingredient;
import foodprint.backend.dto.FoodIngredientQuantityDTO;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.Discount;
import foodprint.backend.dto.RestaurantDTO;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.dto.DiscountDTO;
import foodprint.backend.model.Food;
import foodprint.backend.dto.FoodDTO;
import foodprint.backend.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;

// REST OpenAPI Swagger - http://localhost:8080/foodprint-swagger.html
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/restaurant")
public class RestaurantController {
    
    private RestaurantService service;

    @Autowired
    RestaurantController(RestaurantService service) {
        this.service = service;
    }

    // GET: Get the restaurant
    @GetMapping({"/{restaurantId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets a single restaurant from ID")
    public ResponseEntity<Restaurant> restaurantGet(@PathVariable("restaurantId") Long id) {
        Restaurant restaurant = service.get(id);
        return new ResponseEntity<>(restaurant, HttpStatus.OK);
    }

    // GET (ALL): Get all the restaurants
    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets all restaurants")
    public ResponseEntity<List<Restaurant>> restaurantGetAll() {
        List<Restaurant> restaurants = service.getAllRestaurants();
        return new ResponseEntity<>(restaurants, HttpStatus.OK);
    }

    // POST: Create new restaurant
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "Create a new restaurant using DTO")
    public ResponseEntity<Restaurant> restaurantCreate(@RequestBody @Valid RestaurantDTO restaurantDTO) {
        var convertedRestaurant = this.convertToEntity(restaurantDTO);
        Restaurant savedRestaurant = service.create(convertedRestaurant);
        return new ResponseEntity<>(savedRestaurant, HttpStatus.CREATED);
    }

    // PUT: Update the restaurant
    @PutMapping({"/{restaurantId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Updates an existing restaurant, only changed fields need to be set")
    public ResponseEntity<Restaurant> restaurantUpdate(
        @PathVariable("restaurantId") Long id,
        @RequestBody @Valid Restaurant updatedRestaurant) {
        updatedRestaurant = service.update(id, updatedRestaurant);
        return new ResponseEntity<>(updatedRestaurant, HttpStatus.OK);
    }


    // DELETE: Delete the restaurant
    @DeleteMapping({"/{restaurantId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Deletes an existing restaurant")
    public ResponseEntity<Restaurant> restaurantDelete(@PathVariable("restaurantId") Long id) {
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @GetMapping("/search")
    @Operation(summary = "Search for an existing restaurant")
	public Page<Restaurant> restaurantSearch(@RequestParam("q") String query, @RequestParam(defaultValue = "1") int pageNum) {
		Pageable page = PageRequest.of(pageNum - 1, 5); // Pagination
		Page<Restaurant> searchResult = service.search(page, query);    
        return searchResult;
    }

    /*
    *
    * Food related mappings
    *
    */

    @PostMapping({"/{restaurantId}/food"})
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "Creates a new food instance within a restaurant")
    public ResponseEntity<Food> addRestaurantFood(@PathVariable("restaurantId") Long restaurantId, @RequestBody FoodDTO food) {
        Restaurant restaurant = service.get(restaurantId);
        if(restaurant == null) {
            throw new NotFoundException("restaurant does not exist");
        }
        Food newFood = new Food(food.getFoodName(), food.getFoodPrice(), 0.00);
        newFood.setPicturesPath(new ArrayList<String>());

        List<Ingredient> ingredients = service.getRestaurantIngredients(restaurantId);

        Set<FoodIngredientQuantity> foodIngredientQuantity = new HashSet<>();
        List<FoodIngredientQuantityDTO> ingredientQuantity = food.getIngredientQuantityList();

        for (FoodIngredientQuantityDTO entry : ingredientQuantity) {
            Ingredient newIngredient = null;
            boolean found = false;
            for(Ingredient ingredient : ingredients) {
                if(ingredient.getIngredientId() == entry.getIngredientId()) {
                    newIngredient = ingredient;
                    found = true;
                }
            }

            if (!found) {
                throw new NotFoundException("restaurant does not have the ingredient");
            } else {
                FoodIngredientQuantity newFoodIngredientQuantity = new FoodIngredientQuantity(newFood, newIngredient, entry.getQuantity());
                foodIngredientQuantity.add(newFoodIngredientQuantity);
            }
        }


        Food savedFood = service.addFood(restaurantId, newFood);
        return new ResponseEntity<>(savedFood, HttpStatus.CREATED);
    }

    @GetMapping({"/{restaurantId}/food"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets all food of a restaurant")
    public ResponseEntity<List<Food>> getAllRestaurantFood(@PathVariable("restaurantId") Long restaurantId) {
        Restaurant restaurant = service.get(restaurantId);
        List<Food> allFood = restaurant.getAllFood();
        return new ResponseEntity<>(allFood, HttpStatus.OK);
    }

    @GetMapping({"/{restaurantId}/food/{foodId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Get a specific food from a restaurant")
    public ResponseEntity<Food> getRestaurantFood(@PathVariable("restaurantId") Long restaurantId, @PathVariable("foodId") Long foodId ) {
        Food food = service.getFood(restaurantId, foodId);
        return new ResponseEntity<>(food, HttpStatus.OK);
    }

    // DELETE: Delete the food
    @DeleteMapping({"/{restaurantId}/food/{foodId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Deletes an existing food item")
    public ResponseEntity<Food> deleteRestaurantFood(@PathVariable("restaurantId") Long restaurantId, @PathVariable("foodId") Long foodId) {
        service.deleteFood(restaurantId, foodId);
        try {
            service.getFood(restaurantId, foodId);
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping({"/{restaurantId}/food/{foodId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Updates an existing food item")
    public ResponseEntity<Food> updateRestaurantFood(
        @PathVariable("restaurantId") Long restaurantId,
        @PathVariable("foodId") Long foodId,
        @RequestBody Food updatedFood
    ) {
        Food food = service.updateFood(restaurantId, foodId, updatedFood);
        return new ResponseEntity<>(food, HttpStatus.OK);
    }

    @GetMapping(value = "search", produces = "application/json")
    @Operation(summary = "Search for a food item")
    public Page<Food> FoodSearch(@RequestParam("q") String query, @RequestParam(defaultValue = "1") int pageNum) {
        Pageable pages = PageRequest.of(pageNum - 1, 5); //pagination
        Page<Food> searchResult = service.searchFood(pages, query);

        return searchResult;
    }

    /*
    *
    * Discount related mappings
    *
    */

    @GetMapping({"/{restaurantId}/discount"})
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<List<Discount>> getAllRestaurantDiscount(@PathVariable("restaurantId") Long restaurantId) {
        Restaurant restaurant = service.get(restaurantId);
        List<Discount> allDiscounts = restaurant.getDiscount();
        return new ResponseEntity<>(allDiscounts, HttpStatus.OK);
    }

    //GET: Get a discount of restaurant
    @GetMapping({"/{restaurantId}/discount/{discountId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets a discount of restaurant")
    public ResponseEntity<Discount> getDiscount(@PathVariable("restaurantId") Long restaurantId, @PathVariable("discountId") Long discountId) {
        Restaurant restaurant = service.get(restaurantId);
        List<Discount> allDiscounts = restaurant.getDiscount();
        for(Discount discount : allDiscounts) {
            if (discount.getDiscountId().equals(discountId)) {
                Discount discountFound = service.getDiscount(discountId);
                return new ResponseEntity<>(discountFound, HttpStatus.OK);
            }
        }
        throw new NotFoundException("Discount not found");
    }

    //POST: Creates new discount for restaurant
    @PostMapping("/{restaurantId}/discount")
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "Creates a new discount using dto")
    public ResponseEntity<Discount> createDiscount(@PathVariable Long restaurantId, @RequestBody DiscountDTO discount) {
        // Restaurant restaurantOpt = service.get(restaurantId);
        Discount savedDiscount = service.addDiscount(restaurantId, discount);
        return new ResponseEntity<>(savedDiscount, HttpStatus.CREATED);
    }

    //DELETE: Delete a discount from restaurant
    @DeleteMapping({"/{restaurantId}/discount/{discountId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Deletes an existing discount")
    public ResponseEntity<Discount> deleteDiscount(@PathVariable("restaurantId") Long restaurantId, @PathVariable("discountId") Long discountId) {
        service.deleteDiscount(restaurantId, discountId);
        try {
            service.getDiscount(discountId);
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //PUT: Update Discount
    @PutMapping({"/{restaurantId}/discount/{discountId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Updates an existing discount")
    public ResponseEntity<Discount> updateDiscount(
        @PathVariable("restaurantId") Long restaurantId,
        @PathVariable("discountId") Long discountId,
        @RequestBody DiscountDTO updatedDiscount
    ) {
        Discount discount = new Discount(updatedDiscount.getDiscountDescription(), updatedDiscount.getDiscountPercentage());
        Discount savedDiscount = service.updateDiscount(restaurantId, discountId, discount);
        return new ResponseEntity<>(savedDiscount, HttpStatus.OK);
    }

    // @GetMapping({"/{restaurantId}/calculateIngredients"})
    // @ResponseStatus(code = HttpStatus.OK)
    // @Operation(summary = "Calculate ingredients")
    // public ResponseEntity<List>


    public Restaurant convertToEntity(RestaurantDTO restaurantDTO) {
        Restaurant restaurant = new Restaurant(restaurantDTO.getRestaurantName(), restaurantDTO.getRestaurantLocation());
        restaurant.setRestaurantDesc(restaurantDTO.getRestaurantDesc());
        restaurant.setRestaurantTableCapacity(restaurantDTO.getRestaurantTableCapacity());
        restaurant.setRestaurantWeekdayClosingHour(restaurantDTO.getRestaurantWeekdayClosingHour());
        restaurant.setRestaurantWeekdayClosingMinutes(restaurantDTO.getRestaurantWeekdayClosingMinutes());
        restaurant.setRestaurantWeekdayOpeningHour(restaurantDTO.getRestaurantWeekdayOpeningHour());
        restaurant.setRestaurantWeekdayOpeningMinutes(restaurantDTO.getRestaurantWeekdayOpeningMinutes());
        restaurant.setRestaurantWeekendClosingHour(restaurantDTO.getRestaurantWeekendClosingMinutes());
        restaurant.setRestaurantWeekendClosingMinutes(restaurantDTO.getRestaurantWeekendClosingMinutes());
        restaurant.setRestaurantWeekendOpeningHour(restaurantDTO.getRestaurantWeekendClosingHour());
        restaurant.setRestaurantWeekendOpeningMinutes(restaurantDTO.getRestaurantWeekendOpeningMinutes());
        return restaurant;
    }

    // //POST: Creates new ingredient for restaurant
    // @PostMapping("/{restaurantId}/ingredient/{ingredientId")
    // @ResponseStatus(code = HttpStatus.CREATED)
    // @Operation(summary = "Creates a new ingredient for restaurant")
    // public ResponseEntity<Discount> createRestaurantIngredeint(@PathVariable Long restaurantId, @RequestBody Ingredient ingredient) {
        
    //     return null;
    // }
}
