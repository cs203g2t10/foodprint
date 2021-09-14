package foodprint.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class RegRequest {

    @Schema(defaultValue="bobbytan@gmail.com")
    private String email;

    @Schema(defaultValue="SuperSecurePassw0rd")
    private String password;

    @Schema(defaultValue="Bobby")
    private String firstName;

    @Schema(defaultValue="Tan")
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
