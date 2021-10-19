package foodprint.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class IngredientDTO {

    @Schema(defaultValue = "salmon")
    private String ingredientName;

    @Schema(defaultValue = "Raw Fish")
    private String ingredientDesc;

    @Schema(defaultValue = "gm")
    private String units;

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public String getIngredientDesc() {
        return this.ingredientDesc;
    }

    public void setIngredientDesc(String ingredientDesc) {
        this.ingredientDesc = ingredientDesc;
    }

    public String getUnits() {
        return this.units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

}
