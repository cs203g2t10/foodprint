package foodprint.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class LineItemDTO {

    @Schema(defaultValue = "1")
    private Long foodId;

    @Schema(defaultValue = "1")
    private Integer quantity;

    public LineItemDTO() {
    }

    public LineItemDTO(Long foodId, Integer quantity) {
        this.foodId = foodId;
        this.quantity = quantity;
    }

    public Long getFoodId() {
        return this.foodId;
    }

    public void setFoodId(Long foodId) {
        this.foodId = foodId;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

}
