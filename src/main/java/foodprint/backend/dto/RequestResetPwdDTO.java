package foodprint.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

public class RequestResetPwdDTO {
    
    @Schema(defaultValue="bobbytan@gmail.com")
    @NotEmpty @Email
    private String email;

    public RequestResetPwdDTO() {
        this.email = "";
    }

    public RequestResetPwdDTO(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

}
