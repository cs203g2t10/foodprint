package foodprint.backend.dto;

import java.util.Set;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class EditFoodDTO {
    @NotEmpty(message = "cannot be empty.")
    @Size(min = 1, max = 40, message = "has to be between 1 to 40 letters.")
    private String foodName;

    @NotEmpty(message = "cannot be empty.")
    private String foodDesc;

    private Double foodPrice;

    private Set<FoodIngredientQuantityDTO> foodIngredientQuantity;

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodDesc() {
        return foodDesc;
    }

    public void setFoodDesc(String foodDesc) {
        this.foodDesc = foodDesc;
    }

    public Double getFoodPrice() {
        return foodPrice;
    }

    public void setFoodPrice(Double foodPrice) {
        this.foodPrice = foodPrice;
    }

    public Set<FoodIngredientQuantityDTO> getFoodIngredientQuantity() {
        return foodIngredientQuantity;
    }

    public void setFoodIngredientQuantity(Set<FoodIngredientQuantityDTO> foodIngredientQuantity) {
        this.foodIngredientQuantity = foodIngredientQuantity;
    }

    
}
