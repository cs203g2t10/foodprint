package foodprint.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class FoodIngredientQuantityDTO {

    @Schema(defaultValue = "1")
    private Long ingredientId;

    @Schema(defaultValue = "1")
    private Integer quantity;

    public Long getIngredientId() {
        return ingredientId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setIngredientId(Long ingredientId) {
        this.ingredientId = ingredientId;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
