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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.springframework.transaction.annotation.EnableTransactionManagement;


@Entity
@Table
@EnableTransactionManagement
@JsonIgnoreProperties("food")

public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurantId")
    private Integer restaurantId;

    @Column(name = "restaurantName")
    private String restaurantName;

    @Column(name = "restaurantDesc")
    private String restaurantDesc;

    @Column(name = "picturesPath")
    private String picturesPath;

    @Column (name = "restaurantLocation")
    private String restaurantLocation;

    @Column (name = "restaurantTableCapacity")
    private Integer restaurantTableCapacity = 15;

    //what time the restaurant is open for reservation
    @Column (name = "restaurantWeekdayOpening")
    private String restaurantWeekdayOpening;

    //what time reservation slots close
    @Column (name = "restaurantWeekdayClosing")
    private String restaurantWeekdayClosing;

    @Column (name = "restaurantWeekendOpening")
    private String restaurantWeekendOpening;

    @Column (name = "restaurantWeekendClosing")
    private String restaurantWeekendClosing;


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
    public Integer getRestaurantId() {
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
    
}
