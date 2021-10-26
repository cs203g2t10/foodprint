package foodprint.backend.dto;

import java.util.Set;

public class EditFoodDTO {

    private String foodName;

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
