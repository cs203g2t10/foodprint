package foodprint.backend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import foodprint.backend.config.CustomAuthorityDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
@Table
@EnableTransactionManagement
public class User implements UserDetails {

    // Properties
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Schema(defaultValue="1")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Column(name = "email", nullable=false, unique=true)
    @Schema(defaultValue="bobbytan@gmail.com")
    @Email
    @NotEmpty(message = "Email should not be empty")
    private String email;

    @Column(name = "fName", nullable = false)
    @Schema(defaultValue="Bobby")
    @NotEmpty(message = "First name should not be empty")
    private String firstName;

    @Column(name = "lName", nullable = true)
    @Schema(defaultValue="Tan")
    @NotEmpty(message = "Last name should not be empty")
    private String lastName;

    @Column(name = "password", nullable = false)
    @Schema(defaultValue="Hello123")
    @NotEmpty(message = "Password should not be empty")
    private String password;

    @Column(name = "role", nullable = false)
    @Schema(defaultValue="FP_USER")
    @NotEmpty(message = "Roles should not be empty")
    private String roles;

    @Column(name = "lastLogin", nullable = true)
    private LocalDateTime lastLogin;

    @Column(name = "registeredOn", nullable = false)
    @NotNull
    private LocalDateTime registeredOn;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Reservation> reservations;

    @Column(name = "vaccinationName", nullable = true)
    private String vaccinationName;

    @Column(name = "vaccinationDob", nullable = true)
    private LocalDate vaccinationDob;
    
    @OneToMany(mappedBy="requestor")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Token> token;
    
    @Column(name = "twoFaSecret")
    private String twoFaSecret;

    @Column(name = "twoFaSet")
    private Boolean twoFaSet = false;

    @ManyToOne
    @JoinColumn(name="restaurantId", nullable = true)
    private Restaurant restaurant;

    @ManyToMany
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(name = "user_favouriterestaurants", joinColumns ={ @JoinColumn(name = "user_id")}, inverseJoinColumns ={ @JoinColumn(name = "restaurant_id")} )
    private Set<Restaurant> favouriteRestaurants = new HashSet<>();
    
    public User() {}

    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.firstName = name;
        this.roles = "FP_USER";
    }

    // Mutators and Accessors
    public Long getId() {
        return this.id;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getRoles() {
        return this.roles;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }    

    public LocalDateTime getRegisteredOn() {
        return registeredOn;
    }

    public void setRegisteredOn(LocalDateTime registeredOn) {
        this.registeredOn = registeredOn;
    }

    public List<Reservation> getReservations() {
        return this.reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public String getVaccinationName() {
        return this.vaccinationName;
    }

    public void setVaccinationName(String vaccinationName) {
        this.vaccinationName = vaccinationName;
    }

    public LocalDate getVaccinationDob() {
        return this.vaccinationDob;
    }

    public void setVaccinationDob(LocalDate vaccinationDob) {
        this.vaccinationDob = vaccinationDob;
    }

    public boolean isVaccinated() {
        return (this.vaccinationDob != null && this.vaccinationName != null);
    }
    
    public List<Token> getToken() {
        return this.token;
    }

    public void setToken(List<Token> token) {
        this.token = token;
    }

    public String getTwoFaSecret() {
        return this.twoFaSecret;
    }

    public void setTwoFaSecret(String twoFaSecret) {
        this.twoFaSecret = twoFaSecret;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    public Boolean isTwoFaSet() {
        return twoFaSet;
    }

    public void setTwoFaSet(Boolean twoFaSet) {
        this.twoFaSet = twoFaSet;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public Set<Restaurant> getFavouriteRestaurants() {
        return this.favouriteRestaurants;
    }

    public void setFavouriteRestaurants(Set<Restaurant> favouriteRestaurants) {
        this.favouriteRestaurants = favouriteRestaurants;
    }

    @Override
    @JsonDeserialize(using = CustomAuthorityDeserializer.class)
    @Schema(hidden=true)
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<String> userRoles = Arrays.asList(this.roles.split(","));
        return userRoles
            .stream()
            .map(role -> new SimpleGrantedAuthority(role.trim()))
            .collect(Collectors.toList());
    }

    @Override
    @Schema(hidden=true)
    public String getUsername() {
        return this.getEmail();
    }

    @Override
    @Schema(hidden=true)
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @Schema(hidden=true)
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @Schema(hidden=true)
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @Schema(hidden=true)
    public boolean isEnabled() {
        return true;
    }


    @Override
    public String toString() {
        return "{" +
            " id='" + getId() + "'" +
            ", email='" + getEmail() + "'" +
            ", firstName='" + getFirstName() + "'" +
            ", lastName='" + getLastName() + "'" +
            ", password='" + getPassword() + "'" +
            ", roles='" + getRoles() + "'" +
            ", lastLogin='" + getLastLogin() + "'" +
            ", registeredOn='" + getRegisteredOn() + "'" +
            ", reservations='" + getReservations() + "'" +
            ", vaccinationName='" + getVaccinationName() + "'" +
            ", vaccinationDob='" + getVaccinationDob() + "'" +
            "}";
    }

    
}
