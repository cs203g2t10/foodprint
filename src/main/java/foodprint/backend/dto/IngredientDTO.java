package foodprint.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class IngredientDTO {
    @Schema(defaultValue = "salmon")
    private String ingredientName;

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }
}
