package foodprint.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

//import com.stripe.param.CreditNoteCreateParams.Line;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import foodprint.backend.dto.EditFoodDTO;
import foodprint.backend.dto.FoodDTO;
import foodprint.backend.dto.FoodIngredientQuantityDTO;
import foodprint.backend.exceptions.DeleteFailedException;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.model.Discount;
import foodprint.backend.model.DiscountRepo;
import foodprint.backend.model.Food;
import foodprint.backend.model.FoodRepo;
import foodprint.backend.model.Ingredient;
import foodprint.backend.model.LineItem;
import foodprint.backend.model.Picture;
import foodprint.backend.model.ReservationRepo;
import foodprint.backend.model.Reservation;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.RestaurantRepo;
import foodprint.backend.model.IngredientRepo;
import foodprint.backend.model.FoodIngredientQuantity;
import foodprint.backend.model.FoodIngredientQuantityKey;
import foodprint.backend.model.FoodIngredientQuantityRepo;

@Service
public class RestaurantService {

    private RestaurantRepo repo;

    private FoodRepo foodRepo;

    private DiscountRepo discountRepo;

    private IngredientRepo ingredientRepo;

    private PictureService pictureService;

    private ReservationRepo reservationRepo;

    private FoodIngredientQuantityRepo foodIngredientQuantityRepo;

    public RestaurantService(RestaurantRepo repo, FoodRepo foodRepo, DiscountRepo discountRepo, IngredientRepo ingredientRepo, PictureService pictureService, ReservationRepo reservationRepo, FoodIngredientQuantityRepo foodIngredientQuantityRepo) {
        this.repo = repo;
        this.foodRepo = foodRepo;
        this.discountRepo = discountRepo;
        this.ingredientRepo = ingredientRepo;
        this.pictureService = pictureService;
        this.reservationRepo = reservationRepo;
        this.foodIngredientQuantityRepo = foodIngredientQuantityRepo;
    }
    
    public List<Restaurant> getAllRestaurants() {
        List<Restaurant> restaurants = repo.findAll();
        return restaurants;
    }

    /**
     * Gets a restaurant of a given ID
     * @param id
     * @return
     */
    public Restaurant get(Long id) {
        Optional<Restaurant> restaurant = repo.findById(id);
        return restaurant.orElseThrow(() -> new NotFoundException("Restaurant not found"));
    }

    /**
     * Updates only changed fields of a given food
     * @param id
     * @param updatedRestaurant
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_MANAGER')")
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

        if (updatedRestaurant.getRestaurantCategory() != null && !updatedRestaurant.getRestaurantCategory().isEmpty()) {
            currentRestaurant.setRestaurantCategory(updatedRestaurant.getRestaurantCategory());
        }
        return repo.saveAndFlush(currentRestaurant);
    }

    /**
     * Creates a new restaurant
     * @param restaurant
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_MANAGER')")
    public Restaurant create(Restaurant restaurant) {
        return repo.saveAndFlush(restaurant);
    }

    
    /**
     * Deletes a restaurant
     * @param id
     */
    @PreAuthorize("hasAnyAuthority('FP_MANAGER')")
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

    /**
     * Searches for a restaurant, given the name and paging information
     * @param page
     * @param query
     * @return
     */
    public Page<Restaurant> search(Pageable page, String query) {
        return repo.findByRestaurantNameContainsIgnoreCase(page, query);
    }

    /**
     * Gets all available categories 
     * @return
     */
    public List<String> getCategories() {
        List<String> restaurantCategories = new ArrayList<>();
        List<Restaurant> allRestaurants = repo.findAll();
        for (Restaurant restaurant : allRestaurants) {
            for (String category : restaurant.getRestaurantCategory()) {
                if (!restaurantCategories.contains(category)) {
                    restaurantCategories.add(category);
                }
            }
        }

        return restaurantCategories;
    }

    
    /**
     * Gets all restaurants belonging to a category
     * @param restaurantCategory
     * @return
     */
    public List<Restaurant> getRestaurantsRelatedToCategory(String restaurantCategory) {
        List<Restaurant> restaurantsRelatedToCategory = new ArrayList<>();
        List<Restaurant> allRestaurants = repo.findAll();
        for (Restaurant restaurant : allRestaurants) {
            for (String category : restaurant.getRestaurantCategory()) {
                if (category.equals(restaurantCategory)) {
                    restaurantsRelatedToCategory.add(restaurant);
                }
            }
        }
        return restaurantsRelatedToCategory;
    }
    
    /*
    *
    * Food related methods
    *
    */

    /**
     * Adds a food to a given restaurant
     * @param restaurantId
     * @param foodDTO
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_MANAGER')")
    public Food addFood(Long restaurantId, FoodDTO foodDTO) {
        Restaurant restaurant = repo.findByRestaurantId(restaurantId);

        Food newFood = new Food(foodDTO.getFoodName(), foodDTO.getFoodPrice(), 0.00);
        newFood.setPictures(new ArrayList<Picture>());

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
        return savedFood;
    }

    /**
     * Gets a food of the restaurant
     * @param restaurantId
     * @param foodId
     * @return
     */
    public Food getFood(Long restaurantId, Long foodId) {
        Optional<Food> foodOpt = foodRepo.findById(foodId);
        Food food = foodOpt.orElseThrow(() -> new NotFoundException("Food not found"));
        if (food.getRestaurant().getRestaurantId() != restaurantId) {
            throw new NotFoundException("Food found but in incorrect restaurant");
        }
        return food;
    }

    /**
     * Deletes the food of a given restaurant id
     * @param restaurantId
     * @param foodId
     */
    @PreAuthorize("hasAnyAuthority('FP_MANAGER')")
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

    /**
     * Updates the food of a given restaurant, ignores fields that are null
     * @param restaurantId
     * @param foodId
     * @param updatedFood
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_MANAGER')")
    public Food updateFood(Long restaurantId, Long foodId, Food updatedFood) {
        Food originalFood = foodRepo.findById(foodId).orElseThrow(
             () -> new NotFoundException("Food requested could not be found")
        );
        if (originalFood.getRestaurant().getRestaurantId() != restaurantId) {
            throw new NotFoundException("Food requested could not be found at this restaurant");
        }

        if (updatedFood.getFoodName() != null) {
            originalFood.setFoodName(updatedFood.getFoodName()); 
        }

        if (updatedFood.getFoodDesc() != null) {
            originalFood.setFoodDesc(updatedFood.getFoodDesc());
        }

        if (updatedFood.getFoodDiscount() != null) {
            originalFood.setFoodDiscount(updatedFood.getFoodDiscount());
        }

        if (updatedFood.getFoodPrice() != null) {
            originalFood.setFoodPrice(updatedFood.getFoodPrice());
        }

        if (updatedFood.getFoodIngredientQuantity() != null) {
            originalFood.setFoodIngredientQuantity(updatedFood.getFoodIngredientQuantity());
        }
        
        originalFood = foodRepo.saveAndFlush(originalFood);
        return originalFood;
    }

    @PreAuthorize("hasAnyAuthority('FP_MANAGER')")
    public Food editFood(Long restaurantId, Long foodId, EditFoodDTO foodDTO) {
        final Food originalFood = foodRepo.findById(foodId).orElseThrow(
             () -> new NotFoundException("Food requested could not be found")
        );
        if (originalFood.getRestaurant().getRestaurantId() != restaurantId) {
            throw new NotFoundException("Food requested could not be found at this restaurant");
        }

        if (foodDTO.getFoodName() != null) {
            originalFood.setFoodName(foodDTO.getFoodName()); 
        }

        if (foodDTO.getFoodDesc() != null) {
            originalFood.setFoodDesc(foodDTO.getFoodDesc());
        }

        if (foodDTO.getFoodPrice() != null) {
            originalFood.setFoodPrice(foodDTO.getFoodPrice());
        }

        if (foodDTO.getFoodIngredientQuantity() != null) {

            Set<FoodIngredientQuantityDTO> foodIngredientQuantityDTOs = foodDTO.getFoodIngredientQuantity();
            Set<FoodIngredientQuantity> foodIngredientQuantities = new HashSet<>();

            for (FoodIngredientQuantityDTO dto: foodIngredientQuantityDTOs) {
                FoodIngredientQuantityKey key = new FoodIngredientQuantityKey(foodId, dto.getIngredientId());
                
                FoodIngredientQuantity fiq = foodIngredientQuantityRepo.findById(key).orElseGet(() -> {
                    return new FoodIngredientQuantity(originalFood, ingredientRepo.getById(dto.getIngredientId()), dto.getQuantity());
                });
                fiq.setQuantity(dto.getQuantity());

                foodIngredientQuantities.add(fiq);
            }
            originalFood.setFoodIngredientQuantity(foodIngredientQuantities);
        }
        return foodRepo.saveAndFlush(originalFood);
    }
    
    /**
     * Searches for a given food
     * @param page
     * @param query
     * @return
     */
    public Page<Food> searchFood(Pageable page, String query) {
        return foodRepo.findByFoodNameContains(page,query);
    }

    /**
     * Calculate food between dates
     * @param restaurant
     * @param startDate
     * @param endDate
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_MANAGER', 'FP_ADMIN')")
    public HashMap<String, Integer> calculateFoodNeededBetween(Restaurant restaurant, LocalDate startDate, LocalDate endDate) {

        HashMap<String, Integer> map = new HashMap<>();
        LocalDateTime start = startDate.minusDays(1).atTime(0, 0);
        LocalDateTime end = endDate.plusDays(1).atTime(0,0);
        List<Reservation> reservations = reservationRepo.findByRestaurantAndDateBetween(restaurant, start, end);

        for(Reservation reservation : reservations) {
            List<LineItem> lineItems = reservation.getLineItems();
            for (LineItem lineItem : lineItems){
                String foodName = lineItem.getFood().getFoodName();
                Integer currentAmt = map.getOrDefault(foodName, 0);
                map.put(lineItem.getFood().getFoodName(), currentAmt + lineItem.getQuantity());
            }
        }

        return map;
    }

    /*
    *
    * Discount related methods
    *
    */

    /**
     * Adds discounts to a given restaurant
     * @param restaurantId
     * @param discount
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_MANAGER')")
    public Discount addDiscount(Long restaurantId, Discount discount) {
        Restaurant restaurant = repo.findByRestaurantId(restaurantId);
        Discount Discount = new Discount();
        Discount.restaurant(restaurant)
                     .discountDescription(discount.getDiscountDescription())
                     .discountPercentage(discount.getDiscountPercentage());
        var savedDiscount = discountRepo.saveAndFlush(Discount);
        restaurant.getDiscount().add(savedDiscount);
        return savedDiscount;
    }

    /**
     * Deletes discount of a given restaurant
     * @param restaurantId
     * @param discountId
     */
    @PreAuthorize("hasAnyAuthority('FP_MANAGER')")
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

    /**
     * Updates the discount of a given restaurant, only change fields that are changed
     * @param restaurantId
     * @param discountId
     * @param updatedDiscount
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_MANAGER')")
    public Discount updateDiscount(Long restaurantId, Long discountId, Discount updatedDiscount) {
        Discount originalDiscount = discountRepo.findById(discountId).orElseThrow(() -> new NotFoundException("Discount could not be found"));
        if (originalDiscount.getRestaurant().getRestaurantId() != restaurantId) {
            throw new NotFoundException("Discount found but in incorrect restaurant");
        }

        if (updatedDiscount.getDiscountDescription() != null) {
            originalDiscount.setDiscountDescription(updatedDiscount.getDiscountDescription());
        }
        if (updatedDiscount.getDiscountPercentage() != null) {
            originalDiscount.setDiscountPercentage(updatedDiscount.getDiscountPercentage());
        }

        originalDiscount = discountRepo.saveAndFlush(originalDiscount);
        return originalDiscount;
    }

    /**
     * Gets a discount by discount ID
     * @param discountId
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_USER', 'FP_MANAGER', 'FP_ADMIN')")
    public Discount getDiscount(Long discountId) {
        Optional<Discount> discount = discountRepo.findById(discountId);
        return discount.orElseThrow(() -> new NotFoundException("Discount not found"));
    }

    /*
    * Ingredient related methods
    */

    /**
     * Gets all the restaurant ingredients
     * @param restaurantId
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_MANAGER')")
    public List<Ingredient> getAllRestaurantIngredients(Long restaurantId) {
        Restaurant restaurant = get(restaurantId);
        if (restaurant == null) {
            throw new NotFoundException("Restaurant does not exist");
        }
        return restaurant.getIngredients();
    }

    /**
     * Gets all the restaurants ingredient
     * @param restaurantId
     * @param pageNumber
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_MANAGER')")
    public Page<Ingredient> getRestaurantIngredients(Long restaurantId, Integer pageNumber) {
        Restaurant restaurant = get(restaurantId);
        if (restaurant == null) {
            throw new NotFoundException("Restaurant does not exist");
        }
        Pageable pageReq = PageRequest.of(pageNumber, 10);
        return ingredientRepo.findByRestaurantRestaurantId(pageReq, restaurantId);
    }

    /**
     * Updates an ingredient with a given ID
     * @param restaurantId
     * @param ingredientId
     * @param updatedIngredient
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_MANAGER')")
    public Ingredient updateIngredient(Long restaurantId, Long ingredientId, Ingredient updatedIngredient) {

        Ingredient originalIngredient = ingredientRepo.findById(ingredientId).orElseThrow(
            () -> new NotFoundException("Ingredient requested could not be found")
        );

        if (originalIngredient.getRestaurant().getRestaurantId() != restaurantId) {
            throw new NotFoundException("Ingredient requested could not be found at this restaurant");
        }

        if (updatedIngredient.getIngredientName() != null) {
            originalIngredient.setIngredientName(updatedIngredient.getIngredientName());
        }

        if (updatedIngredient.getIngredientDesc() != null) {
            originalIngredient.setIngredientDesc(updatedIngredient.getIngredientDesc());
        }

        if (updatedIngredient.getUnits() != null) {
            originalIngredient.setUnits(updatedIngredient.getUnits());
        }

        originalIngredient = ingredientRepo.saveAndFlush(originalIngredient);
        return originalIngredient;
    }

    /**
     * Adds an ingredient to the restaurant
     * @param restaurantId
     * @param newIngredient
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_MANAGER')")
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
  
    /**
     * Deletes an ingredient of a given restaurant
     * @param restaurantId
     * @param ingredientId
     */
    @PreAuthorize("hasAnyAuthority('FP_MANAGER')")
    public void deleteRestaurantIngredient(Long restaurantId, Long ingredientId) {

        Ingredient originalIngredient = ingredientRepo.findById(ingredientId).orElseThrow(
            () -> new NotFoundException("Ingredient requested could not be found")
        );

        if (originalIngredient.getRestaurant().getRestaurantId() != restaurantId) {
            throw new NotFoundException("Ingredient requested could not be found at this restaurant");
        }

        ingredientRepo.delete(originalIngredient);
    }

    /**
     * Calculates the ingredients needed between 2 days
     * @param restaurant
     * @param startDate
     * @param endDate
     * @return
     */

    @PreAuthorize("hasAnyAuthority('FP_MANAGER', 'FP_ADMIN')")
    public HashMap<String, Integer> calculateIngredientsNeededBetween(Restaurant restaurant, LocalDate startDate, LocalDate endDate) {
        
        HashMap<String, Integer> map = new HashMap<>();
        LocalDateTime start = startDate.minusDays(1).atTime(0, 0);
        LocalDateTime end = endDate.plusDays(1).atTime(0,0);
        List<Reservation> reservations = reservationRepo.findByRestaurantAndDateBetween(restaurant, start, end);

        for(Reservation reservation : reservations) {
            List<LineItem> lineItems = reservation.getLineItems();
        
            for (LineItem lineItem : lineItems){
                Food food = lineItem.getFood();

                Set<FoodIngredientQuantity> foodIngreQuantity = food.getFoodIngredientQuantity();
                
                for(FoodIngredientQuantity entry : foodIngreQuantity) {
                    Ingredient currIngredient = entry.getIngredient();
                    Integer currQty = map.getOrDefault(currIngredient.getIngredientName(), 0);
                    map.put(currIngredient.getIngredientName(), currQty + entry.getQuantity() * lineItem.getQuantity());
                }

            }
        }

        return map;
    }

    /**
     * Saves picture of a given restaurant
     * @param restaurantId
     * @param title
     * @param description
     * @param file
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_MANAGER')")
    public Picture savePicture(Long restaurantId, String title, String description, MultipartFile file) {
        Picture picture = pictureService.savePicture(title, description, file);
        Restaurant restaurant = get(restaurantId);
        List<Picture> picList = restaurant.getPictures();
        picList.add(picture);
        repo.saveAndFlush(restaurant);
        return picture;
    }

    /**
     * Swets the picture of a given food
     * @param restaurantId
     * @param foodId
     * @param title
     * @param description
     * @param file
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_MANAGER')")
    public Picture saveFoodPicture(Long restaurantId, Long foodId, String title, String description, MultipartFile file) {
        Picture picture = pictureService.savePicture(title, description, file);
        Food food = getFood(restaurantId, foodId);
        List<Picture> picList = food.getPictures();
        picList.add(picture);
        foodRepo.saveAndFlush(food);
        return picture;
    }

    /**
     * Checks if a given picture is found in restaurant
     * @param restaurantId
     * @param pictureId
     * @return
     */
    public Boolean pictureInRestaurant(Long restaurantId, Long pictureId) {
        Restaurant restaurant = get(restaurantId);
        List<Picture> restaurantPics = restaurant.getPictures();
        for (Picture p: restaurantPics) {
            if (p.getId().equals(pictureId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a given picture is found in the food
     * @param restaurantId
     * @param foodId
     * @param pictureId
     * @return
     */
    public Boolean pictureInFood(Long restaurantId, Long foodId, Long pictureId) {
        Food food = getFood(restaurantId, foodId);
        List<Picture> foodPics = food.getPictures();
        for (Picture p: foodPics) {
            if (p.getId().equals(pictureId)) {
                return true;
            }
        }
        return false;
    }

    public String getFoodPictureById(Long restaurantId, Long foodId, Long pictureId ) {
        if (!pictureInFood(restaurantId, foodId, pictureId)) {
            throw new NotFoundException("Picture not found in restaurant");
        }
        return pictureService.getPictureById(pictureId);
    }

    public String getPictureById(Long restaurantId, Long pictureId) {
        if (!pictureInRestaurant(restaurantId, pictureId)) {
            throw new NotFoundException("Picture not found in restaurant");
        }
        return pictureService.getPictureById(pictureId);
    }

    @PreAuthorize("hasAnyAuthority('FP_MANAGER')")
    public void deletePicture(Long restaurantId, Long pictureId) {
        if (!pictureInRestaurant(restaurantId, pictureId)) { //check if pic id is in restaurant list
            throw new NotFoundException("Picture not found in restaurant");
        } else {
            Restaurant restaurant = get(restaurantId);
            List<Picture> pictures = restaurant.getPictures();
            Picture picture = pictureService.get(pictureId);
            pictures.remove(picture);
            restaurant.setPictures(pictures);
            repo.saveAndFlush(restaurant);
            pictureService.deletePicture(pictureId); 
        }
    }

    @PreAuthorize("hasAnyAuthority('FP_MANAGER')")
    public void deleteFoodPicture(Long restaurantId, Long foodId, Long pictureId) {
        if (!pictureInFood(restaurantId, foodId, pictureId)) {
            throw new NotFoundException("Picture not found in restaurant");
        } else {
            Food food = getFood(restaurantId, foodId);
            List<Picture> pictures = food.getPictures();
            Picture picture = pictureService.get(pictureId);
            pictures.remove(picture);
            food.setPictures(pictures);
            foodRepo.saveAndFlush(food);
            pictureService.deletePicture(pictureId); 
        }
    }

    @PreAuthorize("hasAnyAuthority('FP_MANAGER')")
    public Picture updatePictureInformation(Long restaurantId, Long pictureId, Picture picture) {
        if (!pictureInRestaurant(restaurantId, pictureId)) { //check if pic id is in restaurant list
            throw new NotFoundException("Picture not found in restaurant");
        }
        return pictureService.updatedPicture(pictureId, picture);
    }

    @PreAuthorize("hasAnyAuthority('FP_MANAGER')")
    public Picture updateFoodPictureInformation(Long restaurantId, Long foodId, Long pictureId, Picture picture) {
        if (!pictureInFood(restaurantId, foodId, pictureId)) {
            throw new NotFoundException("Picture not found in restaurant");
        } 
        return pictureService.updatedPicture(pictureId, picture);
    }
}
