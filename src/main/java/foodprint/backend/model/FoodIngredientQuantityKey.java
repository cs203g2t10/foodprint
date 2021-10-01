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
}
