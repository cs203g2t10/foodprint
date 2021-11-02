package foodprint.backend.dto;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

import io.swagger.v3.oas.annotations.media.Schema;

public class DiscountDTO {

    @Schema(defaultValue="1")
    private Long restaurantId;

    @Schema(defaultValue="1for1")
    @NotEmpty
    @Length(min = 1)
    private String discountDescription;

    @Schema(defaultValue="50")
    @NotEmpty
    private Integer discountPercentage;

    public DiscountDTO(Long restaurantId, String discountDescription, Integer discountPercentage) {
        this.restaurantId = restaurantId;
        this.discountDescription = discountDescription;
        this.discountPercentage = discountPercentage;
    }

    public Long getRestaurantId() {
        return this.restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
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

    public DiscountDTO restaurantId(Long restaurantId) {
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
