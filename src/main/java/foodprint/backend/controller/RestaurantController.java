package foodprint.backend.controller;

import java.util.Optional;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import foodprint.backend.dto.RestaurantDTO;
import foodprint.backend.model.Food;
import foodprint.backend.model.RestaurantRepo;
import io.swagger.v3.oas.annotations.Operation;
import foodprint.backend.model.FoodRepo;

// REST OpenAPI Swagger - http://localhost:8080/foodprint-swagger.html

@RestController
@RequestMapping("/api/v1/restaurant")
public class RestaurantController {
    
    private RestaurantRepo repo;

    @Autowired
    private FoodRepo foodRepo;

    @Autowired
    RestaurantController(RestaurantRepo repo) {
        this.repo = repo;
    }


    // GET: Get the restaurant
    @GetMapping({"/id/{restaurantId}"})
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<Restaurant> getRestaurant(@PathVariable("restaurantId") Integer id) {
        Optional<Restaurant> restaurant = repo.findById(id);
        if (restaurant.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(restaurant.get(), HttpStatus.OK);
    }

    // GET (ALL): Get all the restaurants
    @GetMapping({"/all"})
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<List<Restaurant>> getAllRestaurants() {
        List<Restaurant> restaurants = repo.findAll();
        return new ResponseEntity<>(restaurants, HttpStatus.OK);
    }

    // POST: Create new restaurant
    @PostMapping
    @Operation(summary = "create a new restaurant")
    @ResponseStatus(code = HttpStatus.CREATED)
    public ResponseEntity<Restaurant> createRestaurant(@RequestBody RestaurantDTO restaurantDTO) {
        Restaurant restaurant = new Restaurant(restaurantDTO.getRestaurantName(), restaurantDTO.getRestaurantLocation());
        var savedRestaurant = repo.saveAndFlush(restaurant);
        return new ResponseEntity<>(savedRestaurant, HttpStatus.CREATED);
    }

    // PUT: Update the restaurant
    @PutMapping({"/id/{restaurantId}"})
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<Restaurant> updateRestaurant(
        @PathVariable("restaurantId") Integer id,
        @RequestBody Restaurant updatedRestaurant
    ) {
        Optional<Restaurant> currentRestaurantOpt = repo.findById(id);
        if (currentRestaurantOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var currentRestaurant = currentRestaurantOpt.get();
        currentRestaurant.setRestaurantDesc(updatedRestaurant.getRestaurantDesc());
        currentRestaurant.setRestaurantName(updatedRestaurant.getRestaurantName());
        currentRestaurant.setRestaurantLocation(updatedRestaurant.getRestaurantLocation());
        currentRestaurant.setPicturesPath(updatedRestaurant.getPicturesPath());
        currentRestaurant.setRestaurantTableCapacity(updatedRestaurant.getRestaurantTableCapacity());
        currentRestaurant = repo.saveAndFlush(currentRestaurant);
        return new ResponseEntity<>(currentRestaurant, HttpStatus.OK);
    }

    // DELETE: Delete the restaurant
    @DeleteMapping({"/id/{restaurantId}"})
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<Restaurant> deleteRestaurant(@PathVariable("restaurantId") Integer id) {
        var savedRestaurant = repo.findById(id);
        
        if (savedRestaurant.isPresent()) {
            repo.delete(savedRestaurant.get());
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        savedRestaurant = repo.findById(id);
        if (savedRestaurant.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(value = "search", produces = "application/json")
	public Page<Restaurant> search(@RequestParam("q") String query, @RequestParam(defaultValue = "1") int pageNum) {
		Pageable pages = PageRequest.of(pageNum - 1, 5); 

        // Pagination 
		Page<Restaurant> searchResult = repo.findByRestaurantNameContains(pages, query);
        
        return searchResult;
    }

    @PostMapping({"/id/{restaurantId}/food"})
    @ResponseStatus(code = HttpStatus.CREATED)
    public ResponseEntity<Food> addRestaurantFood(@PathVariable("restaurantId") Integer restaurantId, @RequestBody Food food) {
        Restaurant restaurant = repo.findByRestaurantId(restaurantId);
        food.setRestaurant(restaurant);
        var savedFood = foodRepo.saveAndFlush(food);
        restaurant.getAllFood().add(food);
        return new ResponseEntity<>(savedFood, HttpStatus.CREATED);
    }

    @GetMapping({"/id/{restaurantId}/allFood"})
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<List<Food>> getAllRestaurantFood(@PathVariable("restaurantId") Integer restaurantId) {
        Restaurant restaurant = repo.findByRestaurantId(restaurantId);
        List<Food> allFood = restaurant.getAllFood();
        return new ResponseEntity<>(allFood, HttpStatus.OK);
    }

}
