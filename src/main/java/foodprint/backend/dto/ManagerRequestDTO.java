package foodprint.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotEmpty;

public class ManagerRequestDTO {
    @Schema(defaultValue = "1")
    @NotEmpty
    private Long userId;

    @Schema(defaultValue = "1")
    @NotEmpty
    private Long restaurantId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }
}

