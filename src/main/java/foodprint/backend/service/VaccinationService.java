package foodprint.backend.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import foodprint.backend.config.JwtTokenUtil;
import foodprint.backend.exceptions.VaccinationValidationException;
import foodprint.backend.model.User;

@Service
public class VaccinationService {
    
    private UserService userService;

    private JwtTokenUtil jwtUtil;

    private HttpClient client;

    private ObjectMapper mapper;
    
    private static Logger loggr = LoggerFactory.getLogger(VaccinationService.class);

    private static final String URL = "https://oa.foodprint.works/";

    @Autowired
    public VaccinationService(UserService userService, JwtTokenUtil jwtUtil, HttpClient client, ObjectMapper objectMapper) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.client = client;
        this.mapper = objectMapper;
    }

    @PreAuthorize("hasAnyAuthority('FP_ADMIN') OR #usrParam.email == authentication.name")
    public String validateVaccination(@Param("usrParam") User user, String oaFileString) {
        
        var request = HttpRequest.newBuilder(URI.create(URL))
            .header("accept", "application/json")
            .header("content-type", "application/json")
            .POST(BodyPublishers.ofString(oaFileString))
            .build();

        HttpResponse<String> response;
        String responseString;
        
        try {
            response = client.send(request, BodyHandlers.ofString());
            responseString = response.body();
        } catch (IOException e) {
            throw new VaccinationValidationException("Unable to validate vaccination status.");
        } catch (InterruptedException e) {
            loggr.error("Unable to validate vaccination status due to server error.");
            Thread.currentThread().interrupt();
            throw new VaccinationValidationException("Unable to validate vaccination status due to server error.");
        }

        JsonNode responseObject = null;

        try {
            responseObject = mapper.readTree(responseString);
        } catch (JsonProcessingException e) {
            throw new VaccinationValidationException("Unable to process vaccination validation response");
        }

        if (!responseObject.has("status")) {
            throw new VaccinationValidationException("Obtaining malformed responses from upstream service");
        }

        String validationStatus = responseObject.get("status").asText();

        if (validationStatus.equals("Valid")) {
            
            String vaccName = responseObject.get("patientName").asText();
            String vaccDob = responseObject.get("patientBirthDate").asText();
            LocalDate vaccDobDate = LocalDate.parse(vaccDob); 

            User newTempUser = new User();
            newTempUser.setVaccinationName(vaccName);
            newTempUser.setVaccinationDob(vaccDobDate);
            userService.updateUser(user.getId(), newTempUser);

            User updatedUser = userService.protectedGetUser(user.getId());
            return jwtUtil.generateAccessToken(updatedUser);

        } else {

            String vaccReason = responseObject.get("reason").asText();
            throw new VaccinationValidationException("Vaccination certificate invalid: " + vaccReason);

        }
    }

}
