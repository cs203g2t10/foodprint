package foodprint.backend.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

public class CurrentUserDetailsDTO {
    
    @Schema(defaultValue="1")
    private Long userId;

    @Schema(defaultValue="bobbytan@gmail.com")
    private String email;

    @Schema(defaultValue="Bobby")
    private String firstName;

    @Schema(defaultValue="Tan")
    private String lastName;

    @Schema(defaultValue="false")
    private Boolean twoFaSet;

    @Schema(defaultValue="BOBBY TAN")
    private String vaccinationName;

    @Schema(defaultValue="1970-01-01")
    private LocalDate vaccinationDob;

    private String[] userRoles;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    } 

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

    public Boolean isTwoFaSet() {
        return this.twoFaSet;
    }

    public Boolean getTwoFaSet() {
        return this.twoFaSet;
    }

    public void setTwoFaSet(Boolean twoFaSet) {
        this.twoFaSet = twoFaSet;
    }

    public Boolean isVaccinated() {
        return this.getVaccinationDob() != null && this.getVaccinationName() != null;
    }

    public String getVaccinationName() {
        return this.vaccinationName;
    }

    public void setVaccinationName(String vaccinationName) {
        this.vaccinationName = vaccinationName;
    }

    public LocalDate getVaccinationDob() {
        return this.vaccinationDob;
    }

    public void setVaccinationDob(LocalDate vaccinationDob) {
        this.vaccinationDob = vaccinationDob;
    }

    public String[] getUserRoles() {
        return this.userRoles;
    }

    public void setUserRoles(String[] userRoles) {
        this.userRoles = userRoles;
    }


}
