package foodprint.backend.model;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import javax.persistence.Table;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Entity
@Table
@EnableTransactionManagement

public class Restaurant {
    @Id
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


    public Restaurant (Integer restaurantId, String restaurantName, String restaurantLocation) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.restaurantLocation = restaurantLocation;
    }

    // Mutators and Accessors
    public void setDesc (String desc) {
        this.restaurantDesc = desc;
    }

    public void setPicturePaths(String path) {
        this.picturesPath = path;
    }

}
