package foodprint.backend.model;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.transaction.annotation.EnableTransactionManagement;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table
@EnableTransactionManagement
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "foodId")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long foodId;

    @Column(name = "foodName")
    @Schema(defaultValue="Sashimi")
    private String foodName;

    @ManyToOne
    @JoinColumn(name="restaurantId")
    @JsonIgnore
    private Restaurant restaurant;

    @Column(name = "foodDesc")
    @Schema(defaultValue = "Salmon slices")
    private String foodDesc;

    @Column(name = "picturesPath")
    private String picturesPath;

    @Column(name = "foodprice")
    @Schema(defaultValue = "10")
    private Double foodPrice;

    @Column(name = "foodDiscount")
    private Double foodDiscount;

    @OneToMany(mappedBy = "food")
    private Set<FoodIngredientQuantity> foodIngredientQuantity;

    @JsonIgnore
    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineItem> lineItems;

    public Food() {}

    public Food(String foodName, Double foodPrice, Double foodDiscount) {
        this.foodName = foodName;
        this.foodPrice = foodPrice;
        this.foodDiscount = foodDiscount;
    }

    public List<LineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public Long getFoodId() {
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

    public void setFoodIngredientQuantity(Set<FoodIngredientQuantity> foodIngredientQuantity) {
        this.foodIngredientQuantity = foodIngredientQuantity;
    }

    public Set<FoodIngredientQuantity> getFoodIngredientQuantity() {
        return this.foodIngredientQuantity;
    }

}
