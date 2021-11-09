package foodprint.backend.dto;

import java.util.Objects;

import javax.validation.constraints.Email;

public class UpdateUserDTO {

    private String firstName;

    private String lastName;

    @Email
    private String email;

    private String password;

    public UpdateUserDTO() {
    }

    public UpdateUserDTO(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
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

    public UpdateUserDTO firstName(String firstName) {
        setFirstName(firstName);
        return this;
    }

    public UpdateUserDTO lastName(String lastName) {
        setLastName(lastName);
        return this;
    }

    public UpdateUserDTO email(String email) {
        setEmail(email);
        return this;
    }

    public UpdateUserDTO password(String password) {
        setPassword(password);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof UpdateUserDTO)) {
            return false;
        }
        UpdateUserDTO updateUserDTO = (UpdateUserDTO) o;
        return Objects.equals(firstName, updateUserDTO.firstName) && Objects.equals(lastName, updateUserDTO.lastName) && Objects.equals(email, updateUserDTO.email) && Objects.equals(password, updateUserDTO.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, email, password);
    }

    @Override
    public String toString() {
        return "{" +
            " firstName='" + getFirstName() + "'" +
            ", lastName='" + getLastName() + "'" +
            ", email='" + getEmail() + "'" +
            ", password='" + getPassword() + "'" +
            "}";
    }
    
    
}
