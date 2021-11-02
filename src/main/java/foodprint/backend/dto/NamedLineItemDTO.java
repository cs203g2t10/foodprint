package foodprint.backend.dto;

import foodprint.backend.model.Picture;
import io.swagger.v3.oas.annotations.media.Schema;

public class NamedLineItemDTO {

    @Schema(defaultValue = "1")
    private Long foodId;

    @Schema(defaultValue = "TastyFood")
    private String foodName;

    @Schema(defaultValue = "1")
    private Integer quantity;

    private Picture picture;

    public Long getFoodId() {
        return this.foodId;
    }

    public void setFoodId(Long foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return this.foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Picture getPicture() {
        return picture;
    }

    public void setPicture(Picture picture) {
        this.picture = picture;
    }

}
