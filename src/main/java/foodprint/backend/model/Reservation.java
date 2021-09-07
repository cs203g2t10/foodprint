package foodprint.backend.model;

import java.util.Date;
import java.util.List;

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

import org.springframework.transaction.annotation.EnableTransactionManagement;

@Entity
@Table
@EnableTransactionManagement

public class Reservation {

    // Properties
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservationId")
    private Integer reservationId;

    @ManyToOne(cascade=CascadeType.MERGE)
    @JoinColumn(name = "user")
    private User user;

    @Column(name = "date")
    private Date date;

    @Column(name = "pax")
    private Integer pax;

    @Column(name = "isVaccinated")
    private Boolean isVaccinated;

    @Column(name = "reservedOn")
    private Date reservedOn;

    public enum Status {
        ONGOING, CANCELLED
    }
    @Column(name = "status")
    private Status status;

    @OneToMany(cascade=CascadeType.MERGE)
    @Column(name="lineItems")
    private List<LineItem> lineItems;

    @ManyToOne(cascade=CascadeType.MERGE)
    @JoinColumn(name="restaurantId")
    private Restaurant restaurant;

    //Constructors
    public Reservation() {}

    public Reservation(Integer reservationId, User user, Date date, Integer pax, Boolean isVaccinated, Date reservedOn, Status status, List<LineItem> lineItems, Restaurant restaurant) {
        this.reservationId = reservationId;
        this.user = user;
        this.date = date;
        this.pax = pax;
        this.isVaccinated = isVaccinated;
        this.reservedOn = reservedOn;
        this.status = status;
        this.lineItems = lineItems;
        this.restaurant = restaurant;
    }


    public Integer getReservationId() {
        return this.reservationId;
    }

    public void setReservationId(Integer reservationId) {
        this.reservationId = reservationId;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
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

    public Date getReservedOn() {
        return this.reservedOn;
    }

    public void setReservedOn(Date reservedOn) {
        this.reservedOn = reservedOn;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
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

}
