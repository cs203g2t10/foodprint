package foodprint.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import foodprint.backend.dto.DiscountDTO;
import foodprint.backend.exceptions.DeleteFailedException;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.model.Discount;
import foodprint.backend.model.DiscountRepo;
import foodprint.backend.model.Food;
import foodprint.backend.model.FoodRepo;
import foodprint.backend.model.Picture;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.RestaurantRepo;

@Service
public class RestaurantService {

    private RestaurantRepo repo;

    private FoodRepo foodRepo;

    private DiscountRepo discountRepo;

    private PictureService pictureService;
    
    
    @Autowired
    public RestaurantService(RestaurantRepo repo, FoodRepo foodRepo, DiscountRepo discountRepo, PictureService pictureService) {
        this.repo = repo;
        this.foodRepo = foodRepo;
        this.discountRepo = discountRepo;
        this.pictureService = pictureService;
    }
    
    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public List<Restaurant> getAllRestaurants() {
        List<Restaurant> restaurants = repo.findAll();
        return restaurants;
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public Restaurant get(Long id) {
        Optional<Restaurant> restaurant = repo.findById(id);
        return restaurant.orElseThrow(() -> new NotFoundException("Restaurant not found"));
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
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

        return repo.saveAndFlush(currentRestaurant);
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public Restaurant create(Restaurant restaurant) {
        return repo.saveAndFlush(restaurant);
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
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

    public Page<Restaurant> search(Pageable page, String query) {
        return repo.findByRestaurantNameContains(page, query);
    }

    /*
    *
    * Food related methods
    *
    */

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public Food addFood(Long restaurantId, Food food) {
        Restaurant restaurant = repo.findByRestaurantId(restaurantId);
        food.setRestaurant(restaurant);
        var savedFood = foodRepo.saveAndFlush(food);
        restaurant.getAllFood().add(savedFood);
        return savedFood;
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public List<Food> getAllFood(Long restaurantId) {
        Restaurant restaurant = repo.findByRestaurantId(restaurantId);
        return restaurant.getAllFood();
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public Food getFood(Long restaurantId, Long foodId) {
        Optional<Food> foodOpt = foodRepo.findById(foodId);
        Food food = foodOpt.orElseThrow(() -> new NotFoundException("Food not found"));
        if (food.getRestaurant().getRestaurantId() != restaurantId) {
            throw new NotFoundException("Food found but in incorrect restaurant");
        }
        return food;
    }

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

    public Food updateFood(Long restaurantId, Long foodId, Food updatedFood) {
        Restaurant restaurant = get(restaurantId);
        List<Food> allFood = restaurant.getAllFood();
        for (Food food : allFood) {
            if (food.getFoodId().equals(foodId)) {
                food.setFoodName(updatedFood.getFoodName());
                food.setFoodDesc(updatedFood.getFoodDesc());
                food.setFoodDiscount(updatedFood.getFoodDiscount());
                food.setFoodPrice(updatedFood.getFoodPrice());
                food = foodRepo.saveAndFlush(food);
                return food;
            }
        }
        throw new NotFoundException("Food not found");
    }
    
    public Page<Food> searchFood(Pageable page, String query) {
        return foodRepo.findByFoodNameContains(page,query);
    }
    /*
    *
    * Discount related methods
    *
    */

    // public List<Discount> getDiscounts(Long restaurantId) {
    //     Optional<Discount> discount = discountRepo.findById(discountId);
    //     return discount;
    // }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public Discount addDiscount(Long restaurantId, DiscountDTO discount) {
        Restaurant restaurant = repo.findByRestaurantId(restaurantId);
        Discount Discount = new Discount();
        Discount.restaurant(restaurant)
                     .discountDescription(discount.getDiscountDescription())
                     .discountPercentage(discount.getDiscountPercentage());
        var savedDiscount = discountRepo.saveAndFlush(Discount);
        restaurant.getDiscount().add(savedDiscount);
        return savedDiscount;
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public void deleteDiscount(Long restaurantId, Long discountId) {
        Restaurant restaurant = get(restaurantId);
        List<Discount> allDiscounts = restaurant.getDiscount();
        for (Discount discount : allDiscounts) {
            if (discount.getDiscountId().equals(discountId)) {
                discountRepo.delete(discount);
                return;
            }
        }
        throw new NotFoundException("Discount not found");
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public Discount updateDiscount(Long restaurantId, Long discountId, Discount discount) {
        Restaurant restaurant = get(restaurantId);
        List<Discount> allDiscounts = restaurant.getDiscount();
        for (Discount currentDiscount : allDiscounts) {
            if (currentDiscount.getDiscountId().equals(discountId)) {
                currentDiscount.setDiscountDescription(discount.getDiscountDescription());
                currentDiscount.setDiscountPercentage(discount.getDiscountPercentage());
                currentDiscount = discountRepo.saveAndFlush(currentDiscount);
                return currentDiscount;
            }
        }
        throw new NotFoundException("Discount not found");
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public Discount getDiscount(Long discountId) {
        Optional<Discount> discount = discountRepo.findById(discountId);
        return discount.orElseThrow(() -> new NotFoundException("Discount not found"));
    }

    public Picture savePicture(Long restaurantId, String title, String description, MultipartFile file) {
        Picture picture = pictureService.savePicture(title, description, file);
        Restaurant restaurant = get(restaurantId);
        List<Picture> picList = restaurant.getPictures();
        picList.add(picture);
        repo.saveAndFlush(restaurant);
        return picture;
    }

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

    public String getPictureById(Long restaurantId, Long pictureId) {
        if (!pictureInRestaurant(restaurantId, pictureId)) {
            throw new NotFoundException("Picture not found in restaurant");
        }
        return pictureService.getPictureById(pictureId);
    }

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

    public Picture updatePictureInformation(Long restaurantId, Long pictureId, Picture picture) {
        if (!pictureInRestaurant(restaurantId, pictureId)) { //check if pic id is in restaurant list
            throw new NotFoundException("Picture not found in restaurant");
        }
        return pictureService.updatedPicture(pictureId, picture);
    }
}
