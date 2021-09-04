package foodprint.backend.model;

import java.util.Arrays;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.transaction.annotation.EnableTransactionManagement;

@Entity
@Table
@EnableTransactionManagement
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "foodId")
    private Integer foodId;

    @Column(name = "foodName")
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
    private Integer foodDiscount;

    public Food() {}

    public Food(Integer foodId, String foodName, Double foodPrice, Integer foodDiscount) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.foodPrice = foodPrice;
        this.foodDiscount = foodDiscount;
    }

    public Integer getFoodId() {
        return this.foodId;
    }

    public void setFoodId(Integer foodId) {
        this.foodId = foodId;
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

    public Integer getFoodDiscount() {
        return this.foodDiscount;
    }

    public void setFoodDiscount(Integer foodDiscount) {
        this.foodDiscount = foodDiscount;
    }

}
