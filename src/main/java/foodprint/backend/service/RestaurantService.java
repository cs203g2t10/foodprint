package foodprint.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import foodprint.backend.dto.EditFoodDTO;
import foodprint.backend.dto.FoodDTO;
import foodprint.backend.dto.FoodIngredientQuantityDTO;
import foodprint.backend.dto.UpdatePictureDTO;
import foodprint.backend.exceptions.AlreadyExistsException;
import foodprint.backend.exceptions.BadRequestException;
import foodprint.backend.exceptions.DeleteFailedException;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.model.Discount;
import foodprint.backend.model.DiscountRepo;
import foodprint.backend.model.Food;
import foodprint.backend.model.FoodIngredientQuantity;
import foodprint.backend.model.FoodIngredientQuantityKey;
import foodprint.backend.model.FoodIngredientQuantityRepo;
import foodprint.backend.model.FoodRepo;
import foodprint.backend.model.Ingredient;
import foodprint.backend.model.IngredientRepo;
import foodprint.backend.model.LineItem;
import foodprint.backend.model.Picture;
import foodprint.backend.model.Reservation;
import foodprint.backend.model.ReservationRepo;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.RestaurantRepo;

@Service
public class RestaurantService {

    private static final String PICTURE_NOT_FOUND_MESSAGE = "Picture not found in restaurant";
    private static final String RESTAURANT_DOES_NOT_EXIST_MESSAGE = "Restaurant does not exist";
    private static final String DISCOUNT_NOT_FOUND_MESSAGE = "Discount not found";

    private RestaurantRepo repo;

    private FoodRepo foodRepo;

    private DiscountRepo discountRepo;

    private IngredientRepo ingredientRepo;

    private PictureService pictureService;

    private ReservationRepo reservationRepo;

    private FoodIngredientQuantityRepo foodIngredientQuantityRepo;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public RestaurantService(RestaurantRepo repo, FoodRepo foodRepo, DiscountRepo discountRepo,
            IngredientRepo ingredientRepo, PictureService pictureService, ReservationRepo reservationRepo,
            FoodIngredientQuantityRepo foodIngredientQuantityRepo) {
        this.repo = repo;
        this.foodRepo = foodRepo;
        this.discountRepo = discountRepo;
        this.ingredientRepo = ingredientRepo;
        this.pictureService = pictureService;
        this.reservationRepo = reservationRepo;
        this.foodIngredientQuantityRepo = foodIngredientQuantityRepo;
    }

    public List<Restaurant> getAllRestaurants() {
        return repo.findAll();
    }

    public Page<Restaurant> getAllRestaurantsPaged(int pageNumber) {
        Pageable pageReq = PageRequest.of(pageNumber, 8);
        return repo.findAll(pageReq);
    }

    /**
     * Gets a restaurant of a given ID
     * 
     * @param id
     * @return
     */
    public Restaurant get(Long id) {
        Optional<Restaurant> restaurant = repo.findById(id);
        return restaurant.orElseThrow(() -> new NotFoundException("Restaurant not found"));
    }

    /**
     * Updates only changed fields of a given food
     * 
     * @param id
     * @param updatedRestaurant
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public Restaurant update(Long id, Restaurant updatedRestaurant) {

        Restaurant currentRestaurant = this.get(id);

        if (updatedRestaurant.getRestaurantDesc() != null) {
            currentRestaurant.setRestaurantDesc(updatedRestaurant.getRestaurantDesc());
        }
        if (updatedRestaurant.getRestaurantName() != null) {
            currentRestaurant.setRestaurantName(updatedRestaurant.getRestaurantName());
        }
        if (updatedRestaurant.getRestaurantLocation() != null) {
            currentRestaurant.setRestaurantLocation(updatedRestaurant.getRestaurantLocation());
        }

        if (updatedRestaurant.getRestaurantPriceRange() != null) {
            currentRestaurant.setRestaurantPriceRange(updatedRestaurant.getRestaurantPriceRange());
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
            currentRestaurant
                    .setRestaurantWeekdayClosingMinutes(updatedRestaurant.getRestaurantWeekdayClosingMinutes());
        }
        if (updatedRestaurant.getRestaurantWeekdayOpeningMinutes() != null) {
            currentRestaurant
                    .setRestaurantWeekdayOpeningMinutes(updatedRestaurant.getRestaurantWeekdayOpeningMinutes());
        }
        if (updatedRestaurant.getRestaurantWeekendClosingMinutes() != null) {
            currentRestaurant
                    .setRestaurantWeekendClosingMinutes(updatedRestaurant.getRestaurantWeekendClosingMinutes());
        }
        if (updatedRestaurant.getRestaurantWeekendOpeningMinutes() != null) {
            currentRestaurant
                    .setRestaurantWeekendOpeningMinutes(updatedRestaurant.getRestaurantWeekendOpeningMinutes());
        }

        if (updatedRestaurant.getRestaurantCategory() != null && !updatedRestaurant.getRestaurantCategory().isEmpty()) {
            currentRestaurant.setRestaurantCategory(updatedRestaurant.getRestaurantCategory());
        }

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Restaurant>> violations = validator.validate(currentRestaurant);
        for (ConstraintViolation<Restaurant> violation : violations) {
            log.error(violation.getMessage());
        }
        return repo.saveAndFlush(currentRestaurant);
    }

    /**
     * Creates a new restaurant
     * 
     * @param restaurant
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public Restaurant create(Restaurant restaurant) {
        return repo.saveAndFlush(restaurant);
    }

    /**
     * Deletes a restaurant
     * 
     * @param id
     */
    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public void delete(Long id) {
        Restaurant restaurant = this.get(id);
        repo.delete(restaurant);
        try {
            this.get(id);
            throw new DeleteFailedException("Restaurant could not be deleted");
        } catch (NotFoundException ex) {
            throw new NotFoundException(RESTAURANT_DOES_NOT_EXIST_MESSAGE);
        }
    }

    /**
     * Searches for a restaurant, given the name and paging information
     * 
     * @param page
     * @param query
     * @return
     */
    public Page<Restaurant> search(Pageable page, String query) {
        return repo.findByRestaurantNameContainsIgnoreCase(page, query);
    }

    /**
     * Gets all available categories
     * 
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
     * 
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
     * 
     * @param restaurantId
     * @param foodDTO
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public Food addFood(Long restaurantId, FoodDTO foodDTO) {
        if (foodDTO.getIngredientQuantityList().isEmpty()) {
            throw new BadRequestException("Food should have at least 1 ingredient");
        }

        Restaurant restaurant = repo.findByRestaurantId(restaurantId);

        Food newFood = new Food(foodDTO.getFoodName(), foodDTO.getFoodDesc(), foodDTO.getFoodPrice(), 0.00);
        newFood.setPicture(null);

        List<Ingredient> ingredients = getAllRestaurantIngredients(restaurantId);
        Set<FoodIngredientQuantity> foodIngredientQuantity = new HashSet<>();
        List<FoodIngredientQuantityDTO> ingredientQuantity = foodDTO.getIngredientQuantityList();

        for (FoodIngredientQuantityDTO entry : ingredientQuantity) {
            Ingredient newIngredient = null;
            for (Ingredient ingredient : ingredients) {
                if (ingredient.getIngredientId().equals(entry.getIngredientId())) {
                    newIngredient = ingredient;
                }
            }

            if (newIngredient == null) {
                throw new NotFoundException("restaurant does not have the ingredient");
            }

            FoodIngredientQuantity newFoodIngredientQuantity = new FoodIngredientQuantity(newFood, newIngredient,
                    entry.getQuantity());
            foodIngredientQuantity.add(newFoodIngredientQuantity);
        }

        newFood.setFoodIngredientQuantity(foodIngredientQuantity);

        newFood.setRestaurant(restaurant);
        return foodRepo.saveAndFlush(newFood);
    }

    /**
     * Gets a food of the restaurant
     * 
     * @param restaurantId
     * @param foodId
     * @return
     */
    public Food getFood(Long restaurantId, Long foodId) {
        Optional<Food> foodOpt = foodRepo.findById(foodId);
        Food food = foodOpt.orElseThrow(() -> new NotFoundException("Food not found"));
        if (food.getRestaurant().getRestaurantId().longValue() != restaurantId) {
            throw new NotFoundException("Food found but in incorrect restaurant");
        }
        return food;
    }

    /**
     * Deletes the food of a given restaurant id
     * 
     * @param restaurantId
     * @param foodId
     */
    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public void deleteFood(Long restaurantId, Long foodId) {
        Food food = foodRepo.findByFoodIdAndRestaurantRestaurantId(foodId, restaurantId)
                .orElseThrow(() -> new NotFoundException("Food not found"));
        foodRepo.delete(food);
    }

    /**
     * Updates the food of a given restaurant, ignores fields that are null
     * 
     * @param restaurantId
     * @param foodId
     * @param updatedFood
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public Food editFood(Long restaurantId, Long foodId, EditFoodDTO foodDTO) {
        final Food originalFood = foodRepo.findById(foodId)
                .orElseThrow(() -> new NotFoundException("Food requested could not be found"));
        if (originalFood.getRestaurant().getRestaurantId().longValue() != restaurantId) {
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

            for (FoodIngredientQuantityDTO dto : foodIngredientQuantityDTOs) {
                FoodIngredientQuantityKey key = new FoodIngredientQuantityKey(foodId, dto.getIngredientId());

                FoodIngredientQuantity fiq = foodIngredientQuantityRepo.findById(key).orElseGet(() -> 
                    new FoodIngredientQuantity(originalFood, ingredientRepo.getById(dto.getIngredientId()),
                            dto.getQuantity())
                );
                fiq.setQuantity(dto.getQuantity());

                foodIngredientQuantities.add(fiq);
            }
            originalFood.setFoodIngredientQuantity(foodIngredientQuantities);
        }
        return foodRepo.saveAndFlush(originalFood);
    }

    /**
     * Searches for a given food
     * 
     * @param page
     * @param query
     * @return
     */
    public Page<Food> searchFood(Pageable page, String query) {
        return foodRepo.findByFoodNameContains(page, query);
    }

    /**
     * Calculate food between dates
     * 
     * @param restaurant
     * @param startDate
     * @param endDate
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_MANAGER', 'FP_ADMIN')")
    public Map<String, Integer> calculateFoodNeededBetween(Restaurant restaurant, LocalDate startDate,
            LocalDate endDate) {

        HashMap<String, Integer> map = new HashMap<>();
        LocalDateTime start = startDate.minusDays(1).atTime(0, 0);
        LocalDateTime end = endDate.plusDays(1).atTime(0, 0);
        List<Reservation> reservations = reservationRepo.findByRestaurantAndDateBetween(restaurant, start, end);

        for (Reservation reservation : reservations) {
            List<LineItem> lineItems = reservation.getLineItems();
            for (LineItem lineItem : lineItems) {
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
     * Creates discount to a given restaurant
     * 
     * @param restaurantId
     * @param discount
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public Discount createDiscount(Long restaurantId, Discount discount) {
        Restaurant restaurant = repo.findByRestaurantId(restaurantId);
        if (restaurant.getDiscount() != null) {
            throw new AlreadyExistsException("Discount already exist");
        }
        Discount newDiscount = new Discount();
        newDiscount.restaurant(restaurant).discountDescription(discount.getDiscountDescription())
                .discountPercentage(discount.getDiscountPercentage());
        var savedDiscount = discountRepo.saveAndFlush(newDiscount);
        restaurant.setDiscount(savedDiscount);
        return savedDiscount;
    }

    /**
     * Deletes discount of a given restaurant
     * 
     * @param restaurantId
     * @param discountId
     */
    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public void deleteDiscount(Long restaurantId) {
        Restaurant res = repo.findByRestaurantId(restaurantId);
        if (res == null) {
            throw new NotFoundException("Restaurant not found");
        }
        Discount dis = res.getDiscount();
        if (dis == null) {
            throw new NotFoundException(DISCOUNT_NOT_FOUND_MESSAGE);
        }
        res.setDiscount(null);
        discountRepo.delete(dis);
    }

    /**
     * Updates the discount of a given restaurant, only change fields that are
     * changed
     * 
     * @param restaurantId
     * @param updatedDiscount
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public Discount updateDiscount(Long restaurantId, Discount updatedDiscount) {
        Restaurant res = repo.findByRestaurantId(restaurantId);
        Discount originalDiscount = res.getDiscount();
        if (originalDiscount == null) {
            throw new NotFoundException(DISCOUNT_NOT_FOUND_MESSAGE);
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
     * 
     * @param discountId
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_USER', 'FP_MANAGER', 'FP_ADMIN')")
    public Discount getDiscount(Long discountId) {
        Optional<Discount> discount = discountRepo.findById(discountId);
        return discount.orElseThrow(() -> new NotFoundException(DISCOUNT_NOT_FOUND_MESSAGE));
    }

    /*
     * Ingredient related methods
     */

    /**
     * Gets all the restaurant ingredients
     * 
     * @param restaurantId
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public List<Ingredient> getAllRestaurantIngredients(Long restaurantId) {
        Restaurant restaurant = get(restaurantId);
        if (restaurant == null) {
            throw new NotFoundException(RESTAURANT_DOES_NOT_EXIST_MESSAGE);
        }
        return restaurant.getIngredients();
    }

    /**
     * Gets all the restaurants ingredient
     * 
     * @param restaurantId
     * @param pageNumber
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public Page<Ingredient> getRestaurantIngredients(Long restaurantId, Integer pageNumber) {
        Restaurant restaurant = get(restaurantId);
        if (restaurant == null) {
            throw new NotFoundException(RESTAURANT_DOES_NOT_EXIST_MESSAGE);
        }
        Pageable pageReq = PageRequest.of(pageNumber, 8);
        return ingredientRepo.findByRestaurantRestaurantId(pageReq, restaurantId);
    }

    /**
     * Updates an ingredient with a given ID
     * 
     * @param restaurantId
     * @param ingredientId
     * @param updatedIngredient
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public Ingredient updateIngredient(Long restaurantId, Long ingredientId, Ingredient updatedIngredient) {

        Ingredient originalIngredient = ingredientRepo.findById(ingredientId)
                .orElseThrow(() -> new NotFoundException("Ingredient requested could not be found"));

        if (originalIngredient.getRestaurant().getRestaurantId().longValue() != restaurantId) {
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
     * 
     * @param restaurantId
     * @param newIngredient
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public Ingredient addRestaurantIngredient(Long restaurantId, Ingredient newIngredient) {
        Restaurant restaurant = get(restaurantId);
        if (restaurant == null) {
            throw new NotFoundException(RESTAURANT_DOES_NOT_EXIST_MESSAGE);
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
     * 
     * @param restaurantId
     * @param ingredientId
     */
    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public void deleteRestaurantIngredient(Long restaurantId, Long ingredientId) {

        Ingredient originalIngredient = ingredientRepo.findById(ingredientId)
                .orElseThrow(() -> new NotFoundException("Ingredient requested could not be found"));

        if (originalIngredient.getRestaurant().getRestaurantId().longValue() != restaurantId) {
            throw new NotFoundException("Ingredient requested could not be found at this restaurant");
        }

        ingredientRepo.delete(originalIngredient);
    }

    /**
     * Calculates the ingredients needed between 2 days
     * 
     * @param restaurant
     * @param startDate
     * @param endDate
     * @return
     */

    @PreAuthorize("hasAnyAuthority('FP_MANAGER', 'FP_ADMIN')")
    public Map<Ingredient, Integer> calculateIngredientsNeededBetween(Restaurant restaurant, LocalDate startDate,
            LocalDate endDate) {

        HashMap<Ingredient, Integer> map = new HashMap<>();
        LocalDateTime start = startDate.minusDays(1).atTime(0, 0);
        LocalDateTime end = endDate.plusDays(1).atTime(0, 0);
        List<Reservation> reservations = reservationRepo.findByRestaurantAndDateBetween(restaurant, start, end);

        for (Reservation reservation : reservations) {
            List<LineItem> lineItems = reservation.getLineItems();

            for (LineItem lineItem : lineItems) {
                Food food = lineItem.getFood();

                Set<FoodIngredientQuantity> foodIngreQuantity = food.getFoodIngredientQuantity();

                for (FoodIngredientQuantity entry : foodIngreQuantity) {
                    Ingredient currIngredient = entry.getIngredient();
                    Integer currQty = map.getOrDefault(currIngredient, 0);
                    map.put(currIngredient, currQty + entry.getQuantity() * lineItem.getQuantity());
                }

            }
        }

        return map;
    }

    /**
     * Sets picture of a given restaurant. If a picture currently exists, delete the
     * old picture and set the new one.
     * 
     * @param restaurantId
     * @param title
     * @param description
     * @param file
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public Picture savePicture(Long restaurantId, String title, String description, MultipartFile file) {
        Picture picture = pictureService.savePicture(title, description, file);
        Restaurant restaurant = get(restaurantId);
        if (restaurant.getPicture() != null) {
            deleteRestaurantPicture(restaurantId);
        }
        restaurant.setPicture(picture);
        repo.saveAndFlush(restaurant);
        return picture;
    }

    /**
     * Swets the picture of a given food. If a picture currently exists, delete the
     * old picture and set the new one.
     * 
     * @param restaurantId
     * @param foodId
     * @param title
     * @param description
     * @param file
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public Picture saveFoodPicture(Long restaurantId, Long foodId, String title, String description,
            MultipartFile file) {
        Picture picture = pictureService.savePicture(title, description, file);
        Food food = getFood(restaurantId, foodId);
        if (food.getPicture() != null) {
            deleteFoodPicture(restaurantId, foodId);
        }
        food.setPicture(picture);
        foodRepo.saveAndFlush(food);
        return picture;
    }

    public String getFoodPicture(Long restaurantId, Long foodId) {
        Food food = getFood(restaurantId, foodId);
        Picture picture = food.getPicture();
        if (picture == null) {
            throw new NotFoundException(PICTURE_NOT_FOUND_MESSAGE);
        }
        return pictureService.getPictureById(picture.getId());
    }

    public String getRestaurantPicture(Long restaurantId) {
        Restaurant restaurant = get(restaurantId);
        Picture picture = restaurant.getPicture();
        if (picture == null) {
            throw new NotFoundException(PICTURE_NOT_FOUND_MESSAGE);
        }
        return pictureService.getPictureById(picture.getId());
    }

    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public void deleteRestaurantPicture(Long restaurantId) {
        Restaurant restaurant = get(restaurantId);
        Picture picture = restaurant.getPicture();
        if (picture != null) {
            restaurant.setPicture(null);
            repo.saveAndFlush(restaurant);
            pictureService.deletePicture(picture.getId());
        } else {
            throw new NotFoundException(PICTURE_NOT_FOUND_MESSAGE);
        }
    }

    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public void deleteFoodPicture(Long restaurantId, Long foodId) {
        Food food = getFood(restaurantId, foodId);
        Picture picture = food.getPicture();
        if (picture != null) {
            food.setPicture(null);
            foodRepo.saveAndFlush(food);
            pictureService.deletePicture(picture.getId());
        } else {
            throw new NotFoundException(PICTURE_NOT_FOUND_MESSAGE);
        }
    }

    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public Picture updateRestaurantPicture(Long restaurantId, UpdatePictureDTO updatedPicture) {
        Restaurant restaurant = get(restaurantId);
        Picture currentPicture = restaurant.getPicture();
        if (currentPicture != null) {
            if (updatedPicture.getPictureFile() != null) {
                currentPicture = pictureService.savePicture(currentPicture.getTitle(), currentPicture.getDescription(),
                        updatedPicture.getPictureFile());
                deleteRestaurantPicture(restaurantId);
            }
            if (updatedPicture.getTitle() != null && !updatedPicture.getTitle().isEmpty()) {
                currentPicture.setTitle(updatedPicture.getTitle());
            }
            if (updatedPicture.getDescription() != null && !updatedPicture.getDescription().isEmpty()) {
                currentPicture.setDescription(updatedPicture.getDescription());
            }

            restaurant.setPicture(currentPicture);
            repo.saveAndFlush(restaurant);
            return pictureService.updatedPicture(currentPicture.getId(), currentPicture);
        } else {
            throw new NotFoundException(PICTURE_NOT_FOUND_MESSAGE);
        }
    }

    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public Picture updateFoodPicture(Long restaurantId, Long foodId, UpdatePictureDTO updatedPicture) {
        Food food = getFood(restaurantId, foodId);
        Picture currentPicture = food.getPicture();
        if (currentPicture != null) {
            if (updatedPicture.getPictureFile() != null) {
                currentPicture = pictureService.savePicture(currentPicture.getTitle(), currentPicture.getDescription(),
                        updatedPicture.getPictureFile());
                deleteFoodPicture(restaurantId, foodId);
            }
            if (updatedPicture.getTitle() != null && !updatedPicture.getTitle().isEmpty()) {
                currentPicture.setTitle(updatedPicture.getTitle());
            }
            if (updatedPicture.getDescription() != null && !updatedPicture.getDescription().isEmpty()) {
                currentPicture.setDescription(updatedPicture.getDescription());
            }
            food.setPicture(currentPicture);
            foodRepo.saveAndFlush(food);
            return pictureService.updatedPicture(currentPicture.getId(), currentPicture);
        } else {
            throw new NotFoundException(PICTURE_NOT_FOUND_MESSAGE);
        }
    }

}
