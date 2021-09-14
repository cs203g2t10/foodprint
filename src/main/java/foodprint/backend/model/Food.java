package foodprint.backend.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.springframework.transaction.annotation.EnableTransactionManagement;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table
@JsonIgnoreProperties("restaurant")
@EnableTransactionManagement
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "foodId")
    private Integer foodId;

    @Column(name = "foodName")
    @Schema(defaultValue="somefood")
    private String foodName;

    @ManyToOne(cascade=CascadeType.MERGE)
    @JoinColumn(name="restaurantId")
    private Restaurant restaurant;

    @Column(name = "foodDesc")
    private String foodDesc;

    @Column(name = "picturesPath")
    private String picturesPath;

    @Column(name = "foodprice")
    private Double foodPrice;

    @Column(name = "foodDiscount")
    private Double foodDiscount;

    @ManyToMany
    @JoinTable(name = "food_ingredients",
        joinColumns = @JoinColumn(name = "foodId"),
        inverseJoinColumns = @JoinColumn(name = "ingredientId"))
    private Set<Ingredient> ingredients = new HashSet<>();

    @OneToOne(mappedBy = "food", cascade = CascadeType.MERGE)
    private LineItem lineItem;

    public Food() {}

    public Food(String foodName, Double foodPrice, Double foodDiscount) {
        this.foodName = foodName;
        this.foodPrice = foodPrice;
        this.foodDiscount = foodDiscount;
    }

    public Integer getFoodId() {
        return this.foodId;
    }

    public String getFoodName() {
        return this.foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    
    public Restaurant getRestaurant() {
        return this.restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public String getFoodDesc() {
        return this.foodDesc;
    }

    public void setFoodDesc(String foodDesc) {
        this.foodDesc = foodDesc;
    }

    public List<String> getPicturesPath() {
        String[] arr = picturesPath.split(",");
        List<String> list = Arrays.asList(arr);
        return list;
    }

    public void setPicturesPath(List<String> list) {
        String picturesPath = String.join(",", list);
        this.picturesPath = picturesPath;
    }

    public Double getFoodPrice() {
        return this.foodPrice;
    }

    public void setFoodPrice(Double foodPrice) {
        this.foodPrice = foodPrice;
    }

    public Double getFoodDiscount() {
        return this.foodDiscount;
    }

    public void setFoodDiscount(Double foodDiscount) {
        this.foodDiscount = foodDiscount;
    }

}
