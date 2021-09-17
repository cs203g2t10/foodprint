package foodprint.backend.controller;

import java.util.Optional;
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
    @GetMapping({"/id/{restaurantId}"})
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<Restaurant> getRestaurant(@PathVariable("restaurantId") Long id) {
        Optional<Restaurant> restaurant = service.get(id);
        if (restaurant.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(restaurant.get(), HttpStatus.OK);
    }

    // GET (ALL): Get all the restaurants
    @GetMapping({"/all"})
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<List<Restaurant>> getAllRestaurants() {
        List<Restaurant> restaurants = service.getAllRestaurants();
        return new ResponseEntity<>(restaurants, HttpStatus.OK);
    }

    // POST: Create new restaurant
    @PostMapping
    @Operation(summary = "Create a new restaurant")
    @ResponseStatus(code = HttpStatus.CREATED)
    public ResponseEntity<Restaurant> createRestaurant(@RequestBody RestaurantDTO restaurantDTO) {
        Restaurant restaurant = new Restaurant(restaurantDTO.getRestaurantName(), restaurantDTO.getRestaurantLocation());
        Restaurant savedRestaurant = service.create(restaurant);
        return new ResponseEntity<>(savedRestaurant, HttpStatus.CREATED);
    }

    // PUT: Update the restaurant
    @PutMapping({"/id/{restaurantId}"})
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<Restaurant> updateRestaurant(
        @PathVariable("restaurantId") Long id,
        @RequestBody Restaurant updatedRestaurant
    ) {

        Optional<Restaurant> currentRestaurantOpt = service.get(id);
        if (currentRestaurantOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        var currentRestaurant = currentRestaurantOpt.get();
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
    @DeleteMapping({"/id/{restaurantId}"})
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<Restaurant> deleteRestaurant(@PathVariable("restaurantId") Long id) {
        var savedRestaurant = service.get(id);
        
        if (savedRestaurant.isPresent()) {
            service.delete(savedRestaurant.get());
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        savedRestaurant = service.get(id);
        if (savedRestaurant.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @GetMapping("/search")
	public Page<Restaurant> search(@RequestParam("q") String query, @RequestParam(defaultValue = "1") int pageNum) {
		Pageable page = PageRequest.of(pageNum - 1, 5); // Pagination
		Page<Restaurant> searchResult = service.search(page, query);    
        return searchResult;
    }

    @PostMapping({"/id/{restaurantId}/food"})
    @ResponseStatus(code = HttpStatus.CREATED)
    public ResponseEntity<Food> addRestaurantFood(@PathVariable("restaurantId") Long restaurantId, @RequestBody Food food) {
        Food savedFood = service.addFood(restaurantId, food);
        return new ResponseEntity<>(savedFood, HttpStatus.CREATED);
    }

    @GetMapping({"/id/{restaurantId}/food/all"})
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<List<Food>> getAllRestaurantFood(@PathVariable("restaurantId") Long restaurantId) {
        Optional<Restaurant> restaurant = service.get(restaurantId);
        if (restaurant.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<Food> allFood = restaurant.get().getAllFood();
        return new ResponseEntity<>(allFood, HttpStatus.OK);
    }

    @GetMapping({"/id/{restaurantId}/discounts"})
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<List<Discount>> getAllRestaurantDiscount(@PathVariable("restaurantId") Long restaurantId) {
        Optional<Restaurant> restaurant = service.get(restaurantId);
        if (restaurant.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<Discount> allDiscounts = restaurant.get().getAllDiscount();
        return new ResponseEntity<>(allDiscounts, HttpStatus.OK);
    }

    //GET: Get a discount of restaurant
    @GetMapping({"/id/{restaurantId}/discount/id/{discountId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets a discount of restaurant")
    public ResponseEntity<Discount> getDiscount(@PathVariable("restaurantId") Long restaurantId, @PathVariable("discountId") Long discountId) {
        Optional<Restaurant> restaurant = service.get(restaurantId);
        if (restaurant.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<Discount> allDiscounts = restaurant.get().getAllDiscount();
        for(Discount discount : allDiscounts) {
            if (discount.getDiscountId().equals(discountId)) {
                Optional<Discount> discountFound = service.getDiscount(discountId);
                return new ResponseEntity<>(discountFound.get(), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    //POST: Creates new discount for restaurant
    @PostMapping("/id/{restaurantId}/discount")
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "Creates a new discount using dto")
    public ResponseEntity<Discount> createDiscount(@PathVariable Long restaurantId, @RequestBody DiscountDTO discount) {
        Optional<Restaurant> restaurantOpt = service.get(restaurantId);

        if (restaurantOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Discount savedDiscount = service.addDiscount(restaurantId, discount);
        return new ResponseEntity<>(savedDiscount, HttpStatus.CREATED);
    }

    //DELETE: Delete a discount from restaurant
    @DeleteMapping({"/id/{restaurantId}/discount/id/{discountId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Deletes an existing discount")
    public ResponseEntity<Discount> deleteDiscount(@PathVariable("restaurantId") Long restaurantId, @PathVariable("discountId") Long discountId) {
        Optional<Restaurant> restaurant = service.get(restaurantId);
        List<Discount> allDiscounts = restaurant.get().getAllDiscount();
        
        for (Discount discount : allDiscounts) {
            if(discount.getDiscountId().equals(discountId)) {
                service.deleteDiscount(discount);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        }
        
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //PUT: Update Discount
    @PutMapping({"/id/{restaurantId}/discount/id/{discountId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Updates an existing discount")
    public ResponseEntity<Discount> updateDiscount(
        @PathVariable("restaurantId") Long restaurantId,
        @PathVariable("discountId") Long discountId,
        @RequestBody DiscountDTO updatedDiscount
    ) {
        Optional<Restaurant> restaurantOpt = service.get(restaurantId);
        if (restaurantOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Discount> allDiscounts = service.get(restaurantId).get().getAllDiscount();
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
