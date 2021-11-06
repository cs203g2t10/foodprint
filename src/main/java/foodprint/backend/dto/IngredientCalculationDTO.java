package foodprint.backend.dto;

public class IngredientCalculationDTO {
    private String ingredient; 

    private int quantity;

    private String units;


    public IngredientCalculationDTO(String ingredient, int quantity, String units) {
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.units = units;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    
}
