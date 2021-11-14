package foodprint.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public class AuthRequestDTO {
    
    @Schema(defaultValue="bobbytan@gmail.com")
    @NotEmpty(message = "Email should not be empty")
    @Email
    private String email;

    @Schema(defaultValue="Hello123")
    @NotEmpty(message = "Password should not be empty")
    private String password;

    private String token;

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

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }


}
