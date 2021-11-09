package foodprint.backend.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;

public class FoodIngredientQuantityKey implements Serializable {
    @Column(name = "foodId")
    private Long foodId;
    
    @Column(name = "ingredientId")
    private Long ingredientId;

    @Override
    public int hashCode() {
        return Objects.hash(foodId, ingredientId);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public FoodIngredientQuantityKey() {

    }

    public FoodIngredientQuantityKey(Long foodId, Long ingredientId) {
        this.foodId = foodId;
        this.ingredientId = ingredientId;
    }

    public Long getFoodId() {
        return foodId;
    }

    public void setFoodId(Long foodId) {
        this.foodId = foodId;
    }

    public Long getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(Long ingredientId) {
        this.ingredientId = ingredientId;
    }
}
