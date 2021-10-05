package foodprint.backend.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;

public class FoodIngredientQuantityKey implements Serializable{
    @Column(name = "foodId")
    private Long foodId;
    
    @Column(name = "ingredientId")
    private Integer ingredientId;

    @Override
    public int hashCode() {
        return Objects.hash(foodId, ingredientId);
    }

    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        return super.equals(obj);
    }

    public Long getFoodId() {
        return foodId;
    }

    public void setFoodId(Long foodId) {
        this.foodId = foodId;
    }

    public Integer getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(Integer ingredientId) {
        this.ingredientId = ingredientId;
    }
}
