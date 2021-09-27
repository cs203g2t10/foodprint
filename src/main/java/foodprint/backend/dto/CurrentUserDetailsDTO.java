package foodprint.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class CurrentUserDetailsDTO {
    
    @Schema(defaultValue="bobbytan@gmail.com")
    private String email;

    @Schema(defaultValue="Bobby")
    private String firstName;

    @Schema(defaultValue="Tan")
    private String lastName;

    @Schema(defaultValue="1")
    private Long userId;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    } 

}
