package foodprint.backend.dto;

import foodprint.backend.model.Reservation.ReservationStatus;

import java.util.List;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

public class CreateReservationDTO {

    @Schema(defaultValue = "2021-12-25T17:21:29.142Z")
    private LocalDateTime date;

    @Schema(defaultValue = "5")
    private Integer pax;

    @Schema(defaultValue = "true")
    private Boolean isVaccinated;

    private List<LineItemDTO> lineItems;
    
    @Schema(defaultValue = "1")
    private Long restaurantId;

    @Schema(defaultValue = "UNPAID")
    private ReservationStatus status = ReservationStatus.UNPAID;

    public CreateReservationDTO(LocalDateTime date, Integer pax, Boolean isVaccinated, List<LineItemDTO> lineItems, Long restaurantId, ReservationStatus status) {
        this.date = date;
        this.pax = pax;
        this.isVaccinated = isVaccinated;
        this.lineItems = lineItems;
        this.restaurantId = restaurantId;
        this.status = status;
    }

    public CreateReservationDTO() {}

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

    public List<LineItemDTO> getLineItems() {
        return this.lineItems;
    }

    public void setLineItems(List<LineItemDTO> lineItems) {
        this.lineItems = lineItems;
    }

    public Long getRestaurantId() {
        return this.restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

}
