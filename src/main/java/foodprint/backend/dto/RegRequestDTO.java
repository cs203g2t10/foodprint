package foodprint.backend.dto;


import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

public class RegRequestDTO {

    @Schema(defaultValue="bobbytan@gmail.com")
    @NotEmpty @Email
    private String email;

    @Schema(defaultValue="SuperSecurePassw0rd")
    @NotEmpty
    @Size(min = 8, max = 60)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message = "must have 1 letter, 1 number and at least 8 characters")
    private String password;

    @Schema(defaultValue="Bobby")
    @NotEmpty
    private String firstName;

    @Schema(defaultValue="Tan")
    @NotEmpty
    private String lastName;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    

}
