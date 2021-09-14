package foodprint.backend.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table
@EnableTransactionManagement
public class User implements UserDetails{

    // Properties
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable=false, unique=true)
    @Schema(defaultValue="bobbytan@gmail.com")
    private String email;

    @Column(name = "fName", nullable = false)
    @Schema(defaultValue="Bobby")
    private String firstName;

    @Column(name = "lName", nullable = true)
    @Schema(defaultValue="Tan")
    private String lastName;

    @Column(name = "password", nullable = false)
    @Schema(defaultValue="SuperSecurePassw0rd")
    private String password;

    @Column(name = "role", nullable = false)
    @Schema(defaultValue="FP_USER")
    private String roles;

    @Column(name = "lastLogin", nullable = true)
    private LocalDateTime lastLogin;

    @Column(name = "registeredOn", nullable = false)
    private LocalDateTime registeredOn;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.MERGE)
    private List<Reservation> reservations;

    // Constructors
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


    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    @Schema(hidden=true)
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // return Arrays.asList(this.roles.split(","));
        return null;
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
        return "User [email=" + email + ", firstName=" + firstName + ", id=" + id + ", lastName=" + lastName
                + ", password=" + password + ", roles=" + roles + "]";
    }

    
}
