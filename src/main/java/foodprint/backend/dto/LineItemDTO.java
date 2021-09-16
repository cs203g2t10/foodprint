package foodprint.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class LineItemDTO {

    @Schema(defaultValue = "1")
    private Integer foodId;

    @Schema(defaultValue = "1")
    private Integer quantity;

    public LineItemDTO() {
    }

    public LineItemDTO(Integer foodId, Integer quantity) {
        this.foodId = foodId;
        this.quantity = quantity;
    }

    public Integer getFoodId() {
        return this.foodId;
    }

    public void setFoodId(Integer foodId) {
        this.foodId = foodId;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

}
