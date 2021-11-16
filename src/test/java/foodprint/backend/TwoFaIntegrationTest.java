package foodprint.backend;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import javax.persistence.DiscriminatorValue;

import org.jboss.aerogear.security.otp.Totp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import foodprint.backend.dto.AuthRequestDTO;
import foodprint.backend.dto.AuthResponseDTO;
import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;

import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DiscriminatorValue( "null" )
public class TwoFaIntegrationTest {
    @LocalServerPort
	private int port;
    
    private final String baseUrl = "http://localhost:";

    @Autowired
    private UserRepo userRepo;

    private User newUser;
    
    HttpHeaders headers = new HttpHeaders();
    TestRestTemplate testRestTemplate = new TestRestTemplate();
    
    @BeforeEach
    void createUser() {
        Optional<User> user = userRepo.findByEmail("bobby@user.com");
        if (!user.isEmpty()) {
            userRepo.delete(user.get());
        }
        String encodedPassword = new BCryptPasswordEncoder().encode("SuperSecurePassw0rd");
        newUser = new User("bobby@user.com", encodedPassword, "bobby");
        newUser.setRoles("FP_USER");
        newUser.setTwoFaSecret( "6jm7n6xwitpjooh7ihewyyzeux7aqmw2");
        newUser.setRegisteredOn(LocalDateTime.now());
        userRepo.saveAndFlush(newUser);
    }

    @AfterEach
    void tearDown() {
        userRepo.deleteAll();
    }
     /**
     * Testing for when users are setting up 2FA.
     * Returns url of QR code
     */
    @Test
    public void twoFactorEnable_Successful() {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@user.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        System.out.println(loginResponse.getStatus());
        headers.add("Content-Type", "application/json");
        
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(newUser.getEmail(), newUser.getPassword());

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/twofactor/enable"),
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class, 
                token.getPrincipal());
        
        assertEquals(200, responseEntity.getStatusCode().value());
        assertTrue(responseEntity.getBody().contains("otpauth"));
    }

     /**
     * Testing for when users try to set up 2FA when they already have 2FA enabled.
     * Throws 500
     */
    @Test
    public void twoFactorEnable_TwoFaAlreadyEnabled_Failure() {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@user.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        System.out.println(loginResponse.getStatus());
        headers.add("Content-Type", "application/json");
        newUser.setTwoFaSet(true);
        userRepo.saveAndFlush(newUser);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(newUser.getEmail(), newUser.getPassword());

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/twofactor/enable"),
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class, 
                token.getPrincipal());
        
        assertEquals(500, responseEntity.getStatusCode().value());
        assertEquals("2FA already enabled.", responseEntity.getBody());
    }

     /**
     * Testing for when users try to confirm 2FA using a OTP.
     */
    @Test
    public void twoFactorConfirm_Successful() {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@user.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        System.out.println(loginResponse.getStatus());
        headers.add("Content-Type", "application/json");
        Totp totp = new Totp(newUser.getTwoFaSecret());
        String tok = totp.now();

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(newUser.getEmail(), newUser.getPassword());

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/twofactor/confirm/{token}"),
                HttpMethod.POST,
                new HttpEntity<Object>(headers),
                String.class, 
                tok,
                token.getPrincipal());
        
        assertEquals(200, responseEntity.getStatusCode().value());
        assertEquals("2FA successfully enabled.", responseEntity.getBody());

    }

     /**
     * Testing for when users try to confirm 2FA using an invalid OTP.
     */
    @Test
    public void twoFactorConfirm_InvalidToken_Failure() {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@user.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        System.out.println(loginResponse.getStatus());
        headers.add("Content-Type", "application/json");
        String tok = "12d";

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(newUser.getEmail(), newUser.getPassword());

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/twofactor/confirm/{token}"),
                HttpMethod.POST,
                new HttpEntity<Object>(headers),
                String.class, 
                tok,
                token.getPrincipal());
        
        assertEquals(500, responseEntity.getStatusCode().value());
        assertEquals("Incorrect token format.", responseEntity.getBody());
    }
    
    /**
     * Testing for when users try to disable 2FA using an OTP.
     */
    @Test
    public void twoFactorDisable_Success() {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@user.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        System.out.println(loginResponse.getStatus());
        headers.add("Content-Type", "application/json");
        Totp totp = new Totp(newUser.getTwoFaSecret());
        String tok = totp.now();
        newUser.setTwoFaSet(true);
        userRepo.saveAndFlush(newUser);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(newUser.getEmail(), newUser.getPassword());

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/twofactor/disable/{token}"),
                HttpMethod.POST,
                new HttpEntity<Object>(headers),
                String.class, 
                tok,
                token.getPrincipal());
        
        assertEquals(200, responseEntity.getStatusCode().value());
        assertEquals("2FA successfully disabled.", responseEntity.getBody());

    }
    /**
     * Testing for when users try to disable 2FA using an invalid OTP.
     */
    @Test
    public void twoFactorDisable_Failure() {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("bobby@user.com");
        loginRequest.setPassword("SuperSecurePassw0rd");
        AuthResponseDTO loginResponse = testRestTemplate.postForObject(createURLWithPort("/api/v1/auth/login"), loginRequest, AuthResponseDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + loginResponse.getToken());
        System.out.println(loginResponse.getStatus());
        headers.add("Content-Type", "application/json");
        String tok = "12d";

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(newUser.getEmail(), newUser.getPassword());

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/api/v1/twofactor/disable/{token}"),
                HttpMethod.POST,
                new HttpEntity<Object>(headers),
                String.class, 
                tok,
                token.getPrincipal());
        
        assertEquals(500, responseEntity.getStatusCode().value());
        assertEquals("Incorrect token format.", responseEntity.getBody());
    }

    private String createURLWithPort(String uri) {
        return baseUrl + port + uri;
    }
}
