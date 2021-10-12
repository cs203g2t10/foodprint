package foodprint.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;

public class TwoFaRequestDTO {
    
    @Schema(defaultValue="bobbytan@gmail.com")
    @NotEmpty @Email
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
