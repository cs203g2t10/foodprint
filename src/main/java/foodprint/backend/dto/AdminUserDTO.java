package foodprint.backend.dto;

import java.time.LocalDateTime;
import java.util.Objects;

public class AdminUserDTO {
    private String firstName;

    private String lastName;
    
    private String email;

    private String password;

    private String roles;

    private LocalDateTime lastLogin;

    private LocalDateTime registeredOn;

    public AdminUserDTO() {
    }

    public AdminUserDTO(String firstName, String lastName, String email, String password, String roles, LocalDateTime lastLogin, LocalDateTime registeredOn) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.lastLogin = lastLogin;
        this.registeredOn = registeredOn;
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

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoles() {
        return this.roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public LocalDateTime getLastLogin() {
        return this.lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public LocalDateTime getRegisteredOn() {
        return this.registeredOn;
    }

    public void setRegisteredOn(LocalDateTime registeredOn) {
        this.registeredOn = registeredOn;
    }


    public AdminUserDTO firstName(String firstName) {
        setFirstName(firstName);
        return this;
    }

    public AdminUserDTO lastName(String lastName) {
        setLastName(lastName);
        return this;
    }

    public AdminUserDTO email(String email) {
        setEmail(email);
        return this;
    }

    public AdminUserDTO password(String password) {
        setPassword(password);
        return this;
    }

    public AdminUserDTO roles(String roles) {
        setRoles(roles);
        return this;
    }

    public AdminUserDTO lastLogin(LocalDateTime lastLogin) {
        setLastLogin(lastLogin);
        return this;
    }

    public AdminUserDTO registeredOn(LocalDateTime registeredOn) {
        setRegisteredOn(registeredOn);
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof AdminUserDTO)) {
            return false;
        }
        AdminUserDTO adminUserDTO = (AdminUserDTO) o;
        return Objects.equals(firstName, adminUserDTO.firstName) && Objects.equals(lastName, adminUserDTO.lastName) && Objects.equals(email, adminUserDTO.email) && Objects.equals(password, adminUserDTO.password) && Objects.equals(roles, adminUserDTO.roles) && Objects.equals(lastLogin, adminUserDTO.lastLogin) && Objects.equals(registeredOn, adminUserDTO.registeredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, email, password, roles, lastLogin, registeredOn);
    }


    @Override
    public String toString() {
        return "{" +
            " firstName='" + getFirstName() + "'" +
            ", lastName='" + getLastName() + "'" +
            ", email='" + getEmail() + "'" +
            ", password='" + getPassword() + "'" +
            ", roles='" + getRoles() + "'" +
            ", lastLogin='" + getLastLogin() + "'" +
            ", registeredOn='" + getRegisteredOn() + "'" +
            "}";
    }

    
}
