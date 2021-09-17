package foodprint.backend.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.springframework.transaction.annotation.EnableTransactionManagement;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@JsonIgnoreProperties("restaurant")
@EnableTransactionManagement
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discountId")
    @Schema(defaultValue="1")
    private Long discountId;

    @ManyToOne(cascade=CascadeType.MERGE)
    @JoinColumn(name = "restaurantId")
    @Schema(defaultValue="1")
    private Restaurant restaurant;

    @Column(name = "discountDescription")
    @Schema(defaultValue="1for1")
    private String discountDescription;

    @Column(name = "discountPercentage")
    @Schema(defaultValue="50")
    private Integer discountPercentage;

    public Discount() {}

    public Discount(Long discountId, String discountDescription, Integer discountPercentage) {
        this.discountId = discountId;
        this.discountDescription = discountDescription;
        this.discountPercentage = discountPercentage;
    }
    
    public Long getDiscountId() {
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

    public Discount restaurant(Restaurant restaurant) {
        setRestaurant(restaurant);
        return this;
    }

    public Discount discountDescription(String discountDescription) {
        setDiscountDescription(discountDescription);
        return this;
    }

    public Discount discountPercentage(Integer discountPercentage) {
        setDiscountPercentage(discountPercentage);
        return this;
    }
    
}
