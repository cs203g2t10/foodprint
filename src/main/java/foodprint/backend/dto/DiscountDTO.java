package foodprint.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class DiscountDTO {

    @Schema(defaultValue="1")
    private Integer restaurantId;

    @Schema(defaultValue="1for1")
    private String discountDescription;

    @Schema(defaultValue="50")
    private Integer discountPercentage;

    public DiscountDTO(Integer restaurantId, String discountDescription, Integer discountPercentage) {
        this.restaurantId = restaurantId;
        this.discountDescription = discountDescription;
        this.discountPercentage = discountPercentage;
    }

    public Integer getRestaurantId() {
        return this.restaurantId;
    }

    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getDiscountDescription() {
        return this.discountDescription;
    }

    public void setDiscountDescription(String discountDescription) {
        this.discountDescription = discountDescription;
    }

    public Integer getDiscountPercentage() {
        return this.discountPercentage;
    }

    public void setDiscountPercentage(Integer discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public DiscountDTO restaurantId(Integer restaurantId) {
        setRestaurantId(restaurantId);
        return this;
    }

    public DiscountDTO discountDescription(String discountDescription) {
        setDiscountDescription(discountDescription);
        return this;
    }

    public DiscountDTO discountPercentage(Integer discountPercentage) {
        setDiscountPercentage(discountPercentage);
        return this;
    }
}