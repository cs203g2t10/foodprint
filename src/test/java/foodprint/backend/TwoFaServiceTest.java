package foodprint.backend;

import org.jboss.aerogear.security.otp.Totp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Optional;

import org.mockito.junit.jupiter.MockitoExtension;

import foodprint.backend.exceptions.InvalidException;
import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;
import foodprint.backend.service.TwoFaService;

@ExtendWith(MockitoExtension.class)
public class TwoFaServiceTest {
    @Mock
    private UserRepo users;

    @Mock
    private Principal principal;

    @InjectMocks
    private TwoFaService twoFaService;

    private User user;

    @BeforeEach
    void init() {
        user = new User("bobbytan@gmail.com", "SuperSecurePassw0rd", "Bobby Tan");
        user.setRoles("FP_USER");
    }

    @Test
    void generateQRUrl_Success_ReturnURL() {
        String email = "valid@email.com";
        String secret = "secret";

        String url = twoFaService.generateQRUrl(email, secret);

        assertNotNull(url);
    }

    @Test
    void setUp_2FANotSet_ReturnUrl() {
        String email = user.getEmail();
        user.setTwoFaSet(false);
        when(principal.getName()).thenReturn(email);
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        when(users.saveAndFlush(any(User.class))).thenReturn(user);

        String exceptionMsg = "";
        try {
            twoFaService.setup(principal);
        } catch (InvalidException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("", exceptionMsg);
        assertNotNull(user.getTwoFaSecret());
        verify(principal).getName();
        verify(users).findByEmail(user.getEmail());
        verify(users).saveAndFlush(user);
    }

    @Test
    void setUp_2FASet_ReturnException() {
        String email = user.getEmail();
        user.setTwoFaSet(true);
        when(principal.getName()).thenReturn(email);
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));

        String exceptionMsg = "";
        try {
            twoFaService.setup(principal);
        } catch (InvalidException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("2FA already enabled.", exceptionMsg);
        verify(principal).getName();
        verify(users).findByEmail(user.getEmail());
    }

    @Test
    void confirm_ValidToken_Success() {
        String email = user.getEmail();
        String secret = "6jm7n6xwitpjooh7ihewyyzeux7aqmw2";
        Totp totp = new Totp(secret);
        String token = totp.now();
        user.setTwoFaSet(false);
        user.setTwoFaSecret(secret);
        when(principal.getName()).thenReturn(email);
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        when(users.saveAndFlush(any(User.class))).thenReturn(user);
        
        String exceptionMsg ="";
        try {
            twoFaService.confirm(token, principal);
        } catch (InvalidException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("", exceptionMsg);
        assertTrue(user.isTwoFaSet());
        
        verify(principal).getName();
        verify(users).findByEmail(user.getEmail());
        verify(users).saveAndFlush(user);
    }

    @Test
    void confirm_IncorrectTokenFormat_ReturnException() {
        String email = user.getEmail();
        String secret = "6jm7n6xwitpjooh7ihewyyzeux7aqmw2";
        String token = "invalidToken";
        user.setTwoFaSet(false);
        user.setTwoFaSecret(secret);
        when(principal.getName()).thenReturn(email);
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        
        String exceptionMsg ="";
        try {
            twoFaService.confirm(token, principal);
        } catch (InvalidException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Incorrect token format.", exceptionMsg);
        verify(principal).getName();
        verify(users).findByEmail(user.getEmail());
    }

    @Test
    void confirm_TwoFAAlreadyEnabled_ReturnException() {
        String email = user.getEmail();
        String secret = "6jm7n6xwitpjooh7ihewyyzeux7aqmw2";
        Totp totp = new Totp(secret);
        String token = totp.now();
        user.setTwoFaSet(true);
        user.setTwoFaSecret(secret);
        when(principal.getName()).thenReturn(email);
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        
        String exceptionMsg ="";
        try {
            twoFaService.confirm(token, principal);
        } catch (InvalidException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("2FA already enabled.", exceptionMsg);
        verify(principal).getName();
        verify(users).findByEmail(user.getEmail());
    }

    @Test
    void confirm_WrongToken_ReturnException() {
        String email = user.getEmail();
        String secret = "6jm7n6xwitpjooh7ihewyyzeux7aqmw2";
        String token = "123456";
        user.setTwoFaSet(false);
        user.setTwoFaSecret(secret);
        when(principal.getName()).thenReturn(email);
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        
        String exceptionMsg ="";
        try {
            twoFaService.confirm(token, principal);
        } catch (InvalidException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Incorrect OTP entered, please restart the setup.", exceptionMsg);
        verify(principal).getName();
        verify(users).findByEmail(user.getEmail());
    }

    @Test
    void confirm_SecretNull_ReturnException() {
        String email = user.getEmail();
        String secret = null;
        String token = "123456";
        user.setTwoFaSet(false);
        user.setTwoFaSecret(secret);
        when(principal.getName()).thenReturn(email);
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        
        String exceptionMsg ="";
        try {
            twoFaService.confirm(token, principal);
        } catch (InvalidException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Something went wrong, please try again.", exceptionMsg);
        verify(principal).getName();
        verify(users).findByEmail(user.getEmail());
    }

    @Test
    void confirm_SecretEmpty_ReturnException() {
        String email = user.getEmail();
        String secret = "";
        String token = "123456";
        user.setTwoFaSet(false);
        user.setTwoFaSecret(secret);
        when(principal.getName()).thenReturn(email);
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        
        String exceptionMsg ="";
        try {
            twoFaService.confirm(token, principal);
        } catch (InvalidException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Something went wrong, please try again.", exceptionMsg);
        verify(principal).getName();
        verify(users).findByEmail(user.getEmail());
    }

    @Test
    void disable_ValidToken_Success() {
        String email = user.getEmail();
        String secret = "6jm7n6xwitpjooh7ihewyyzeux7aqmw2";
        Totp totp = new Totp(secret);
        String token = totp.now();
        user.setTwoFaSet(true);
        user.setTwoFaSecret(secret);
        when(principal.getName()).thenReturn(email);
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        when(users.saveAndFlush(any(User.class))).thenReturn(user);
        
        String exceptionMsg ="";
        try {
            twoFaService.disable(token, principal);
        } catch (InvalidException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("", exceptionMsg);
        assertNull(user.getTwoFaSecret());
        verify(principal).getName();
        verify(users).findByEmail(user.getEmail());
        verify(users).saveAndFlush(user);
    }

    @Test
    void disable_IncorrectTokenFormat_ReturnException() {
        String email = user.getEmail();
        String secret = "6jm7n6xwitpjooh7ihewyyzeux7aqmw2";
        String token = "invalidToken";
        user.setTwoFaSet(true);
        user.setTwoFaSecret(secret);
        when(principal.getName()).thenReturn(email);
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        
        String exceptionMsg ="";
        try {
            twoFaService.disable(token, principal);
        } catch (InvalidException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Incorrect token format.", exceptionMsg);
        verify(principal).getName();
        verify(users).findByEmail(user.getEmail());
    }

    @Test
    void disable_TwoFaNotYetEnabled_ReturnException() {
        String email = user.getEmail();
        String secret = "6jm7n6xwitpjooh7ihewyyzeux7aqmw2";
        Totp totp = new Totp(secret);
        String token = totp.now();
        user.setTwoFaSet(false);
        user.setTwoFaSecret(secret);
        when(principal.getName()).thenReturn(email);
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        
        String exceptionMsg ="";
        try {
            twoFaService.disable(token, principal);
        } catch (InvalidException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("2FA not yet set.", exceptionMsg);
        verify(principal).getName();
        verify(users).findByEmail(user.getEmail());
    }

    @Test
    void disable_WrongToken_ReturnException() {
        String email = user.getEmail();
        String secret = "6jm7n6xwitpjooh7ihewyyzeux7aqmw2";
        String token = "123456";
        user.setTwoFaSet(true);
        user.setTwoFaSecret(secret);
        when(principal.getName()).thenReturn(email);
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        
        String exceptionMsg ="";
        try {
            twoFaService.disable(token, principal);
        } catch (InvalidException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Incorrect OTP entered, please restart the disabling process.", exceptionMsg);
        verify(principal).getName();
        verify(users).findByEmail(user.getEmail());
    }

    @Test 
    void validToken_CorrectFormat_ReturnTrue() {
        String token = "123456";

        Boolean validToken = twoFaService.validToken(token);

        assertTrue(validToken);
    }

    @Test 
    void validToken_WrongLength_ReturnFalse() {
        String token = "12345";

        Boolean validToken = twoFaService.validToken(token);

        assertFalse(validToken);
    }

    @Test
    void validToken_TokenNotValid_ReturnFalse() {
        String token = "123s45";

        Boolean validToken = twoFaService.validToken(token);

        assertFalse(validToken);
    }
}
