package foodprint.backend.dto;


public class FoodIngredientQuantityDTO {

    private Integer ingredientId;

    private Integer quantity;

    public Integer getIngredientId() {
        return ingredientId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setIngredientId(Integer ingredientId) {
        this.ingredientId = ingredientId;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
