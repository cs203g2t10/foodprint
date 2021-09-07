package foodprint.backend.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private UserRepo repo;

    private PasswordEncoder passwordEncoder;
    
    @Autowired
    UserController(UserRepo repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
    }

    // POST: Create the user
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public ResponseEntity<User> createUser(@RequestBody User user) {
        System.out.println(user);
        var savedUser = repo.saveAndFlush(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    // GET: Get the user by ID
    @GetMapping({"/id/{id}"})
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<User> getUser(@PathVariable("id") Long id) {
        Optional<User> user = repo.findById(id);
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user.get(), HttpStatus.OK);
    }

    // //GET: Get the food
    // @GetMapping({"/id/{foodId}"})
    // @ResponseStatus(code = HttpStatus.OK)
    // public ResponseEntity<Food> getFood(@PathVariable("foodId") Integer id) {
    //     Optional<Food> food = repo.findById(id);
    //     if (food.isEmpty()) {
    //         return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    //     }
    //     return new ResponseEntity<>(food.get(), HttpStatus.OK);
    // }

    // //GET: Get ALL the food
    // @GetMapping({"/all"})
    // @ResponseStatus(code = HttpStatus.OK)
    // public ResponseEntity<List<Food>> getAllFood() {
    //     List<Food> food = repo.findAll();
    //     return new ResponseEntity<>(food, HttpStatus.OK);
    // }

    // //POST: Create new Food
    // @PostMapping
    // @ResponseStatus(code = HttpStatus.CREATED)
    // public ResponseEntity<Food> createFood(@RequestBody Food food) {
    //     var savedFood = repo.saveAndFlush(food);
    //     return new ResponseEntity<>(savedFood, HttpStatus.CREATED);
    // }

    // //PUT: Update Food
    // @PutMapping({"/id/{foodId}"})
    // @ResponseStatus(code = HttpStatus.OK)
    // public ResponseEntity<Food> updateFood(
    //     @PathVariable("foodId") Integer id,
    //     @RequestBody Food updatedFood
    // ) {
    //     Optional<Food> currentFoodOpt = repo.findById(id);
    //     if (currentFoodOpt.isEmpty()) {
    //         return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    //     }
    //     var currentFood = currentFoodOpt.get();
    //     currentFood.setFoodDesc(updatedFood.getFoodDesc());
    //     currentFood.setFoodName(updatedFood.getFoodName());
    //     currentFood.setPicturesPath(updatedFood.getPicturesPath());
    //     currentFood.setFoodPrice(updatedFood.getFoodPrice());
    //     currentFood.setFoodDiscount(updatedFood.getFoodDiscount());
    //     currentFood = repo.saveAndFlush(currentFood);
    //     return new ResponseEntity<>(currentFood, HttpStatus.OK);
    // }

    // // DELETE: Delete the food
    // @DeleteMapping({"/id/{foodId}"})
    // @ResponseStatus(code = HttpStatus.OK)
    // public ResponseEntity<Food> deleteFood(@PathVariable("foodId") Integer id) {
    //     var savedFood = repo.findById(id);
        
    //     if (savedFood.isPresent()) {
    //         repo.delete(savedFood.get());
    //     } else {
    //         return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    //     }
        
    //     savedFood = repo.findById(id);
    //     if (savedFood.isEmpty()) {
    //         return new ResponseEntity<>(HttpStatus.OK);
    //     }
        
    //     return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    // }

    // @GetMapping(value = "search", produces = "application/json")
    // @ResponseStatus(code = HttpStatus.OK)
	// public List<Food> searchFood(@RequestParam("q") String query) {
		
	// 	List<Food> searchResult = repo.findByFoodNameContains(query);

	// 	List<Food> results = new ArrayList<Food>();

	// 	for (Food food : searchResult) {
	// 		if (food.getFoodName().toLowerCase().contains(query.toLowerCase())) {
	// 			results.add(new Food( food.getFoodId(), food.getFoodName(), food.getFoodPrice(), food.getFoodDiscount()));
	// 		}
	// 	}

    //     // Pagination 
    //     Sort sorting = Sort.by(Sort.Direction.ASC, "foodName");
    //     Page<Food> page = repo.findAll(PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "foodName")));
    //     return results;
    // }
}
