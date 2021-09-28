package foodprint.backend.model;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
public class FoodIngredientQuantity {
    @EmbeddedId
    FoodIngredientQuantityKey id;

    @ManyToOne
    @MapsId("ingredientId")
    @JoinColumn(name = "ingredientId")
    Ingredient ingredient;

    @ManyToOne
    @MapsId("foodId")
    @JoinColumn(name = "foodId")
    Food food;

    @Schema(defaultValue = "1")
    Integer quantity;

    public FoodIngredientQuantity(Food food, Ingredient ingredient, Integer quantity) {
        this.food = food;
        this.ingredient = ingredient;
        this.quantity = quantity;
    }
}
