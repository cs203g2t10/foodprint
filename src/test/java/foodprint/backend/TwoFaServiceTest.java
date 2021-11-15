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
    void checkEmailHas2FA_EmailHas2FA_ReturnTrue() {
        user.setTwoFaSecret("validSecret");
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));

        boolean rslt = twoFaService.checkEmailHas2FA(user.getEmail());

        assertTrue(rslt);
        verify(users).findByEmail(user.getEmail());
    }

    @Test
    void checkEmailHas2FA_EmailHasNo2FA_ReturnFalse() {
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));

        boolean rslt = twoFaService.checkEmailHas2FA(user.getEmail());

        assertFalse(rslt);
        verify(users).findByEmail(user.getEmail());
    }

    @Test
    void checkEmailHas2FA_EmailDoesNotExist_ReturnFalse() {
        when(users.findByEmail(any(String.class))).thenReturn(Optional.empty());

        boolean rslt = twoFaService.checkEmailHas2FA(user.getEmail());

        assertFalse(rslt);
        verify(users).findByEmail(user.getEmail());
    }

    @Test
    void generateQRUrl_Success_ReturnURL() {
        String email = "valid@email.com";
        String secret = "secret";

        String url = twoFaService.generateQRUrl(email, secret);

        assertNotNull(url);
    }

    @Test
    void setup_2faNotSet_returnUrl() {
        String email = user.getEmail();
        user.setTwoFaSet(false);
        when(principal.getName()).thenReturn(email);
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        when(users.saveAndFlush(any(User.class))).thenReturn(user);

        String errorMsg = "";
        try {
            twoFaService.setup(principal);
        } catch (InvalidException e) {
            errorMsg = e.getMessage();
        }

        assertEquals("", errorMsg);
        verify(principal).getName();
        verify(users).findByEmail(user.getEmail());
        verify(users).saveAndFlush(user);
    }

    @Test
    void setup_2faSet_returnException() {
        String email = user.getEmail();
        user.setTwoFaSet(true);
        when(principal.getName()).thenReturn(email);
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));

        String errorMsg = "";
        try {
            twoFaService.setup(principal);
        } catch (InvalidException e) {
            errorMsg = e.getMessage();
        }

        assertEquals("2FA already enabled.", errorMsg);
        verify(principal).getName();
        verify(users).findByEmail(user.getEmail());
    }

    @Test
    void confirm_ValidToken_Return() {
        String email = user.getEmail();
        String secret = "6jm7n6xwitpjooh7ihewyyzeux7aqmw2";
        Totp totp = new Totp(secret);
        String token = totp.now();
        user.setTwoFaSet(false);
        user.setTwoFaSecret(secret);
        when(principal.getName()).thenReturn(email);
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        when(users.saveAndFlush(any(User.class))).thenReturn(user);
        
        String errorMsg ="";
        try {
            twoFaService.confirm(token, principal);
        } catch (InvalidException e) {
            errorMsg = e.getMessage();
        }

        assertEquals("", errorMsg);
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
        
        String errorMsg ="";
        try {
            twoFaService.confirm(token, principal);
        } catch (InvalidException e) {
            errorMsg = e.getMessage();
        }

        assertEquals("Incorrect token format.", errorMsg);
        verify(principal).getName();
        verify(users).findByEmail(user.getEmail());
    }

    @Test
    void confirm_TwoFaEnabledAlready_ReturnException() {
        String email = user.getEmail();
        String secret = "6jm7n6xwitpjooh7ihewyyzeux7aqmw2";
        Totp totp = new Totp(secret);
        String token = totp.now();
        user.setTwoFaSet(true);
        user.setTwoFaSecret(secret);
        when(principal.getName()).thenReturn(email);
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        
        String errorMsg ="";
        try {
            twoFaService.confirm(token, principal);
        } catch (InvalidException e) {
            errorMsg = e.getMessage();
        }

        assertEquals("2FA already enabled.", errorMsg);
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
        
        String errorMsg ="";
        try {
            twoFaService.confirm(token, principal);
        } catch (InvalidException e) {
            errorMsg = e.getMessage();
        }

        assertEquals("Incorrect OTP entered, please restart the setup.", errorMsg);
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
        
        String errorMsg ="";
        try {
            twoFaService.confirm(token, principal);
        } catch (InvalidException e) {
            errorMsg = e.getMessage();
        }

        assertEquals("Something went wrong, please try again.", errorMsg);
        verify(principal).getName();
        verify(users).findByEmail(user.getEmail());
    }

    @Test
    void disable_ValidToken_Return() {
        String email = user.getEmail();
        String secret = "6jm7n6xwitpjooh7ihewyyzeux7aqmw2";
        Totp totp = new Totp(secret);
        String token = totp.now();
        user.setTwoFaSet(true);
        user.setTwoFaSecret(secret);
        when(principal.getName()).thenReturn(email);
        when(users.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        when(users.saveAndFlush(any(User.class))).thenReturn(user);
        
        String errorMsg ="";
        try {
            twoFaService.disable(token, principal);
        } catch (InvalidException e) {
            errorMsg = e.getMessage();
        }

        assertEquals("", errorMsg);
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
        
        String errorMsg ="";
        try {
            twoFaService.disable(token, principal);
        } catch (InvalidException e) {
            errorMsg = e.getMessage();
        }

        assertEquals("Incorrect token format.", errorMsg);
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
        
        String errorMsg ="";
        try {
            twoFaService.disable(token, principal);
        } catch (InvalidException e) {
            errorMsg = e.getMessage();
        }

        assertEquals("2FA not yet set.", errorMsg);
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
        
        String errorMsg ="";
        try {
            twoFaService.disable(token, principal);
        } catch (InvalidException e) {
            errorMsg = e.getMessage();
        }

        assertEquals("Incorrect OTP entered, please restart the disabling process.", errorMsg);
        verify(principal).getName();
        verify(users).findByEmail(user.getEmail());
    }
}
