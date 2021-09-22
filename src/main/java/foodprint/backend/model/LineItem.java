package foodprint.backend.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
public class LineItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lineItemId")
    @Schema(defaultValue="1")
    private Long lineItemId;

    @ManyToOne(cascade=CascadeType.MERGE)
    @JoinColumn(name = "foodId")
    private Food food;

    @JsonIgnore
    @ManyToOne(cascade=CascadeType.MERGE)
    @JoinColumn(name = "reservationId")
    private Reservation reservation;

    @Column(name = "quantity")
    @Schema(defaultValue = "1")
    private Integer quantity;

    public LineItem() {}

    public LineItem(Food food, Reservation reservation, Integer quantity) {
        this.food = food;
        this.reservation = reservation;
        this.quantity = quantity;
    }

    public Long getLineItemId() {
        return this.lineItemId;
    }

    public Food getFood() {
        return this.food;
    }

    public void setFood(Food food) {
        this.food = food;
    }

    public Reservation getReservation() {
        return this.reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

}
