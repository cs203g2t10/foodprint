package foodprint.backend.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Length;

import org.springframework.transaction.annotation.EnableTransactionManagement;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@JsonIgnoreProperties("restaurant")
@EnableTransactionManagement
public class Discount implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discountId")
    @Schema(defaultValue="1")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long discountId;

    @OneToOne
    @JoinColumn(name = "restaurantId")
    @Schema(defaultValue="1")
    private Restaurant restaurant;

    @Column(name = "discountDescription")
    @Schema(defaultValue="1for1")
    @NotEmpty
    @Length(min = 1)
    private String discountDescription;

    @Column(name = "discountPercentage")
    @Schema(defaultValue="50")
    private Integer discountPercentage;

    public Discount() {}

    public Discount(String discountDescription, Integer discountPercentage) {
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
