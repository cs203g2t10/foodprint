package foodprint.backend.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;

import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import foodprint.backend.exceptions.InvalidException;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;

@Service
public class TwoFaService {
    @Autowired
    UserRepo userRepo;

    private static Logger loggr = LoggerFactory.getLogger(TwoFaService.class);
    
    public static final String QR_PREFIX = "https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=";
    public static final int TOKEN_LENGTH = 6;

    private static final String USER_NOT_FOUND_MESSAGE = "User not found";

    public String generateQRUrl(String userEmail, String twoFaSecret) {
        try {

            return QR_PREFIX + URLEncoder.encode(
                String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", 
                    "foodprint", userEmail, twoFaSecret, "foodprint"),
                "UTF-8");

        } catch (UnsupportedEncodingException e) {
            loggr.error("UnsupportedEncodingException", e);
        }

        return null;

    }

    public String generateSecret() {
        return Base32.random();
    }

    public boolean validate(String secret, String token) {
        Totp otp = new Totp(secret);
        return otp.verify(token);
    }

    public String setup(Principal principal) {
        String email = principal.getName();
        User user = userRepo.findByEmail(email).orElseThrow(() ->  new NotFoundException(USER_NOT_FOUND_MESSAGE));

        if (Boolean.TRUE.equals(user.isTwoFaSet())) { 
            throw new InvalidException("2FA already enabled.");
        }

        String secret = generateSecret();
        user.setTwoFaSecret(secret);
        userRepo.saveAndFlush(user);
        
        return generateQRUrl(email, secret);
    }

    public void confirm(String token, Principal principal) {
        String email = principal.getName();
        User user = userRepo.findByEmail(email).orElseThrow(() ->  new NotFoundException(USER_NOT_FOUND_MESSAGE));
        String twoFaSecret = user.getTwoFaSecret();

        if (!validToken(token)) {
            throw new InvalidException("Incorrect token format.");
        }
        
        if (Boolean.TRUE.equals(user.isTwoFaSet())) { 
            throw new InvalidException("2FA already enabled.");
        }

        if (twoFaSecret == null || twoFaSecret.equals("")) {
            throw new InvalidException("Something went wrong, please try again.");
        }

        boolean setupOtpOk = validate(twoFaSecret, token);

        if (!setupOtpOk) {
            throw new InvalidException("Incorrect OTP entered, please restart the setup.");
        }

        user.setTwoFaSet(true);
        userRepo.saveAndFlush(user);
    }

    public void disable(String token, Principal principal) {
        String email = principal.getName();
        User user = userRepo.findByEmail(email).orElseThrow(() ->  new NotFoundException(USER_NOT_FOUND_MESSAGE));
        String twoFaSecret = user.getTwoFaSecret();

        if (!validToken(token)) {
            throw new InvalidException("Incorrect token format.");
        }

        if (Boolean.FALSE.equals(user.isTwoFaSet())) { 
            throw new InvalidException("2FA not yet set.");
        } 

        boolean disableOtpOk = validate(twoFaSecret, token);

        if (!disableOtpOk) {
            throw new InvalidException("Incorrect OTP entered, please restart the disabling process.");
        }

        user.setTwoFaSecret(null);
        user.setTwoFaSet(false);
        userRepo.saveAndFlush(user);
    }

    public boolean validToken(String token) {
        return !(token.length() != TOKEN_LENGTH || !token.matches("[0-9]+"));
    }
}
