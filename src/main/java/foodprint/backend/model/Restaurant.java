package foodprint.backend.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import javax.persistence.Table;

import org.springframework.transaction.annotation.EnableTransactionManagement;


@Entity
@Table
@EnableTransactionManagement

public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurantId")
    private Integer restaurantId;

    @Column(name = "restaurantName")
    private String restaurantName;

    @Column(name = "restaurantDesc")
    private String restaurantDesc;

    @Column(name = "picturesPath")
    private String picturesPath;

    @Column (name = "restaurantLocation")
    private String restaurantLocation;

    protected Restaurant () { }

    public Restaurant (Integer restaurantId, String restaurantName, String restaurantLocation) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.restaurantLocation = restaurantLocation;
    }

    // Mutators and Accessors
    public Integer getRestaurantId() {
        return this.restaurantId;
    }

    public String getRestaurantName() {
        return this.restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantDesc() {
        return this.restaurantDesc;
    }

    public void setRestaurantDesc(String restaurantDesc) {
        this.restaurantDesc = restaurantDesc;
    }

    public String getPicturesPath() {
        return this.picturesPath;
    }

    public void setPicturesPath(String picturesPath) {
        this.picturesPath = picturesPath;
    }

    public String getRestaurantLocation() {
        return this.restaurantLocation;
    }

    public void setRestaurantLocation(String restaurantLocation) {
        this.restaurantLocation = restaurantLocation;
    }

}
