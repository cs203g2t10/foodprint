package foodprint.backend.dto;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

import io.swagger.v3.oas.annotations.media.Schema;

public class IngredientDTO {

    @Schema(defaultValue = "salmon")
    @NotEmpty
    @Length(min = 1, max = 40)
    private String ingredientName;

    @Schema(defaultValue = "Raw Fish")
    @NotEmpty
    @Length(min = 1)
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
