package foodprint.backend.model;

import java.util.List;
import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.transaction.annotation.EnableTransactionManagement;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table
@EnableTransactionManagement

public class Reservation implements Serializable {
    
    // Properties
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservationId")
    @Schema(defaultValue="1")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long reservationId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "pax")
    @Schema(defaultValue = "5")
    private Integer pax;

    @Column(name = "isVaccinated")
    @Schema(defaultValue = "true")
    private Boolean isVaccinated;

    @Column(name = "reservedOn")
    private LocalDateTime reservedOn;

    public enum ReservationStatus {
        PAID, UNPAID, CANCELLED
    }
    @Column(name = "status")
    @Schema(defaultValue = "UNPAID")
    private ReservationStatus status;

    @OneToMany(mappedBy = "reservation", cascade=CascadeType.ALL, orphanRemoval = true)
    private List<LineItem> lineItems;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="restaurantId")
    private Restaurant restaurant;

    public Double getPrice() {
        Double price = 0.0;
        for (LineItem lineItem : lineItems) {
            price += lineItem.getFood().getFoodPrice() * lineItem.getQuantity();
        }
        price = price * 100;
        Discount discount = restaurant.getDiscount();
        if (discount != null) {
            Double discountPercentage = 100 - (double)discount.getDiscountPercentage();
            price = price * ( discountPercentage / 100);
        }
        return price;
    }

    //Constructors
    public Reservation() {}

    public Reservation(User user, LocalDateTime date, Integer pax, Boolean isVaccinated, LocalDateTime reservedOn, ReservationStatus status, List<LineItem> lineItems, Restaurant restaurant) {
        this.user = user;
        this.date = date;
        this.pax = pax;
        this.isVaccinated = isVaccinated;
        this.reservedOn = reservedOn;
        this.status = status;
        this.lineItems = lineItems;
        this.restaurant = restaurant;
    }

    public Reservation(User user, LocalDateTime date, Integer pax, Boolean isVaccinated, LocalDateTime reservedOn, ReservationStatus status, Restaurant restaurant) {
        this.user = user;
        this.date = date;
        this.pax = pax;
        this.isVaccinated = isVaccinated;
        this.reservedOn = reservedOn;
        this.status = status;
        this.restaurant = restaurant;
    }

    public void changeReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getReservationId() {
        return this.reservationId;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public LocalDateTime getReservedOn() {
        return this.reservedOn;
    }

    public void setReservedOn(LocalDateTime reservedOn) {
        this.reservedOn = reservedOn;
    }

    public ReservationStatus getStatus() {
        return this.status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
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

    public Reservation user(User user) {
        setUser(user);
        return this;
    }

    public Reservation date(LocalDateTime date) {
        setDate(date);
        return this;
    }

    public Reservation pax(Integer pax) {
        setPax(pax);
        return this;
    }

    public Reservation isVaccinated(Boolean isVaccinated) {
        setIsVaccinated(isVaccinated);
        return this;
    }

    public Reservation reservedOn(LocalDateTime reservedOn) {
        setReservedOn(reservedOn);
        return this;
    }

    public Reservation status(ReservationStatus status) {
        setStatus(status);
        return this;
    }

    public Reservation lineItems(List<LineItem> lineItems) {
        setLineItems(lineItems);
        return this;
    }

    public Reservation restaurant(Restaurant restaurant) {
        setRestaurant(restaurant);
        return this;
    } 
}
