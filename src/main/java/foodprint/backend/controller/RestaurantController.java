package foodprint.backend.controller;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.validation.Valid;
// import javax.ws.rs.BadRequestException;

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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import foodprint.backend.dto.DiscountDTO;
import foodprint.backend.dto.EditFoodDTO;
import foodprint.backend.dto.FoodDTO;
import foodprint.backend.dto.IngredientDTO;
import foodprint.backend.dto.PictureDTO;
import foodprint.backend.dto.RestaurantDTO;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.exceptions.BadRequestException;
import foodprint.backend.model.Discount;
import foodprint.backend.model.Food;
import foodprint.backend.model.Ingredient;
import foodprint.backend.model.Picture;
import foodprint.backend.model.Restaurant;
// import foodprint.backend.service.EmailService;
import foodprint.backend.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
// REST OpenAPI Swagger - http://localhost:8080/foodprint-swagger.html
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/restaurant")
public class RestaurantController {
    
    private RestaurantService service;
	// private EmailService emailService;

    @Autowired
    RestaurantController(RestaurantService service) {
        this.service = service;
    }

    // @Autowired
    // RestaurantController(RestaurantService service, EmailService emailService) {
    //     this.service = service;
    //     this.emailService = emailService;
    // }

    // GET: Get the restaurant
    @GetMapping({"/{restaurantId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets a single restaurant from ID")
    public ResponseEntity<RestaurantDTO> restaurantGet(@PathVariable("restaurantId") Long id) {
        Restaurant restaurant = service.get(id);
        RestaurantDTO restaurantDto = convertToDTO(restaurant);
        return new ResponseEntity<>(restaurantDto, HttpStatus.OK);
    }

    // GET (ALL): Get all the restaurants
    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets all restaurants")
    public ResponseEntity<List<RestaurantDTO>> restaurantGetAll() {
        List<Restaurant> restaurants = service.getAllRestaurants();
        List<RestaurantDTO> restaurantDtos = restaurants.stream().map(r -> convertToDTO(r)).collect(Collectors.toList());
        return new ResponseEntity<>(restaurantDtos, HttpStatus.OK);
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

    // PATCH: Update the restaurant
    @PatchMapping({"/{restaurantId}"})
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


    @GetMapping({"/search"})
    @Operation(summary = "Search for an existing restaurant")
	public Page<RestaurantDTO> restaurantSearch(
        @RequestParam("q") String query, 
        @RequestParam(name = "p", defaultValue = "1") int pageNum,
        @RequestParam(name = "sortBy", defaultValue = "restaurantName") String sortField,
        @RequestParam(name = "sortDesc", defaultValue ="false") Boolean sortDesc
    ) {
        Direction direction = (sortDesc) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sorting = Sort.by(direction, sortField);
		Pageable page = PageRequest.of(pageNum - 1, 16, sorting); // Pagination
		Page<Restaurant> searchResult = service.search(page, query);
        Page<RestaurantDTO> searchResultsDTO = searchResult.map(result -> convertToDTO(result));
        return searchResultsDTO;
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
    @Operation(summary = "Get a list of restaurants associated with selected category")
    public ResponseEntity<List<Restaurant>> getRestaurantRelatedToCategory(@PathVariable("category") String restaurantCategory) {
            List<Restaurant> restaurantsRelatedToCategory = service.getRestaurantsRelatedToCategory(restaurantCategory);
            return new ResponseEntity<>(restaurantsRelatedToCategory, HttpStatus.OK);
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
            throw new NotFoundException("restaurant does not exist");

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
        try {
            service.getFood(restaurantId, foodId);
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
    public ResponseEntity<Discount> createDiscount(@PathVariable Long restaurantId, @RequestBody @Valid DiscountDTO discount) {
        // Restaurant restaurantOpt = service.get(restaurantId);
        Discount newDiscount = new Discount(discount.getDiscountDescription(), discount.getDiscountPercentage());
        Discount savedDiscount = service.addDiscount(restaurantId, newDiscount);
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

    @PatchMapping({"/{restaurantId}/discount/{discountId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Updates an existing restaurant, only changed fields need to be set")
    public ResponseEntity<Discount> updateRestaurantDiscount(
        @PathVariable("restaurantId") Long restaurantId,
        @PathVariable("discountId") Long discountId,
        @RequestBody Discount updatedDiscount
    ) {
        Discount discount = service.updateDiscount(restaurantId, discountId, updatedDiscount);
        return new ResponseEntity<>(discount, HttpStatus.OK);
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
    public ResponseEntity<Ingredient> createRestaurantIngredient(@PathVariable Long restaurantId, @RequestBody IngredientDTO ingredientDTO) {
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
    public ResponseEntity<Ingredient> modifyRestaurantIngredient(@PathVariable Long restaurantId, @PathVariable Long ingredientId, @RequestBody IngredientDTO ingredientDto) {
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
    public ResponseEntity<Map<String, Integer>> calculateIngredientsBetween (@PathVariable Long restaurantId, @RequestParam("start") String startDate, @RequestParam("end") String endDate) {
        Restaurant restaurant = service.get(restaurantId);
        if(restaurant == null) {
            throw new NotFoundException("restaurant does not exist");
        }
        
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        if (start.isBefore(LocalDateTime.now().toLocalDate())) {
            throw new BadRequestException("starting date should be after today");
        } else if (start.isAfter(end)) {
            throw new BadRequestException("start date should be before end date");
        }
        Map<String, Integer> result = service.calculateIngredientsNeededBetween(restaurant, start, end);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping({"/{restaurantId}/calculateFoodBetween"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Calculate food needed between")
    public ResponseEntity<Map<String, Integer>> calculateFoodBetween(@PathVariable Long restaurantId, @RequestParam("start") String startDate, @RequestParam("end") String endDate) {
        Restaurant restaurant = service.get(restaurantId);
        if(restaurant == null)
            throw new NotFoundException("restaurant does not exist");
        
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

    @GetMapping({"/{restaurantId}/picture/{pictureId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Get a picture of a restaurant by using restaurant id and picture id")
    public ResponseEntity<String> getPictureById(@PathVariable("restaurantId") Long restaurantId, @PathVariable("pictureId") Long pictureId) {
        String url = service.getPictureById(restaurantId, pictureId);
        return new ResponseEntity<>(url, HttpStatus.OK);
    }

    @DeleteMapping({"/{restaurantId}/picture/{pictureId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Deletes a restaurant's picture by id")
    public ResponseEntity<Picture> deletePicture(@PathVariable("restaurantId") Long restaurantId, @PathVariable("pictureId") Long pictureId) {
        service.deletePicture(restaurantId, pictureId);
        try {
            service.getPictureById(restaurantId, pictureId);
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping({"/{restaurantId}/picture/{pictureId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Updates a picture's title and description")
    public ResponseEntity<Picture> updatePictureInformation(
        @PathVariable("restaurantId") Long restaurantId,
        @PathVariable("pictureId") Long pictureId,
        @RequestBody PictureDTO updatedPicture
    ) {
        Picture picture = new Picture(updatedPicture.getTitle() , updatedPicture.getDescription());
        Picture savedPicture = service.updatePictureInformation(restaurantId, pictureId, picture);
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

    @GetMapping({"/{restaurantId}/food/{foodId}/picture/{pictureId}/"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Get a picture of a restaurant's food by using restaurant id, picture id and food id")
    public ResponseEntity<String> getPictureById(@PathVariable("restaurantId") Long restaurantId,  @PathVariable("foodId") Long foodId,  @PathVariable("pictureId") Long pictureId) {
        String url = service.getFoodPictureById(restaurantId, foodId, pictureId);
        return new ResponseEntity<>(url, HttpStatus.OK);
    }

    @DeleteMapping({"/{restaurantId}/food/{foodId}/picture/{pictureId}/"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Deletes a food's picture by id")
    public ResponseEntity<Picture> deleteFoodPicture(@PathVariable("restaurantId") Long restaurantId, @PathVariable("foodId") Long foodId, @PathVariable("pictureId") Long pictureId) {
        service.deleteFoodPicture(restaurantId, foodId, pictureId);
        try {
            service.getFoodPictureById(restaurantId, foodId, pictureId);
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping({"/{restaurantId}/food/{foodId}/picture/{pictureId}/"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Updates a picture's title and description")
    public ResponseEntity<Picture> updateFoodPictureInformation(
        @PathVariable("restaurantId") Long restaurantId,
        @PathVariable("foodId") Long foodId,
        @PathVariable("pictureId") Long pictureId,
        @RequestBody PictureDTO updatedPicture
    ) {
        Picture picture = new Picture(updatedPicture.getTitle() , updatedPicture.getDescription());
        Picture savedPicture = service.updateFoodPictureInformation(restaurantId, foodId, pictureId, picture);
        return new ResponseEntity<>(savedPicture, HttpStatus.OK);
    }

    // DTO <-> Entity Conversion Helper Methods
    public Restaurant convertToEntity(RestaurantDTO restaurantDTO) {
        Restaurant restaurant = new Restaurant(restaurantDTO.getRestaurantName(), restaurantDTO.getRestaurantLocation());
        restaurant.setRestaurantDesc(restaurantDTO.getRestaurantDesc());
        restaurant.setRestaurantTableCapacity(restaurantDTO.getRestaurantTableCapacity());
        restaurant.setRestaurantWeekdayClosingHour(restaurantDTO.getRestaurantWeekdayClosingHour());
        restaurant.setRestaurantWeekdayClosingMinutes(restaurantDTO.getRestaurantWeekdayClosingMinutes());
        restaurant.setRestaurantWeekdayOpeningHour(restaurantDTO.getRestaurantWeekdayOpeningHour());
        restaurant.setRestaurantWeekdayOpeningMinutes(restaurantDTO.getRestaurantWeekdayOpeningMinutes());
        restaurant.setRestaurantWeekendClosingHour(restaurantDTO.getRestaurantWeekendClosingHour());
        restaurant.setRestaurantWeekendClosingMinutes(restaurantDTO.getRestaurantWeekendClosingMinutes());
        restaurant.setRestaurantWeekendOpeningHour(restaurantDTO.getRestaurantWeekendOpeningHour());
        restaurant.setRestaurantWeekendOpeningMinutes(restaurantDTO.getRestaurantWeekendOpeningMinutes());
        restaurant.setRestaurantCategory(restaurantDTO.getRestaurantCategory());
        return restaurant;
    }

    public RestaurantDTO convertToDTO(Restaurant restaurant) {
        RestaurantDTO dto = new RestaurantDTO();
        dto.setRestaurantId(restaurant.getRestaurantId());
        dto.setRestaurantName(restaurant.getRestaurantName());
        dto.setRestaurantLocation(restaurant.getRestaurantLocation());
        dto.setRestaurantDesc(restaurant.getRestaurantDesc());
        dto.setRestaurantTableCapacity(restaurant.getRestaurantTableCapacity());
        dto.setRestaurantWeekdayClosingHour(restaurant.getRestaurantWeekdayClosingHour());
        dto.setRestaurantWeekdayClosingMinutes(restaurant.getRestaurantWeekdayClosingMinutes());
        dto.setRestaurantWeekdayOpeningHour(restaurant.getRestaurantWeekdayOpeningHour());
        dto.setRestaurantWeekdayOpeningMinutes(restaurant.getRestaurantWeekdayOpeningMinutes());
        dto.setRestaurantWeekendClosingHour(restaurant.getRestaurantWeekendClosingHour());
        dto.setRestaurantWeekendClosingMinutes(restaurant.getRestaurantWeekendClosingMinutes());
        dto.setRestaurantWeekendOpeningHour(restaurant.getRestaurantWeekendOpeningHour());
        dto.setRestaurantWeekendOpeningMinutes(restaurant.getRestaurantWeekendOpeningMinutes());
        dto.setRestaurantCategory(restaurant.getRestaurantCategory());
        
        List<Picture> pictures = restaurant.getPictures();
        List<PictureDTO> pictureDtos = new ArrayList<>();
        dto.setPictures(pictureDtos);

        for (Picture picture : pictures) {
            PictureDTO picDto = new PictureDTO(picture.getTitle(), picture.getDescription(), picture.getUrl());
            pictureDtos.add(picDto);
        }

        return dto;
    }
}
