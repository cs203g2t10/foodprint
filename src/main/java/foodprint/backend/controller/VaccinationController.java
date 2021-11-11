package foodprint.backend.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import foodprint.backend.dto.VaccinationResponseDTO;
import foodprint.backend.exceptions.VaccinationValidationException;
import foodprint.backend.model.User;
import foodprint.backend.service.UserService;
import foodprint.backend.service.VaccinationService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/vaccination")
public class VaccinationController {
    
    private UserService userService;

    private VaccinationService vaccinationService;

    @Autowired
    VaccinationController(UserService userService, VaccinationService vaccinationService) {
        this.userService = userService;
        this.vaccinationService = vaccinationService;
    }

    // Vaccination certificate validation
    @PostMapping(path = {"/validate/{userId}"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Upload the vaccination cert for verification")
    public ResponseEntity<VaccinationResponseDTO> validateVaccinationCert(
        @PathVariable("userId") Long userId,
        @RequestParam("file") MultipartFile file
    ) {

        User currentUser = userService.unprotectedGetUser(userId);
        String newJwtToken = null;
        try {
            String vaccineCertString = new String(file.getBytes());
            newJwtToken = vaccinationService.validateVaccination(currentUser, vaccineCertString);
        } catch (VaccinationValidationException e) {
            return new ResponseEntity<>(new VaccinationResponseDTO("Error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        currentUser = userService.unprotectedGetUser(userId);
        if (currentUser.isVaccinated()) {
            return ResponseEntity
                .status(HttpStatus.OK)
                .header("Authorization", newJwtToken)
                .body(
                    new VaccinationResponseDTO("Vaccinated", "Vaccination certified for " + currentUser.getVaccinationName())
                );
        } else {
            return new ResponseEntity<>(new VaccinationResponseDTO("Unvaccinated", "An error occurred and validation failed."), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        
    }

}
