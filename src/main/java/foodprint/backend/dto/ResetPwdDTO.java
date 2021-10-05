package foodprint.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class ResetPwdDTO {
    
    @Schema(defaultValue="bobbytan@gmail.com")
    @NotEmpty @Email
    private String email;

    @Schema(defaultValue="NewSuperSecurePassw0rd")
    @NotEmpty
    @Size(min = 8, max = 60)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message = "must have one letter, one number and at least 8 characters")
    private String password;

    public ResetPwdDTO() {
        this.email = "";
    }

    public ResetPwdDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

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