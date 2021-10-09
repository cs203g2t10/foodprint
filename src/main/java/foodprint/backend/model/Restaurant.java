package foodprint.backend.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
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

    @Column(name = "restaurantTableCapacity")
    @Schema(defaultValue="15")
    private Integer restaurantTableCapacity = 15;

    //what time the restaurant is open for reservation - assume 24 hour clock e.g "13:00" for 1pm -> values of 0- 23
    @Column(name = "restaurantWeekdayOpeningHour")
    @Schema(defaultValue ="9")
    private Integer restaurantWeekdayOpeningHour;

    //what time in minutes the restaurant is open for reservation -> should take in values of between 0 and 59 only
    @Column(name = "restaurantWeekdayOpeningMinutes")
    @Schema(defaultValue = "15")
    private Integer restaurantWeekdayOpeningMinutes;

    //what time reservation slots close
    @Column(name = "restaurantWeekdayClosingHour")
    @Schema(defaultValue = "22")
    private Integer restaurantWeekdayClosingHour;

    @Column(name = "restaurantWeekdayClosingMinutes")
    private Integer restaurantWeekdayClosingMinutes;

    @Schema(defaultValue = "11")
    @Column (name = "restaurantWeekendOpeningHour")
    private Integer restaurantWeekendOpeningHour;

    @Column (name = "restaurantWeekendOpeningMinutes")
    private Integer restaurantWeekendOpeningMinutes;

    @Schema(defaultValue = "22")
    @Column (name = "restaurantWeekendClosingHour")
    private Integer restaurantWeekendClosingHour;

    @Column (name = "restaurantWeekendClosingMinutes")
    private Integer restaurantWeekendClosingMinutes;

    @ElementCollection
    @CollectionTable(name = "categories", joinColumns = @JoinColumn(name = "restaurantId"))
    @Column(name = "restaurantCategory")
    private List<String> restaurantCategory = new ArrayList<String>();

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

    // Mutators and Accessors
    public Long getRestaurantId() {
        return this.restaurantId;
    }

    public String getRestaurantName() {
        return this.restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public List<Food> getAllFood() {
        return food;
    }

    public void setAllFood(List<Food> food) {
        this.food = food;
    }

    public String getRestaurantDesc() {
        return this.restaurantDesc;
    }

    public void setRestaurantDesc(String restaurantDesc) {
        this.restaurantDesc = restaurantDesc;
    }

    public List<Picture> getPictures() {
        return pictures;
    }

    public void setPictures(List<Picture> pictures) {
        this.pictures = pictures;
    }

    public List<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public String getRestaurantLocation() {
        return this.restaurantLocation;
    }

    public void setRestaurantLocation(String restaurantLocation) {
        this.restaurantLocation = restaurantLocation;
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
    
    public List<Discount> getDiscount() {
        return discount;
    }

    public void setDiscount(List<Discount> discount) {
        this.discount = discount;
    }

    public List<Reservation> getAllReservations() {
        return reservations;
    }

    public void setAllReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }
}
