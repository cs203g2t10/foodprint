package foodprint.backend.model;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
public class FoodIngredientQuantity {
    @EmbeddedId
    private FoodIngredientQuantityKey id;

    @ManyToOne
    @MapsId("ingredientId")
    @JoinColumn(name = "ingredientId")
    private Ingredient ingredient;

    @ManyToOne
    @JsonIgnore
    @MapsId("foodId")
    @JoinColumn(name = "foodId")
    private Food food;

    @Schema(defaultValue = "1")
    private Integer quantity;

    public FoodIngredientQuantity(Food food, Ingredient ingredient, Integer quantity) {
        this.food = food;
        this.ingredient = ingredient;
        this.quantity = quantity;
    }

    public Food getFood () {
        return this.food;
    }

    public void setFood(Food food) {
        this.food = food;
    }

    public Ingredient getIngredient() {
        return this.ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
