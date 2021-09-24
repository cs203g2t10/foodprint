package foodprint.backend.dto;

import java.util.List;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

public class ReservationDTO {

    @Schema(defaultValue = "1")
    private Long reservationId;

    @Schema(defaultValue = "1")
    private Long restaurantId;

    @Schema(defaultValue = "Sushi Tei")
    private String restaurantName;

    @Schema(defaultValue = "2021-12-25T17:21:29.142Z")
    private LocalDateTime date;

    @Schema(defaultValue = "5")
    private Integer pax;

    @Schema(defaultValue = "true")
    private Boolean isVaccinated;

    private List<NamedLineItemDTO> lineItems;

    public ReservationDTO() {}

    public Long getReservationId() {
        return this.reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public LocalDateTime getDate() {
        return this.date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Integer getPax() {
        return this.pax;
    }

    public void setPax(Integer pax) {
        this.pax = pax;
    }

    public Boolean getIsVaccinated() {
        return this.isVaccinated;
    }

    public void setIsVaccinated(Boolean isVaccinated) {
        this.isVaccinated = isVaccinated;
    }

    public List<NamedLineItemDTO> getLineItems() {
        return this.lineItems;
    }

    public void setLineItems(List<NamedLineItemDTO> lineItems) {
        this.lineItems = lineItems;
    }

    public Long getRestaurantId() {
        return this.restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public Boolean isIsVaccinated() {
        return this.isVaccinated;
    }

    public String getRestaurantName() {
        return this.restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }


}