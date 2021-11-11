package foodprint.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.mockito.junit.jupiter.MockitoExtension;

import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;
import foodprint.backend.service.TwoFaService;

@ExtendWith(MockitoExtension.class)
public class TwoFaServiceTest {
    @Mock
    private UserRepo users;

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

}
