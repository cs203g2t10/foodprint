package foodprint.backend.controller;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import foodprint.backend.dto.DiscountDTO;
import foodprint.backend.dto.EditFoodDTO;
import foodprint.backend.dto.FoodDTO;
import foodprint.backend.dto.IngredientCalculationDTO;
import foodprint.backend.dto.IngredientDTO;
import foodprint.backend.dto.RestaurantDTO;
import foodprint.backend.dto.UpdatePictureDTO;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.exceptions.AlreadyExistsException;
import foodprint.backend.exceptions.BadRequestException;
import foodprint.backend.model.Discount;
import foodprint.backend.model.Food;
import foodprint.backend.model.Ingredient;
import foodprint.backend.model.Picture;
import foodprint.backend.model.Restaurant;
import foodprint.backend.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/restaurant")
public class RestaurantController {
    
    private RestaurantService service;
    private static final String RESTAURANT_NOT_FOUND = "restaurant does not exist";

    @Autowired
    RestaurantController(RestaurantService service) {
        this.service = service;
    }

    // GET: Get the restaurant
    @GetMapping({"/{restaurantId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets a single restaurant from ID")
    public ResponseEntity<RestaurantDTO> restaurantGet(@PathVariable("restaurantId") Long id) {
        Restaurant restaurant = service.get(id);
        RestaurantDTO restaurantDto = restaurantConvertToDTO(restaurant);
        return new ResponseEntity<>(restaurantDto, HttpStatus.OK);
    }

    // GET (ALL): Get all the restaurants
    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets all restaurants")
    public ResponseEntity<List<RestaurantDTO>> restaurantGetAll() {
        List<Restaurant> restaurants = service.getAllRestaurants();
        List<RestaurantDTO> restaurantDtos = restaurants.stream().map(this::restaurantConvertToDTO).collect(Collectors.toList());

        return new ResponseEntity<>(restaurantDtos, HttpStatus.OK);
    }

    // GET (ALL): Get all the restaurants
    @GetMapping({"/page"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets all restaurants")
    public ResponseEntity<Page<RestaurantDTO>> restaurantGetAllPaged(@RequestParam(name="p", defaultValue="0") int page) {
        Page<Restaurant> restaurants = service.getAllRestaurantsPaged(page);
        Page<RestaurantDTO> restaurantDtos = restaurants.map(this::restaurantConvertToDTO);
        return new ResponseEntity<>(restaurantDtos, HttpStatus.OK);
    }

    // POST: Create new restaurant
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "Create a new restaurant using DTO")
    public ResponseEntity<Restaurant> restaurantCreate(@RequestBody @Valid RestaurantDTO restaurantDTO) {
        var convertedRestaurant = restaurantConvertToEntity(restaurantDTO);
        Restaurant savedRestaurant = service.create(convertedRestaurant);
        return new ResponseEntity<>(savedRestaurant, HttpStatus.CREATED);
    }

    // PATCH: Update the restaurant
    @PatchMapping({"/{restaurantId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Updates an existing restaurant, only changed fields need to be set")
    public ResponseEntity<Restaurant> restaurantUpdate(
        @PathVariable("restaurantId") Long id,
        @RequestBody RestaurantDTO updatedRestaurant) {
        Restaurant restaurant = restaurantConvertToEntity(updatedRestaurant);
        return new ResponseEntity<>(service.update(id, restaurant), HttpStatus.OK);
    }


    // DELETE: Delete the restaurant
    @DeleteMapping({"/{restaurantId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Deletes an existing restaurant")
    public ResponseEntity<Restaurant> restaurantDelete(@PathVariable("restaurantId") Long id) {
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @GetMapping({"/search"})
    @Operation(summary = "Search for an existing restaurant")
	public Page<RestaurantDTO> restaurantSearch(
        @RequestParam("q") String query, 
        @RequestParam(name = "p", defaultValue = "1") int pageNum,
        @RequestParam(name = "sortBy", defaultValue = "restaurantName") String sortField,
        @RequestParam(name = "sortDesc", defaultValue ="false") boolean sortDesc
    ) {
        Direction direction = (sortDesc) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sorting = Sort.by(direction, sortField);
		Pageable page = PageRequest.of(pageNum - 1, 16, sorting); // Pagination
		Page<Restaurant> searchResult = service.search(page, query);
        return searchResult.map(this::restaurantConvertToDTO);
    } 

    @GetMapping({"/categories"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Get a list of categories available")
    public ResponseEntity<List<String>> getRestaurantCategories() {
        List<String> restaurantCategories = service.getCategories();
        return new ResponseEntity<>(restaurantCategories, HttpStatus.OK);
    }

    @GetMapping({"/categories/{category}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Get a list of restaurant DTOs associated with selected category")
    public ResponseEntity<List<RestaurantDTO>> getRestaurantRelatedToCategory(@PathVariable("category") String restaurantCategory) {
            List<Restaurant> restaurantsRelatedToCategory = service.getRestaurantsRelatedToCategory(restaurantCategory);
            List<RestaurantDTO> restaurantDtos = restaurantsRelatedToCategory.stream().map(this::restaurantConvertToDTO).collect(Collectors.toList());
            return new ResponseEntity<>(restaurantDtos, HttpStatus.OK);
    }

    /*
    *
    * Food related mappings
    *
    */

    @PostMapping({"/{restaurantId}/food"})
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "Creates a new food instance within a restaurant")
    public ResponseEntity<Food> addRestaurantFood(@PathVariable("restaurantId") Long restaurantId, @RequestBody @Valid FoodDTO food) {
        Restaurant restaurant = service.get(restaurantId);
        if(restaurant == null)
            throw new NotFoundException(RESTAURANT_NOT_FOUND);

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

    // DELETE: Delete the food
    @DeleteMapping({"/{restaurantId}/food/{foodId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Deletes an existing food item")
    public ResponseEntity<Food> deleteRestaurantFood(@PathVariable("restaurantId") Long restaurantId, @PathVariable("foodId") Long foodId) {
        service.deleteFood(restaurantId, foodId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping({"/{restaurantId}/food/{foodId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Updates an existing food item")
    public ResponseEntity<Food> updateRestaurantFood(
        @PathVariable("restaurantId") Long restaurantId,
        @PathVariable("foodId") Long foodId,
        @RequestBody @Valid EditFoodDTO updatedFood
    ) {
        Food food = service.editFood(restaurantId, foodId, updatedFood);
        return new ResponseEntity<>(food, HttpStatus.OK);
    }

    /*
    *
    * Discount related mappings
    *
    */

    //POST: Creates new discount for restaurant
    @PostMapping("/{restaurantId}/discount")
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "Creates a new discount using dto")
    public ResponseEntity<Discount> createDiscount(@PathVariable Long restaurantId, @RequestBody @Valid DiscountDTO discount) {
        Discount newDiscount = new Discount(discount.getDiscountDescription(), discount.getDiscountPercentage());
        try {
            Discount savedDiscount = service.createDiscount(restaurantId, newDiscount);
            return new ResponseEntity<>(savedDiscount, HttpStatus.CREATED);
        } catch (AlreadyExistsException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        
    }

    //DELETE: Delete a discount from restaurant
    @DeleteMapping({"/{restaurantId}/discount"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Deletes an existing discount")
    public ResponseEntity<Discount> deleteDiscount(@PathVariable("restaurantId") Long restaurantId) {
        service.deleteDiscount(restaurantId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping({"/{restaurantId}/discount"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Updates an existing discount of restaurant, only changed fields need to be set")
    public ResponseEntity<Discount> updateRestaurantDiscount(
        @PathVariable("restaurantId") Long restaurantId,
        @RequestBody DiscountDTO updatedDiscount
    ) { 
        ModelMapper mapper = new ModelMapper();
        Discount discount = mapper.map(updatedDiscount, Discount.class);
        return new ResponseEntity<>(service.updateDiscount(restaurantId, discount), HttpStatus.OK);
    }

    /*
    *
    * Ingredients related mappings
    * 
    */

    // POST: Creates new ingredient for restaurant
    @PostMapping("/{restaurantId}/ingredient")
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "Creates a new ingredient for restaurant")
    public ResponseEntity<Ingredient> createRestaurantIngredient(@PathVariable Long restaurantId, @RequestBody @Valid IngredientDTO ingredientDTO) {
        Ingredient newIngredient = new Ingredient(ingredientDTO.getIngredientName());
        newIngredient.setIngredientDesc(ingredientDTO.getIngredientDesc());
        newIngredient.setUnits(ingredientDTO.getUnits());
        newIngredient = service.addRestaurantIngredient(restaurantId, newIngredient);
        return new ResponseEntity<>(newIngredient, HttpStatus.CREATED);
    }

    // GET: Get paged ingredients for restaurant
    @GetMapping("/{restaurantId}/ingredient")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets paged restaurant's ingredients")
    public ResponseEntity<Page<Ingredient>> restaurantGetIngredientsPaged(
        @PathVariable Long restaurantId, 
        @RequestParam(value = "p", defaultValue = "0", required = false) Integer pageNumber
    ) {
        Page<Ingredient> ingredients = service.getRestaurantIngredients(restaurantId, pageNumber);
        return new ResponseEntity<>(ingredients, HttpStatus.OK);
    }

    // GET: Get all ingredients for restaurant
    @GetMapping("/{restaurantId}/ingredient/all")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets all the restaurant ingredients")
    public ResponseEntity<List<Ingredient>> restaurantGetIngredientsAll(@PathVariable Long restaurantId) {
        List<Ingredient> allIngredients = service.getAllRestaurantIngredients(restaurantId);
        return new ResponseEntity<>(allIngredients, HttpStatus.OK);
    }

    // PATCH: Modify an ingredient for a restaurant
    @PatchMapping("/{restaurantId}/ingredient/{ingredientId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Modify an ingredient")
    public ResponseEntity<Ingredient> modifyRestaurantIngredient(@PathVariable Long restaurantId, @PathVariable Long ingredientId, @RequestBody @Valid IngredientDTO ingredientDto) {
        Ingredient modifiedIngredient = new Ingredient(ingredientDto.getIngredientName());
        modifiedIngredient.setIngredientDesc(ingredientDto.getIngredientDesc());
        modifiedIngredient.setUnits(ingredientDto.getUnits());
        modifiedIngredient = service.updateIngredient(restaurantId, ingredientId, modifiedIngredient);
        return new ResponseEntity<>(modifiedIngredient, HttpStatus.OK);
    }

    @DeleteMapping("/{restaurantId}/ingredient/{ingredientId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Delete an existing ingredient")
    public ResponseEntity<Ingredient> deleteRestaurantIngredient(@PathVariable Long restaurantId, @PathVariable Long ingredientId) {
        service.deleteRestaurantIngredient(restaurantId, ingredientId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping({"/{restaurantId}/calculateIngredientsBetween"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Calculate ingredients needed between start and end date (inclusive)")
    public ResponseEntity<List<IngredientCalculationDTO>> calculateIngredientsBetween (@PathVariable Long restaurantId, @RequestParam("start") String startDate, @RequestParam("end") String endDate) {
        Restaurant restaurant = service.get(restaurantId);
        if(restaurant == null) {
            throw new NotFoundException(RESTAURANT_NOT_FOUND);
        }
        
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        if (start.isBefore(LocalDateTime.now().toLocalDate())) {
            throw new BadRequestException("starting date should be after today");
        } else if (start.isAfter(end)) {
            throw new BadRequestException("start date should be before end date");
        }

        Map<Ingredient, Integer> ingredientQuantity = service.calculateIngredientsNeededBetween(restaurant, start, end);
        List<IngredientCalculationDTO> result = new ArrayList<>();

        Set<Ingredient> ingredientSet = ingredientQuantity.keySet();
        for(Ingredient ingredient : ingredientSet) {
            result.add(new IngredientCalculationDTO(ingredient.getIngredientName(), ingredientQuantity.get(ingredient), ingredient.getUnits()));
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping({"/{restaurantId}/calculateFoodBetween"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Calculate food needed between")
    public ResponseEntity<Map<String, Integer>> calculateFoodBetween(@PathVariable Long restaurantId, @RequestParam("start") String startDate, @RequestParam("end") String endDate) {
        Restaurant restaurant = service.get(restaurantId);
        if(restaurant == null)
            throw new NotFoundException(RESTAURANT_NOT_FOUND);
        
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        if (start.isBefore(LocalDateTime.now().toLocalDate())) {
            throw new BadRequestException("starting date should be after today");
        } else if (start.isAfter(end)) {
            throw new BadRequestException("start date should be before end date");
        }
        Map<String, Integer> result = service.calculateFoodNeededBetween(restaurant, start, end);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
   

    @PostMapping(path = "/{restaurantId}/uploadPicture",
                consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.CREATED)
    public ResponseEntity<Picture> savePicture(@PathVariable("restaurantId") Long restaurantId, @RequestParam("title") String title,
                                            @RequestParam("description") String description,
                                            @RequestParam("file") MultipartFile file) {                                
        return new ResponseEntity<>(service.savePicture(restaurantId, title, description, file), HttpStatus.CREATED);
    }

    @DeleteMapping({"/{restaurantId}/picture"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Deletes a restaurant's picture")
    public ResponseEntity<Picture> deletePicture(@PathVariable("restaurantId") Long restaurantId) {
        service.deleteRestaurantPicture(restaurantId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping(path = "/{restaurantId}/picture", 
                consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Updates a picture's title and description")
    public ResponseEntity<Picture> updatePictureInformation(
        @PathVariable("restaurantId") Long restaurantId,
        @ModelAttribute UpdatePictureDTO updatedPicture
    ) {
        Picture savedPicture = service.updateRestaurantPicture(restaurantId, updatedPicture);
        return new ResponseEntity<>(savedPicture, HttpStatus.OK);
    }

     
    @PostMapping(path = "/{restaurantId}/food/{foodId}/uploadPicture",
                consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.CREATED)
    public ResponseEntity<Picture> savePictureForFood(@PathVariable("restaurantId") Long restaurantId, @PathVariable("foodId") Long foodId, @RequestParam("title") String title,
                                            @RequestParam("description") String description,
                                            @RequestParam("file") MultipartFile file) {                                
        return new ResponseEntity<>(service.saveFoodPicture(restaurantId, foodId, title, description, file), HttpStatus.CREATED);
    }

    @DeleteMapping({"/{restaurantId}/food/{foodId}/picture"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Deletes a food's picture")
    public ResponseEntity<Picture> deleteFoodPicture(@PathVariable("restaurantId") Long restaurantId, @PathVariable("foodId") Long foodId) {
        service.deleteFoodPicture(restaurantId, foodId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping(path = "/{restaurantId}/food/{foodId}/picture",
                    consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Updates a picture's title and description")
    public ResponseEntity<Picture> updateFoodPictureInformation(
        @PathVariable("restaurantId") Long restaurantId,
        @PathVariable("foodId") Long foodId,
        @ModelAttribute UpdatePictureDTO updatedPicture
    ) {
        Picture savedPicture = service.updateFoodPicture(restaurantId, foodId, updatedPicture);
        return new ResponseEntity<>(savedPicture, HttpStatus.OK);
    }

    private Restaurant restaurantConvertToEntity(RestaurantDTO restaurantDTO) {
        ModelMapper mapper = new ModelMapper();
        return mapper.map(restaurantDTO, Restaurant.class);
    }

    private RestaurantDTO restaurantConvertToDTO(Restaurant restaurant) {
        ModelMapper mapper = new ModelMapper();
        return mapper.map(restaurant, RestaurantDTO.class);
    }

}
