package foodprint.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

public class RestaurantDTO {
    
    @Schema(defaultValue="Sushi Tei")
    @NotEmpty
    private String restaurantName;

    @Schema(defaultValue="Serangoon")
    private String restaurantLocation;

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantLocation() {
        return restaurantLocation;
    }

    public void setRestaurantLocation(String restaurantLocation) {
        this.restaurantLocation = restaurantLocation;
    }

}
