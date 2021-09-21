package foodprint.backend.model;


import java.util.Arrays;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

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

    @Column(name = "picturesPath")
    private String picturesPath;

    @Column (name = "restaurantLocation")
    @Schema(defaultValue="Serangoon")
    private String restaurantLocation;

    @Column (name = "restaurantTableCapacity")
    @Schema(defaultValue="15")
    private Integer restaurantTableCapacity = 15;

    //what time the restaurant is open for reservation - assume 24 hour clock e.g "13:00" for 1pm -> values of 0- 23
    @Column (name = "restaurantWeekdayOpeningHour")
    @Schema(defaultValue ="9")
    private Integer restaurantWeekdayOpeningHour;

    //what time in minutes the restaurant is open for reservation -> should take in values of between 0 and 59 only
    @Column (name = "restaurantWeekdayOpeningMinutes")
    @Schema(defaultValue = "15")
    private Integer restaurantWeekdayOpeningMinutes;

    //what time reservation slots close
    @Column (name = "restaurantWeekdayClosingHour")
    @Schema(defaultValue = "22")
    private Integer restaurantWeekdayClosingHour;

    @Column (name = "restaurantWeekdayClosingMinutes")
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

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.MERGE)
    private List<Food> food;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.MERGE)
    private List<RestaurantManager> restaurantManagers;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.MERGE)
    private List<Discount> discount;

    protected Restaurant () { }

    public Restaurant (String restaurantName, String restaurantLocation) {
        this.restaurantName = restaurantName;
        this.restaurantLocation = restaurantLocation;
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

    public List<String> getPicturesPath() {
        if (picturesPath == null) {
            return null;
        }

        String[] arr = picturesPath.split(",");
        List<String> list = Arrays.asList(arr);
        return list;
    }

    public void setPicturesPath(List<String> list) {
        if (!list.isEmpty()) {
            String picturesPath = String.join(",", list);
            this.picturesPath = picturesPath;
        }
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

    
    public List<Discount> getAllDiscount() {
        return discount;
    }

    public void setAllDiscount(List<Discount> discount) {
        this.discount = discount;
    }
}
