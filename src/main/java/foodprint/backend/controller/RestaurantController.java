package foodprint.backend.controller;

import java.util.List;

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

import foodprint.backend.model.Restaurant;
import foodprint.backend.model.Discount;
import foodprint.backend.dto.RestaurantDTO;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.dto.DiscountDTO;
import foodprint.backend.model.Food;
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
    public ResponseEntity<Restaurant> restaurantCreate(@RequestBody RestaurantDTO restaurantDTO) {
        Restaurant restaurant = new Restaurant(restaurantDTO.getRestaurantName(), restaurantDTO.getRestaurantLocation());
        Restaurant savedRestaurant = service.create(restaurant);
        return new ResponseEntity<>(savedRestaurant, HttpStatus.CREATED);
    }

    // PUT: Update the restaurant
    @PutMapping({"/{restaurantId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Updates an existing restaurant")
    public ResponseEntity<Restaurant> restaurantUpdate(
        @PathVariable("restaurantId") Long id,
        @RequestBody Restaurant updatedRestaurant
    ) {

        Restaurant currentRestaurant = service.get(id);
        currentRestaurant.setRestaurantDesc(updatedRestaurant.getRestaurantDesc());
        currentRestaurant.setRestaurantName(updatedRestaurant.getRestaurantName());
        currentRestaurant.setRestaurantLocation(updatedRestaurant.getRestaurantLocation());
        currentRestaurant.setPicturesPath(updatedRestaurant.getPicturesPath());
        currentRestaurant.setRestaurantTableCapacity(updatedRestaurant.getRestaurantTableCapacity());
        currentRestaurant.setRestaurantWeekdayClosing(updatedRestaurant.getRestaurantWeekdayClosing());
        currentRestaurant.setRestaurantWeekdayOpening(updatedRestaurant.getRestaurantWeekdayOpening());
        currentRestaurant.setRestaurantWeekendClosing(updatedRestaurant.getRestaurantWeekendClosing());
        currentRestaurant.setRestaurantWeekendOpening(updatedRestaurant.getRestaurantWeekendOpening());
        currentRestaurant = service.update(currentRestaurant);
        return new ResponseEntity<>(currentRestaurant, HttpStatus.OK);

    }


    // DELETE: Delete the restaurant
    @DeleteMapping({"/{restaurantId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Deletes an existing restaurant")
    public ResponseEntity<Restaurant> restaurantDelete(@PathVariable("restaurantId") Long id) {
        Restaurant restaurant = service.get(id);
        service.delete(restaurant);
        
        try { 
            service.get(id);
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
    public ResponseEntity<Food> addRestaurantFood(@PathVariable("restaurantId") Long restaurantId, @RequestBody Food food) {
        Food savedFood = service.addFood(restaurantId, food);
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

    /*
    *
    * Discount related mappings
    *
    */

    @GetMapping({"/{restaurantId}/discount"})
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<List<Discount>> getAllRestaurantDiscount(@PathVariable("restaurantId") Long restaurantId) {
        Restaurant restaurant = service.get(restaurantId);
        List<Discount> allDiscounts = restaurant.getAllDiscount();
        return new ResponseEntity<>(allDiscounts, HttpStatus.OK);
    }

    //GET: Get a discount of restaurant
    @GetMapping({"/{restaurantId}/discount/{discountId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets a discount of restaurant")
    public ResponseEntity<Discount> getDiscount(@PathVariable("restaurantId") Long restaurantId, @PathVariable("discountId") Long discountId) {
        Restaurant restaurant = service.get(restaurantId);
        List<Discount> allDiscounts = restaurant.getAllDiscount();
        for(Discount discount : allDiscounts) {
            if (discount.getDiscountId().equals(discountId)) {
                Discount discountFound = service.getDiscount(discountId);
                return new ResponseEntity<>(discountFound, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
        Restaurant restaurant = service.get(restaurantId);
        List<Discount> allDiscounts = restaurant.getAllDiscount();
        for (Discount discount : allDiscounts) {
            if(discount.getDiscountId().equals(discountId)) {
                service.deleteDiscount(discount);
                return new ResponseEntity<>(HttpStatus.OK);
            }
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
        List<Discount> allDiscounts = service.get(restaurantId).getAllDiscount();
        Discount changedDiscount = new Discount();
        for(Discount discount : allDiscounts) {
            if(discount.getDiscountId().equals(discountId)) {
                changedDiscount = service.updateDiscount(discount, updatedDiscount);
                return new ResponseEntity<>(changedDiscount, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
