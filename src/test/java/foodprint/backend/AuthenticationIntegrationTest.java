package foodprint.backend;

import foodprint.backend.dto.AuthRequestDTO;
import foodprint.backend.dto.AuthResponseDTO;
import foodprint.backend.dto.CurrentUserDetailsDTO;
import foodprint.backend.dto.RegRequestDTO;
import foodprint.backend.model.Token;
import foodprint.backend.model.TokenRepo;
import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
            "foodprint.email.server=smtp.mailtrap.io",
            "foodprint.email.port=587",
            "foodprint.email.address=d50679dc6cc279",
            "FOODPRINT_EMAIL_PASSWORD=eb29812e6ead51"
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

    private String verifyTokenString = "";

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
        Token verifyToken = new Token(Token.EMAIL_CONFIRMATION_TOKEN, unverifiedUser);
        tokenRepo.saveAndFlush(verifyToken);
        verifyTokenString = verifyToken.getToken();

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

        assertEquals("USER_UNVERIFIED", responseEntity.getBody().getStatus());
        assertEquals(401, responseEntity.getStatusCode().value());
    }

    @Test
    public void login_CorrectPasswordCorrectOTP_Success() throws Exception {
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
    public void login_CorrectPasswordWrongOTP_IncorrectOTP() throws Exception {
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

    @Test
    public void register_Valid_Success() throws Exception {
        // Try signing up
        RegRequestDTO regRequest = new RegRequestDTO();
        regRequest.setFirstName("Bobby");
        regRequest.setLastName("NewUser");
        regRequest.setEmail("bobby@newuser.com");
        regRequest.setPassword("SuperSecurePassw0rd");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegRequestDTO> entity = new HttpEntity<>(regRequest, headers);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/api/v1/auth/register"),
            HttpMethod.POST, entity, Void.class
        );

        assertEquals(200, responseEntity.getStatusCode().value());
        
        // Now try logging in with created user
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@newuser.com");
        loginRequest.setPassword("SuperSecurePassw0rd");

        HttpEntity<AuthRequestDTO> entityLogin = new HttpEntity<>(loginRequest, headers);
        ResponseEntity<AuthResponseDTO> responseEntityLogin = testRestTemplate.exchange(
                createURLWithPort("/api/v1/auth/login"),
                HttpMethod.POST, entityLogin, AuthResponseDTO.class
            );

        assertEquals("USER_UNVERIFIED", responseEntityLogin.getBody().getStatus());
        assertEquals(401, responseEntityLogin.getStatusCode().value());
    }

    @Test
    public void register_InvalidEmail_Error() throws Exception {
        // Try signing up
        RegRequestDTO regRequest = new RegRequestDTO();
        regRequest.setFirstName("Bobby");
        regRequest.setLastName("NewUser");
        regRequest.setEmail("bobby.com");
        regRequest.setPassword("SuperSecurePassw0rd");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegRequestDTO> entity = new HttpEntity<>(regRequest, headers);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/api/v1/auth/register"),
            HttpMethod.POST, entity, Void.class
        );

        assertEquals(400, responseEntity.getStatusCode().value());
    }

    @Test
    public void register_InsecurePassword_Error() throws Exception {
        // Try signing up
        RegRequestDTO regRequest = new RegRequestDTO();
        regRequest.setFirstName("Bobby");
        regRequest.setLastName("NewUser");
        regRequest.setEmail("bobby@newuser.com");
        regRequest.setPassword("abcde");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegRequestDTO> entity = new HttpEntity<>(regRequest, headers);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/api/v1/auth/register"),
            HttpMethod.POST, entity, Void.class
        );

        assertEquals(400, responseEntity.getStatusCode().value());
    }

    @Test
    public void whoAmI_LoggedIn_Success() throws Exception {
        // Use existing user to login
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@normaluser.com");
        loginRequest.setPassword("SuperSecurePassw0rd");

        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<AuthRequestDTO> loginEntity = new HttpEntity<>(loginRequest, loginHeaders);
        ResponseEntity<AuthResponseDTO> loginResponseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/auth/login"),
                HttpMethod.POST, loginEntity, AuthResponseDTO.class
            );

        String jwtToken = loginResponseEntity.getBody().getToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<RegRequestDTO> entity = new HttpEntity<>(headers);
        ResponseEntity<CurrentUserDetailsDTO> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/api/v1/auth/whoami"),
            HttpMethod.GET, entity, CurrentUserDetailsDTO.class
        );

        assertEquals(200, responseEntity.getStatusCode().value());
        assertEquals("bobby@normaluser.com", responseEntity.getBody().getEmail());
    }

    
    @Test
    public void whoAmI_NotLoggedIn_Unauthorized() throws Exception {
        
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<RegRequestDTO> entity = new HttpEntity<>(headers);
        ResponseEntity<CurrentUserDetailsDTO> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/api/v1/auth/whoami"),
            HttpMethod.GET, entity, CurrentUserDetailsDTO.class
        );

        assertEquals(401, responseEntity.getStatusCode().value());
    }

    @Test
    public void checkUser2FA_UserHas2FA_ReturnTrue() throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Boolean> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/api/v1/auth/checkUser2FA/bobby@twofauser.com"),
            HttpMethod.GET, entity, Boolean.class
        );

        assertEquals(200, responseEntity.getStatusCode().value());
        assertTrue(responseEntity.getBody());

    }

    @Test
    public void checkUser2FA_UserDoesNotHave2FA_ReturnFalse() throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Boolean> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/api/v1/auth/checkUser2FA/bobby@normaluser.com"),
            HttpMethod.GET, entity, Boolean.class
        );

        assertEquals(200, responseEntity.getStatusCode().value());
        assertFalse(responseEntity.getBody());
        
    }

    @Test
    public void confirmEmail_TokenCorrect_Success() throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/api/v1/auth/register/confirm/" + verifyTokenString),
            HttpMethod.GET, entity, Void.class
        );

        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void confirmEmail_TokenIncorrect_Failure() throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/api/v1/auth/register/confirm/clearlyNotAToken"),
            HttpMethod.GET, entity, Void.class
        );

        assertEquals(404, responseEntity.getStatusCode().value());
    }
    
    private String createURLWithPort(String uri)
    {
        return baseUrl + port + uri;
    }
}
