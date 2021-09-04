package foodprint.backend.controller;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import foodprint.backend.model.Food;
import foodprint.backend.model.FoodRepo;

@RestController
@RequestMapping("/api/v1/restaurant/food")
public class FoodController {

    private FoodRepo repo;
    
    @Autowired
    FoodController(FoodRepo repo) {
        this.repo = repo;
    }

    //GET: Get the food
    @GetMapping({"/id/{foodId}"})
    public ResponseEntity<Food> getFood(@PathVariable("foodId") Integer id) {
        Optional<Food> food = repo.findById(id);
        if (food.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(food.get(), HttpStatus.OK);
    }

    //GET: Get ALL the food
    @GetMapping({"/all"})
    public ResponseEntity<List<Food>> getAllFood() {
        List<Food> food = repo.findAll();
        return new ResponseEntity<>(food, HttpStatus.OK);
    }

    //POST: Create new Food
    @PostMapping
    public ResponseEntity<Food> createFood(@RequestBody Food food) {
        var savedFood = repo.saveAndFlush(food);
        return new ResponseEntity<>(savedFood, HttpStatus.CREATED);
    }

    //PUT: Update Food
    @PutMapping({"/id/{foodId}"})
    public ResponseEntity<Food> updateFood(
        @PathVariable("foodId") Integer id,
        @RequestBody Food updatedFood
    ) {
        Optional<Food> currentFoodOpt = repo.findById(id);
        if (currentFoodOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var currentFood = currentFoodOpt.get();
        currentFood.setFoodDesc(updatedFood.getFoodDesc());
        currentFood.setFoodName(updatedFood.getFoodName());
        currentFood.setPicturesPath(updatedFood.getPicturesPath());
        currentFood.setFoodPrice(updatedFood.getFoodPrice());
        currentFood.setFoodDiscount(updatedFood.getFoodDiscount());
        currentFood = repo.saveAndFlush(currentFood);
        return new ResponseEntity<>(currentFood, HttpStatus.OK);
    }

    // DELETE: Delete the food
    @DeleteMapping({"/id/{foodId}"})
    public ResponseEntity<Food> deleteFood(@PathVariable("foodId") Integer id) {
        var savedFood = repo.findById(id);
        
        if (savedFood.isPresent()) {
            repo.delete(savedFood.get());
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        savedFood = repo.findById(id);
        if (savedFood.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(value = "search", produces = "application/json")
	public List<Food> searchFood(@RequestParam("q") String query) {
		
		List<Food> searchResult = repo.findByFoodNameContains(query);

		List<Food> results = new ArrayList<Food>();

		for (Food food : searchResult) {
			if (food.getFoodName().toLowerCase().contains(query.toLowerCase())) {
				results.add(new Food( food.getFoodId(), food.getFoodName(), food.getFoodPrice(), food.getFoodDiscount()));
			}
		}

        // Pagination 
        Sort sorting = Sort.by(Sort.Direction.ASC, "foodName");
        Page<Food> page = repo.findAll(PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "foodName")));
        return results;
    }
}
