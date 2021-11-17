package foodprint.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class ResetPwdDTO {

    @Schema(defaultValue="NewSuperSecurePassw0rd")
    @NotEmpty(message = "Password should not be empty")
    @Size(min = 8, max = 60)
    @Pattern(regexp = "(?=^.{8,}$)(?=.*\\d)(?=.*[a-zA-Z])(?!.*\\s)[0-9a-zA-Z*$-+?_&=!%{}/'.]*$", message = "must have 1 letter, 1 number and at least 8 characters")
    private String password;

    public ResetPwdDTO() {}

    public ResetPwdDTO(String password) {
        this.password = password;
    }
    
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}