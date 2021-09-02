package foodprint.backend.model;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Entity
@Table
@EnableTransactionManagement
public class RestaurantManager extends User{
    
    @ManyToOne
    @JoinColumn(name="restaurantId")
    private Restaurant restaurant;
    
    public RestaurantManager(String email, String password, String name, Restaurant restaurant) {
        super(email, password, name);
        this.restaurant = restaurant;
    }
}
