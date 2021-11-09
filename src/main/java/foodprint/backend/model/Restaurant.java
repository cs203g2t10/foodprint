package foodprint.backend.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.validator.constraints.Length;
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
    @Length(min = 1, max = 40)
    private String restaurantName;

    @Column(name = "restaurantDesc")
    @Schema(defaultValue="Japanese restaurant")
    @NotEmpty
    @Length(min = 1)
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

    @OneToOne(optional = true, cascade = CascadeType.ALL)/* (mappedBy = "restaurant") */
    private Picture picture;

    @ElementCollection
    @CollectionTable(name = "categories", joinColumns = @JoinColumn(name = "restaurantId"))
    @Column(name = "restaurantCategory")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<@NotEmpty @Length(min=1) String> restaurantCategory = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Food> food = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.MERGE)
    @JsonIgnore
    private List<User> restaurantManagers;

    @JsonIgnore
    @OneToOne(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private Discount discount;

    @JsonIgnore
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Reservation> reservations;
    
    @JsonIgnore
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Ingredient> ingredients;

   
    protected Restaurant () { }

    public Restaurant (String restaurantName, String restaurantLocation) {
        this.restaurantName = restaurantName;
        this.restaurantLocation = restaurantLocation;
    }


    public Restaurant(String restaurantName, String restaurantDesc, String restaurantLocation, Integer restaurantTableCapacity, Integer restaurantWeekdayOpeningHour, Integer restaurantWeekdayOpeningMinutes, Integer restaurantWeekdayClosingHour, Integer restaurantWeekdayClosingMinutes, Integer restaurantWeekendOpeningHour, Integer restaurantWeekendOpeningMinutes, Integer restaurantWeekendClosingHour, Integer restaurantWeekendClosingMinutes, List<String> restaurantCategory) {
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
        this.restaurantCategory = restaurantCategory;
    }


    public Restaurant(Long restaurantId, String restaurantName, String restaurantDesc, String restaurantLocation, Integer restaurantPriceRange, Integer restaurantTableCapacity, Integer restaurantWeekdayOpeningHour, Integer restaurantWeekdayOpeningMinutes, Integer restaurantWeekdayClosingHour, Integer restaurantWeekdayClosingMinutes, Integer restaurantWeekendOpeningHour, Integer restaurantWeekendOpeningMinutes, Integer restaurantWeekendClosingHour, Integer restaurantWeekendClosingMinutes, List<Food> food, List<User> restaurantManagers, Discount discount, List<Reservation> reservations, List<Ingredient> ingredients, Picture picture) {
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
        this.picture = picture;
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

    public List<String> getRestaurantCategory() {
        return this.restaurantCategory;
    }

    public void setRestaurantCategory(List<String> restaurantCategory) {
        this.restaurantCategory = restaurantCategory;
    }
    
    public List<Food> getAllFood() {
        return this.food;
    }

    public void setAllFood(List<Food> food) {
        this.food = food;
    }

    public List<User> getRestaurantManagers() {
        return this.restaurantManagers;
    }

    public void setRestaurantManagers(List<User> restaurantManagers) {
        this.restaurantManagers = restaurantManagers;
    }

    public Discount getDiscount() {
        return this.discount;
    }

    public void setDiscount(Discount discount) {
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

    public Picture getPicture() {
        return this.picture;
    }

    public void setPicture(Picture picture) {
        this.picture = picture;
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

    public Restaurant restaurantManagers(List<User> restaurantManagers) {
        setRestaurantManagers(restaurantManagers);
        return this;
    }

    public Restaurant discount(Discount discount) {
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

    public Restaurant picture(Picture picture) {
        setPicture(picture);
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
        return Objects.equals(restaurantId, restaurant.restaurantId) && Objects.equals(restaurantName, restaurant.restaurantName) && Objects.equals(restaurantDesc, restaurant.restaurantDesc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(restaurantId, restaurantName, restaurantDesc);
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
            ", picture='" + getPicture() + "'" +
            "}";
    }

}
