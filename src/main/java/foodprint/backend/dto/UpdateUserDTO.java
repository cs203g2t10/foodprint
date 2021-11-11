package foodprint.backend.dto;

import java.util.Objects;

import javax.validation.constraints.Email;

public class UpdateUserDTO {

    private String firstName;

    private String lastName;

    @Email
    private String email;

    private String oldPassword;

    private String newPassword;

    public UpdateUserDTO() {
    }

    public UpdateUserDTO(String firstName, String lastName, String email, String oldPassword, String newPassword) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
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

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
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

    public UpdateUserDTO newPassword(String newPassword) {
        setNewPassword(newPassword);
        return this;
    }

    public UpdateUserDTO oldPassword(String oldPassword) {
        setOldPassword(oldPassword);
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
        return Objects.equals(firstName, updateUserDTO.firstName) && Objects.equals(lastName, updateUserDTO.lastName) && Objects.equals(email, updateUserDTO.email) && Objects.equals(oldPassword, updateUserDTO.oldPassword) && Objects.equals(newPassword, updateUserDTO.newPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, email, oldPassword, newPassword);
    }

    @Override
    public String toString() {
        return "{" +
            " firstName='" + getFirstName() + "'" +
            ", lastName='" + getLastName() + "'" +
            ", email='" + getEmail() + "'" +
            ", oldPassword='" + getOldPassword() + "'" +
            ", newPassword='" + getNewPassword() + "'" +
            "}";
    }
    
}
