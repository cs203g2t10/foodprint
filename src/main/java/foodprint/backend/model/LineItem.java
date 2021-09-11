package foodprint.backend.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class LineItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lineItemId")
    private Integer lineItemId;

    @OneToOne(cascade=CascadeType.MERGE)
    @JoinColumn(name = "foodId")
    private Food food;

    @ManyToOne(cascade=CascadeType.MERGE)
    @JoinColumn(name = "reservationId")
    private Reservation reservation;

    @Column(name = "quantity")
    private Integer quantity;

    public LineItem() {}

    public LineItem(Integer lineItemId, Food food, Reservation reservation, Integer quantity) {
        this.lineItemId = lineItemId;
        this.food = food;
        this.reservation = reservation;
        this.quantity = quantity;
    }

    public Integer getLineItemId() {
        return this.lineItemId;
    }

    public void setLineItemId(Integer lineItemId) {
        this.lineItemId = lineItemId;
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
