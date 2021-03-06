package foodprint.backend.model;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.Length;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table
public class Ingredient implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ingredientId")
    @Schema(defaultValue="1")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long ingredientId;

    @Column(name = "ingredientName")
    @Schema(defaultValue = "Salmon")
    @NotEmpty(message = "Ingredient name should not be empty")
    @Length(min = 1, max = 40)
    private String ingredientName;

    @Column(name = "ingredientDesc")
    @Schema(defaultValue = "Sashimi grade salmon")
    @NotEmpty(message = "Ingredient description should not be empty")
    @Length(min = 1)
    private String ingredientDesc;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Column(name = "units")
    @Schema(defaultValue = "1")
    @NotEmpty(message = "Unit should not be empty")
    private String units;

    @OneToMany(mappedBy = "ingredient")
    private Set<FoodIngredientQuantity> foodIngredientQuantity;

    protected Ingredient () { }

    public Ingredient (String ingredientName) {
        this.ingredientName = ingredientName;
    }

    // Mutators and Accessors
    public Restaurant getRestaurant() {
        return this.restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public Long getIngredientId() {
        return this.ingredientId;
    }

    public String getIngredientName() {
        return this.ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public String getIngredientDesc() {
        return this.ingredientDesc;
    }

    public void setIngredientDesc(String ingredientDesc) {
        this.ingredientDesc = ingredientDesc;
    }

    public String getUnits() {
        return this.units;
    }

    public void setUnits (String units) {
        this.units = units;
    }

}