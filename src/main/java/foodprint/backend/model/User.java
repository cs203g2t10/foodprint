package foodprint.backend.model;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    // Constructors
    protected User() {}

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
