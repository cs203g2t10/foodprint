package foodprint.backend.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.transaction.annotation.EnableTransactionManagement;

import io.swagger.v3.oas.annotations.media.Schema;


@Entity
@Table
@EnableTransactionManagement
@JsonIgnoreProperties("food")

public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurantId")
    @Schema(defaultValue="1")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long restaurantId;

    @Column(name = "restaurantName")
    @Schema(defaultValue="Sushi Tei")
    @NotEmpty
    private String restaurantName;

    @Column(name = "restaurantDesc")
    @Schema(defaultValue="Japanese restaurant")
    private String restaurantDesc;

    @Column(name = "restaurantLocation")
    @Schema(defaultValue="Serangoon")
    private String restaurantLocation;

    @Column(name = "priceRange")
    @Schema(defaultValue = "3")
    @Min(1)
    @Max(5)
    private Integer restaurantPriceRange;

    @Column(name = "restaurantTableCapacity")
    @Schema(defaultValue="15")
    @Min(0)
    @Max(1000)
    private Integer restaurantTableCapacity = 15;

    //what time the restaurant is open for reservation - assume 24 hour clock e.g "13:00" for 1pm -> values of 0- 23
    @Column(name = "restaurantWeekdayOpeningHour")
    @Schema(defaultValue ="9")
    @Min(0)
    @Max(23)
    private Integer restaurantWeekdayOpeningHour;

    //what time in minutes the restaurant is open for reservation -> should take in values of between 0 and 59 only
    @Column(name = "restaurantWeekdayOpeningMinutes")
    @Schema(defaultValue = "15")
    @Min(0)
    @Max(59)
    private Integer restaurantWeekdayOpeningMinutes;

    //what time reservation slots close
    @Column(name = "restaurantWeekdayClosingHour")
    @Schema(defaultValue = "22")
    @Min(0)
    @Max(23)
    private Integer restaurantWeekdayClosingHour;

    @Column(name = "restaurantWeekdayClosingMinutes")
    @Min(0)
    @Max(59)
    private Integer restaurantWeekdayClosingMinutes;

    @Schema(defaultValue = "11")
    @Column (name = "restaurantWeekendOpeningHour")
    @Min(0)
    @Max(23)
    private Integer restaurantWeekendOpeningHour;

    @Column (name = "restaurantWeekendOpeningMinutes")
    @Min(0)
    @Max(59)
    private Integer restaurantWeekendOpeningMinutes;

    @Schema(defaultValue = "22")
    @Column (name = "restaurantWeekendClosingHour")
    @Min(0)
    @Max(23)
    private Integer restaurantWeekendClosingHour;

    @Column (name = "restaurantWeekendClosingMinutes")
    @Min(0)
    @Max(59)
    private Integer restaurantWeekendClosingMinutes;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Food> food = new ArrayList<Food>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.MERGE)
    private List<RestaurantManager> restaurantManagers;

    @JsonIgnore
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Discount> discount = new ArrayList<Discount>();

    @JsonIgnore
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Reservation> reservations;
    
    @JsonIgnore
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Ingredient> ingredients;

    @OneToMany
    private List<Picture> pictures;

    protected Restaurant () { }

    public Restaurant (String restaurantName, String restaurantLocation) {
        this.restaurantName = restaurantName;
        this.restaurantLocation = restaurantLocation;
    }

    public Restaurant(String restaurantName, String restaurantDesc, String restaurantLocation, Integer restaurantTableCapacity, Integer restaurantWeekdayOpeningHour, Integer restaurantWeekdayOpeningMinutes, Integer restaurantWeekdayClosingHour, Integer restaurantWeekdayClosingMinutes, Integer restaurantWeekendOpeningHour, Integer restaurantWeekendOpeningMinutes, Integer restaurantWeekendClosingHour, Integer restaurantWeekendClosingMinutes) {
        this.restaurantName = restaurantName;
        this.restaurantDesc = restaurantDesc;
        this.restaurantLocation = restaurantLocation;
        this.restaurantTableCapacity = restaurantTableCapacity;
        this.restaurantWeekdayOpeningHour = restaurantWeekdayOpeningHour;
        this.restaurantWeekdayOpeningMinutes = restaurantWeekdayOpeningMinutes;
        this.restaurantWeekdayClosingHour = restaurantWeekdayClosingHour;
        this.restaurantWeekdayClosingMinutes = restaurantWeekdayClosingMinutes;
        this.restaurantWeekendOpeningHour = restaurantWeekendOpeningHour;
        this.restaurantWeekendOpeningMinutes = restaurantWeekendOpeningMinutes;
        this.restaurantWeekendClosingHour = restaurantWeekendClosingHour;
        this.restaurantWeekendClosingMinutes = restaurantWeekendClosingMinutes;
    }


    public Restaurant(Long restaurantId, String restaurantName, String restaurantDesc, String restaurantLocation, Integer restaurantPriceRange, Integer restaurantTableCapacity, Integer restaurantWeekdayOpeningHour, Integer restaurantWeekdayOpeningMinutes, Integer restaurantWeekdayClosingHour, Integer restaurantWeekdayClosingMinutes, Integer restaurantWeekendOpeningHour, Integer restaurantWeekendOpeningMinutes, Integer restaurantWeekendClosingHour, Integer restaurantWeekendClosingMinutes, List<Food> food, List<RestaurantManager> restaurantManagers, List<Discount> discount, List<Reservation> reservations, List<Ingredient> ingredients, List<Picture> pictures) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.restaurantDesc = restaurantDesc;
        this.restaurantLocation = restaurantLocation;
        this.restaurantPriceRange = restaurantPriceRange;
        this.restaurantTableCapacity = restaurantTableCapacity;
        this.restaurantWeekdayOpeningHour = restaurantWeekdayOpeningHour;
        this.restaurantWeekdayOpeningMinutes = restaurantWeekdayOpeningMinutes;
        this.restaurantWeekdayClosingHour = restaurantWeekdayClosingHour;
        this.restaurantWeekdayClosingMinutes = restaurantWeekdayClosingMinutes;
        this.restaurantWeekendOpeningHour = restaurantWeekendOpeningHour;
        this.restaurantWeekendOpeningMinutes = restaurantWeekendOpeningMinutes;
        this.restaurantWeekendClosingHour = restaurantWeekendClosingHour;
        this.restaurantWeekendClosingMinutes = restaurantWeekendClosingMinutes;
        this.food = food;
        this.restaurantManagers = restaurantManagers;
        this.discount = discount;
        this.reservations = reservations;
        this.ingredients = ingredients;
        this.pictures = pictures;
    }

    public Long getRestaurantId() {
        return this.restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getRestaurantName() {
        return this.restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantDesc() {
        return this.restaurantDesc;
    }

    public void setRestaurantDesc(String restaurantDesc) {
        this.restaurantDesc = restaurantDesc;
    }

    public String getRestaurantLocation() {
        return this.restaurantLocation;
    }

    public void setRestaurantLocation(String restaurantLocation) {
        this.restaurantLocation = restaurantLocation;
    }

    public Integer getRestaurantPriceRange() {
        return this.restaurantPriceRange;
    }

    public void setRestaurantPriceRange(Integer restaurantPriceRange) {
        this.restaurantPriceRange = restaurantPriceRange;
    }

    public Integer getRestaurantTableCapacity() {
        return this.restaurantTableCapacity;
    }

    public void setRestaurantTableCapacity(Integer restaurantTableCapacity) {
        this.restaurantTableCapacity = restaurantTableCapacity;
    }

    public Integer getRestaurantWeekdayOpeningHour() {
        return this.restaurantWeekdayOpeningHour;
    }

    public void setRestaurantWeekdayOpeningHour(Integer restaurantWeekdayOpeningHour) {
        this.restaurantWeekdayOpeningHour = restaurantWeekdayOpeningHour;
    }

    public Integer getRestaurantWeekdayOpeningMinutes() {
        return this.restaurantWeekdayOpeningMinutes;
    }

    public void setRestaurantWeekdayOpeningMinutes(Integer restaurantWeekdayOpeningMinutes) {
        this.restaurantWeekdayOpeningMinutes = restaurantWeekdayOpeningMinutes;
    }

    public Integer getRestaurantWeekdayClosingHour() {
        return this.restaurantWeekdayClosingHour;
    }

    public void setRestaurantWeekdayClosingHour(Integer restaurantWeekdayClosingHour) {
        this.restaurantWeekdayClosingHour = restaurantWeekdayClosingHour;
    }

    public Integer getRestaurantWeekdayClosingMinutes() {
        return this.restaurantWeekdayClosingMinutes;
    }

    public void setRestaurantWeekdayClosingMinutes(Integer restaurantWeekdayClosingMinutes) {
        this.restaurantWeekdayClosingMinutes = restaurantWeekdayClosingMinutes;
    }

    public Integer getRestaurantWeekendOpeningHour() {
        return this.restaurantWeekendOpeningHour;
    }

    public void setRestaurantWeekendOpeningHour(Integer restaurantWeekendOpeningHour) {
        this.restaurantWeekendOpeningHour = restaurantWeekendOpeningHour;
    }

    public Integer getRestaurantWeekendOpeningMinutes() {
        return this.restaurantWeekendOpeningMinutes;
    }

    public void setRestaurantWeekendOpeningMinutes(Integer restaurantWeekendOpeningMinutes) {
        this.restaurantWeekendOpeningMinutes = restaurantWeekendOpeningMinutes;
    }

    public Integer getRestaurantWeekendClosingHour() {
        return this.restaurantWeekendClosingHour;
    }

    public void setRestaurantWeekendClosingHour(Integer restaurantWeekendClosingHour) {
        this.restaurantWeekendClosingHour = restaurantWeekendClosingHour;
    }

    public Integer getRestaurantWeekendClosingMinutes() {
        return this.restaurantWeekendClosingMinutes;
    }

    public void setRestaurantWeekendClosingMinutes(Integer restaurantWeekendClosingMinutes) {
        this.restaurantWeekendClosingMinutes = restaurantWeekendClosingMinutes;
    }

    public List<Food> getAllFood() {
        return this.food;
    }

    public void setAllFood(List<Food> food) {
        this.food = food;
    }

    public List<RestaurantManager> getRestaurantManagers() {
        return this.restaurantManagers;
    }

    public void setRestaurantManagers(List<RestaurantManager> restaurantManagers) {
        this.restaurantManagers = restaurantManagers;
    }

    public List<Discount> getDiscount() {
        return this.discount;
    }

    public void setDiscount(List<Discount> discount) {
        this.discount = discount;
    }

    public List<Reservation> getReservations() {
        return this.reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public List<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public List<Picture> getPictures() {
        return this.pictures;
    }

    public void setPictures(List<Picture> pictures) {
        this.pictures = pictures;
    }

    public Restaurant restaurantId(Long restaurantId) {
        setRestaurantId(restaurantId);
        return this;
    }

    public Restaurant restaurantName(String restaurantName) {
        setRestaurantName(restaurantName);
        return this;
    }

    public Restaurant restaurantDesc(String restaurantDesc) {
        setRestaurantDesc(restaurantDesc);
        return this;
    }

    public Restaurant restaurantLocation(String restaurantLocation) {
        setRestaurantLocation(restaurantLocation);
        return this;
    }

    public Restaurant restaurantPriceRange(Integer restaurantPriceRange) {
        setRestaurantPriceRange(restaurantPriceRange);
        return this;
    }

    public Restaurant restaurantTableCapacity(Integer restaurantTableCapacity) {
        setRestaurantTableCapacity(restaurantTableCapacity);
        return this;
    }

    public Restaurant restaurantWeekdayOpeningHour(Integer restaurantWeekdayOpeningHour) {
        setRestaurantWeekdayOpeningHour(restaurantWeekdayOpeningHour);
        return this;
    }

    public Restaurant restaurantWeekdayOpeningMinutes(Integer restaurantWeekdayOpeningMinutes) {
        setRestaurantWeekdayOpeningMinutes(restaurantWeekdayOpeningMinutes);
        return this;
    }

    public Restaurant restaurantWeekdayClosingHour(Integer restaurantWeekdayClosingHour) {
        setRestaurantWeekdayClosingHour(restaurantWeekdayClosingHour);
        return this;
    }

    public Restaurant restaurantWeekdayClosingMinutes(Integer restaurantWeekdayClosingMinutes) {
        setRestaurantWeekdayClosingMinutes(restaurantWeekdayClosingMinutes);
        return this;
    }

    public Restaurant restaurantWeekendOpeningHour(Integer restaurantWeekendOpeningHour) {
        setRestaurantWeekendOpeningHour(restaurantWeekendOpeningHour);
        return this;
    }

    public Restaurant restaurantWeekendOpeningMinutes(Integer restaurantWeekendOpeningMinutes) {
        setRestaurantWeekendOpeningMinutes(restaurantWeekendOpeningMinutes);
        return this;
    }

    public Restaurant restaurantWeekendClosingHour(Integer restaurantWeekendClosingHour) {
        setRestaurantWeekendClosingHour(restaurantWeekendClosingHour);
        return this;
    }

    public Restaurant restaurantWeekendClosingMinutes(Integer restaurantWeekendClosingMinutes) {
        setRestaurantWeekendClosingMinutes(restaurantWeekendClosingMinutes);
        return this;
    }

    public Restaurant food(List<Food> food) {
        setAllFood(food);
        return this;
    }

    public Restaurant restaurantManagers(List<RestaurantManager> restaurantManagers) {
        setRestaurantManagers(restaurantManagers);
        return this;
    }

    public Restaurant discount(List<Discount> discount) {
        setDiscount(discount);
        return this;
    }

    public Restaurant reservations(List<Reservation> reservations) {
        setReservations(reservations);
        return this;
    }

    public Restaurant ingredients(List<Ingredient> ingredients) {
        setIngredients(ingredients);
        return this;
    }

    public Restaurant pictures(List<Picture> pictures) {
        setPictures(pictures);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Restaurant)) {
            return false;
        }
        Restaurant restaurant = (Restaurant) o;
        return Objects.equals(restaurantId, restaurant.restaurantId) && Objects.equals(restaurantName, restaurant.restaurantName) && Objects.equals(restaurantDesc, restaurant.restaurantDesc) && Objects.equals(restaurantLocation, restaurant.restaurantLocation) && Objects.equals(restaurantPriceRange, restaurant.restaurantPriceRange) && Objects.equals(restaurantTableCapacity, restaurant.restaurantTableCapacity) && Objects.equals(restaurantWeekdayOpeningHour, restaurant.restaurantWeekdayOpeningHour) && Objects.equals(restaurantWeekdayOpeningMinutes, restaurant.restaurantWeekdayOpeningMinutes) && Objects.equals(restaurantWeekdayClosingHour, restaurant.restaurantWeekdayClosingHour) && Objects.equals(restaurantWeekdayClosingMinutes, restaurant.restaurantWeekdayClosingMinutes) && Objects.equals(restaurantWeekendOpeningHour, restaurant.restaurantWeekendOpeningHour) && Objects.equals(restaurantWeekendOpeningMinutes, restaurant.restaurantWeekendOpeningMinutes) && Objects.equals(restaurantWeekendClosingHour, restaurant.restaurantWeekendClosingHour) && Objects.equals(restaurantWeekendClosingMinutes, restaurant.restaurantWeekendClosingMinutes) && Objects.equals(food, restaurant.food) && Objects.equals(restaurantManagers, restaurant.restaurantManagers) && Objects.equals(discount, restaurant.discount) && Objects.equals(reservations, restaurant.reservations) && Objects.equals(ingredients, restaurant.ingredients) && Objects.equals(pictures, restaurant.pictures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(restaurantId, restaurantName, restaurantDesc, restaurantLocation, restaurantPriceRange, restaurantTableCapacity, restaurantWeekdayOpeningHour, restaurantWeekdayOpeningMinutes, restaurantWeekdayClosingHour, restaurantWeekdayClosingMinutes, restaurantWeekendOpeningHour, restaurantWeekendOpeningMinutes, restaurantWeekendClosingHour, restaurantWeekendClosingMinutes, food, restaurantManagers, discount, reservations, ingredients, pictures);
    }

    @Override
    public String toString() {
        return "{" +
            " restaurantId='" + getRestaurantId() + "'" +
            ", restaurantName='" + getRestaurantName() + "'" +
            ", restaurantDesc='" + getRestaurantDesc() + "'" +
            ", restaurantLocation='" + getRestaurantLocation() + "'" +
            ", restaurantPriceRange='" + getRestaurantPriceRange() + "'" +
            ", restaurantTableCapacity='" + getRestaurantTableCapacity() + "'" +
            ", restaurantWeekdayOpeningHour='" + getRestaurantWeekdayOpeningHour() + "'" +
            ", restaurantWeekdayOpeningMinutes='" + getRestaurantWeekdayOpeningMinutes() + "'" +
            ", restaurantWeekdayClosingHour='" + getRestaurantWeekdayClosingHour() + "'" +
            ", restaurantWeekdayClosingMinutes='" + getRestaurantWeekdayClosingMinutes() + "'" +
            ", restaurantWeekendOpeningHour='" + getRestaurantWeekendOpeningHour() + "'" +
            ", restaurantWeekendOpeningMinutes='" + getRestaurantWeekendOpeningMinutes() + "'" +
            ", restaurantWeekendClosingHour='" + getRestaurantWeekendClosingHour() + "'" +
            ", restaurantWeekendClosingMinutes='" + getRestaurantWeekendClosingMinutes() + "'" +
            ", food='" + getAllFood() + "'" +
            ", restaurantManagers='" + getRestaurantManagers() + "'" +
            ", discount='" + getDiscount() + "'" +
            ", reservations='" + getReservations() + "'" +
            ", ingredients='" + getIngredients() + "'" +
            ", pictures='" + getPictures() + "'" +
            "}";
    }

}
