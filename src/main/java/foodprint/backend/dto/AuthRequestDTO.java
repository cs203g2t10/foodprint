package foodprint.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class AuthRequestDTO {
    
    @Schema(defaultValue="bobbytan@gmail.com")
    private String email;

    @Schema(defaultValue="SuperSecurePassw0rd")
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
