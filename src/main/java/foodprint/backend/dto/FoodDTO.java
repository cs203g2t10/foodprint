package foodprint.backend.dto;

import java.util.*;


import org.hibernate.validator.constraints.Length;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class FoodDTO {
    @Schema(defaultValue = "sushi")
    @NotNull
    @Length(min = 1, max = 40)
    private String foodName;

    @Schema(defaultValue = "sashimi")
    @Length(min = 1)
    @NotNull
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
