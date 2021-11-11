package foodprint.backend;

import foodprint.backend.dto.AuthRequestDTO;
import foodprint.backend.dto.AuthResponseDTO;
import foodprint.backend.model.TokenRepo;
import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.Arrays;

import javax.persistence.DiscriminatorValue;

import org.jboss.aerogear.security.otp.Totp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
            "foodprint.email.server=smtp.ethereal.email",
            "foodprint.email.port=587",
            "foodprint.email.address=kianna.larson20@ethereal.email",
            "FOODPRINT_EMAIL_PASSWORD=vZzzeNPqxvDpZfKp9z"
        })
@ActiveProfiles("test")
@DiscriminatorValue( "null" )
public class AuthenticationIntegrationTest {

    @LocalServerPort
	private int port;
    
    private final String baseUrl = "http://localhost:";

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TokenRepo tokenRepo;

    private TestRestTemplate testRestTemplate = new TestRestTemplate();

    private static final String OTP_SECRET = "JBSWY3DPEHPK3PXP";

    @BeforeEach
    void createUser() {
        // Recreate the users
        String encodedPassword = new BCryptPasswordEncoder().encode("SuperSecurePassw0rd");

        User newUser = new User("bobby@normaluser.com", encodedPassword, "bobby");
        newUser.setRoles("FP_USER");
        newUser.setRegisteredOn(LocalDateTime.now());
        userRepo.saveAndFlush(newUser);

        User unverifiedUser = new User("bobby@unverifieduser.com", encodedPassword, "bobby");
        unverifiedUser.setRoles("FP_UNVERIFIED");
        unverifiedUser.setRegisteredOn(LocalDateTime.now());
        unverifiedUser = userRepo.saveAndFlush(unverifiedUser);

        User userWith2FA = new User("bobby@twofauser.com", encodedPassword, "bobby");
        userWith2FA.setRoles("FP_USER");
        userWith2FA.setTwoFaSet(true);
        userWith2FA.setTwoFaSecret("JBSWY3DPEHPK3PXP");
        userWith2FA.setRegisteredOn(LocalDateTime.now());
        userRepo.saveAndFlush(userWith2FA);
    }

    @AfterEach
    void tearDown() {
        tokenRepo.deleteAll();
        userRepo.deleteAll();
    }
    
    @Test
    public void login_Valid_Success() throws Exception {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@normaluser.com");
        loginRequest.setPassword("SuperSecurePassw0rd");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");
        
        HttpEntity<AuthRequestDTO> entity = new HttpEntity<>(loginRequest, headers);
        ResponseEntity<AuthResponseDTO> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/auth/login"),
                HttpMethod.POST, entity, AuthResponseDTO.class
            );

        assertEquals("SUCCESS", responseEntity.getBody().getStatus());
        assertEquals(200, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getHeaders().get("Authorization"));
        assertNotNull(responseEntity.getBody().getToken());
    }

    @Test
    public void login_InvalidPassword_Incorrect() throws Exception {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@normaluser.com");
        loginRequest.setPassword("definitelyWrongPassword");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");
        
        HttpEntity<AuthRequestDTO> entity = new HttpEntity<>(loginRequest, headers);
        ResponseEntity<AuthResponseDTO> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/auth/login"),
                HttpMethod.POST, entity, AuthResponseDTO.class
            );

        assertEquals("INCORRECT", responseEntity.getBody().getStatus());
        assertEquals(401, responseEntity.getStatusCode().value());
    }

    @Test
    public void login_NoSuchEmail_Incorrect() throws Exception {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobbytan@doesnotexist.com");
        loginRequest.setPassword("definitelyWrongPassword");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");
        
        HttpEntity<AuthRequestDTO> entity = new HttpEntity<>(loginRequest, headers);
        ResponseEntity<AuthResponseDTO> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/auth/login"),
                HttpMethod.POST, entity, AuthResponseDTO.class
            );

        assertEquals("INCORRECT", responseEntity.getBody().getStatus());
        assertEquals(401, responseEntity.getStatusCode().value());
    }

    @Test
    public void login_UserUnverified_UserUnverifiedError() throws Exception {      
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@unverifieduser.com");
        loginRequest.setPassword("SuperSecurePassw0rd");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");
        
        HttpEntity<AuthRequestDTO> entity = new HttpEntity<>(loginRequest, headers);
        ResponseEntity<AuthResponseDTO> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/auth/login"),
                HttpMethod.POST, entity, AuthResponseDTO.class
            );

        System.out.println(responseEntity.toString());
        assertEquals("USER_UNVERIFIED", responseEntity.getBody().getStatus());
        assertEquals(401, responseEntity.getStatusCode().value());
    }

    @Test
    public void login_correctPasswordCorrectOtp_Success() throws Exception {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@twofauser.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        Totp otp = new Totp(OTP_SECRET);
        loginRequest.setToken(otp.now());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        // headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");
        
        HttpEntity<AuthRequestDTO> entity = new HttpEntity<>(loginRequest, headers);
        ResponseEntity<AuthResponseDTO> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/auth/login"),
                HttpMethod.POST, entity, AuthResponseDTO.class
            );

        assertEquals("SUCCESS", responseEntity.getBody().getStatus());
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void login_correctPasswordWrongOtp_IncorrectOtp() throws Exception {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@twofauser.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        loginRequest.setToken("000000");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        // headers.add("Authorization", "Bearer " + loginResponse.getToken());
        headers.add("Content-Type", "application/json");
        
        HttpEntity<AuthRequestDTO> entity = new HttpEntity<>(loginRequest, headers);
        ResponseEntity<AuthResponseDTO> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/auth/login"),
                HttpMethod.POST, entity, AuthResponseDTO.class
            );

        assertEquals("INCORRECT OTP", responseEntity.getBody().getStatus());
        assertEquals(401, responseEntity.getStatusCode().value());
    }

    // @Test
    // public void getAllRestaurant_Successful() throws Exception {
    //     AuthRequestDTO loginRequest = new AuthRequestDTO();
    //     loginRequest.setEmail("bobby@gmail.com");
    //     loginRequest.setPassword("SuperSecurePassw0rd");
    //     AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

    //     HttpHeaders headers = new HttpHeaders();
    //     headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    //     headers.add("Authorization", "Bearer " + loginResponse.getToken());
    //     headers.add("Content-Type", "application/json");

    //     List<String> restaurantCategories = new ArrayList<>();
    //     restaurantCategories.add("Japanese");
    //     restaurantCategories.add("Rice");
    //     Restaurant restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
    //     restaurants.saveAndFlush(restaurant);

    //     ResponseEntity<Restaurant[]> responseEntity = testRestTemplate.getForEntity(
    //             createURLWithPort("/api/v1/restaurant"),
    //             Restaurant[].class);
    //     assertEquals(200, responseEntity.getStatusCode().value());
    // }
    
    private String createURLWithPort(String uri)
    {
        return baseUrl + port + uri;
    }
}
