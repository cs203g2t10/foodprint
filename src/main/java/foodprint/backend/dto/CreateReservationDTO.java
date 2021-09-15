package foodprint.backend.dto;

import java.util.List;
import java.time.LocalDateTime;
import foodprint.backend.model.*;
import io.swagger.v3.oas.annotations.media.Schema;

public class CreateReservationDTO {
    
    @Schema(defaultValue="bobbytan@gmail.com")
    private String email;

    private LocalDateTime date;

    @Schema(defaultValue = "5")
    private Integer pax;

    @Schema(defaultValue = "true")
    private Boolean isVaccinated;

    private List<LineItem> lineItems;
    
    private Restaurant restaurant;

    public CreateReservationDTO(String email, LocalDateTime date, Integer pax, Boolean isVaccinated, List<LineItem> lineItems, Restaurant restaurant) {
        this.email = email;
        this.date = date;
        this.pax = pax;
        this.isVaccinated = isVaccinated;
        this.lineItems = lineItems;
        this.restaurant = restaurant;
    }

    public CreateReservationDTO() {}

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public List<LineItem> getLineItems() {
        return this.lineItems;
    }

    public void setLineItems(List<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public Restaurant getRestaurant() {
        return this.restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

}
