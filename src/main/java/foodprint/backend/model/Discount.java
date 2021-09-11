package foodprint.backend.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discountId")
    private Integer discountId;

    @ManyToOne(cascade=CascadeType.MERGE)
    @JoinColumn(name = "restaurant")
    private Restaurant restaurant;

    @Column(name = "discountDesciption")
    @Schema(defaultValue="1for1")
    private String discountDescription;

    @Column(name = "discountPercentage")
    @Schema(defaultValue="50%")
    private Integer discountPercentage;

    public Discount() {}

    public Discount(Integer discountId, Restaurant restaurant, String discountDescription, Integer discountPercentage) {
        this.discountId = discountId;
        this.restaurant = restaurant;
        this.discountDescription = discountDescription;
        this.discountPercentage = discountPercentage;
    }
    
    public Integer getDiscountId() {
        return this.discountId;
    }

    public Restaurant getRestaurant() {
        return this.restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public String getDiscountDescription() {
        return this.discountDescription;
    }

    public void setDiscountDescription(String discountDescription) {
        this.discountDescription = discountDescription;
    }

    public Integer getDiscountPercentage() {
        return this.discountPercentage;
    }

    public void setDiscountPercentage(Integer discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
    
}
