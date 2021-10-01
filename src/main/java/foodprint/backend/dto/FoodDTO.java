package foodprint.backend.dto;

import java.util.*;

import io.swagger.v3.oas.annotations.media.Schema;

public class FoodDTO {
    @Schema(defaultValue = "sushi")
    private String foodName;

    @Schema(defaultValue = "sashimi")
    private String foodDesc;

    @Schema(defaultValue = "10")
    private Double foodPrice;

    private List<FoodIngredientQuantityDTO> ingredientQuantityList;

    public String getFoodName() {
        return this.foodName;
    }

    public String getFoodDesc() {
        return this.foodDesc;
    }

    public Double getFoodPrice() {
        return this.foodPrice;
    }

    public List<FoodIngredientQuantityDTO> getIngredientQuantityList() {
        return this.ingredientQuantityList;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public void setFoodDesc(String foodDesc) {
        this.foodDesc = foodDesc;
    }

    public void setFoodPrice(Double foodPrice) {
        this.foodPrice = foodPrice;
    }

    public void setIngredientQuantityList(List<FoodIngredientQuantityDTO> ingredientQuantityList) {
        this.ingredientQuantityList = ingredientQuantityList;
    }
}
