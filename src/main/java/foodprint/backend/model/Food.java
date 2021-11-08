package foodprint.backend.model;

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.Length;
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
    @NotEmpty
    @Length(min = 1, max = 40)
    private String foodName;

    @ManyToOne
    @JoinColumn(name="restaurantId")
    @JsonIgnore
    private Restaurant restaurant;

    @Column(name = "foodDesc")
    @Schema(defaultValue = "Salmon slices")
    @NotEmpty
    @Length(min = 1)
    private String foodDesc;

    @Column(name = "foodprice")
    @Schema(defaultValue = "10")
    private Double foodPrice;

    @Column(name = "foodDiscount")
    private Double foodDiscount;

    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL)
    private Set<FoodIngredientQuantity> foodIngredientQuantity = new HashSet<FoodIngredientQuantity>();

    @JsonIgnore
    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineItem> lineItems;

    @OneToOne(optional = true)
    private Picture picture;

    public Food() {}

    public Food(String foodName, Double foodPrice, Double foodDiscount) {
        this.foodName = foodName;
        this.foodPrice = foodPrice;
        this.foodDiscount = foodDiscount;
    }

    public Food(String foodName, String foodDesc, Double foodPrice, Double foodDiscount) {
        this.foodName = foodName;
        this.foodDesc = foodDesc;
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
    
    public void setFoodId(Long foodId) {
        this.foodId = foodId;
    }

    public Picture getPicture() {
        return this.picture;
    }

    public void setPicture(Picture picture) {
        this.picture = picture;
    }
    

}
