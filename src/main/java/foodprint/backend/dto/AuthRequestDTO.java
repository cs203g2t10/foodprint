package foodprint.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public class AuthRequestDTO {
    
    @Schema(defaultValue="bobbytan@gmail.com")
    @NotEmpty @Email
    private String email;

    @Schema(defaultValue="SuperSecurePassw0rd")
    @NotEmpty
    private String password;

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

}
