package foodprint.backend.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.Optional;

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

    private static Logger LOGGER = LoggerFactory.getLogger(TwoFaService.class);
    
    public static final String QR_PREFIX = "https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=";
    public int TOKEN_LENGTH = 6;

    public boolean checkEmailHas2FA(String email) {

        Optional<User> optUser = userRepo.findByEmail(email);

        if (optUser.isEmpty()) {
            return false;
        }
        User user = optUser.get();

        if (user.getTwoFaSecret() == null || user.getTwoFaSecret().equals("")) {
            return false;
        }

        return true;
    }

    public String generateQRUrl(String userEmail, String twoFaSecret) {
        try {

            return QR_PREFIX + URLEncoder.encode(
                String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", 
                    "foodprint", userEmail, twoFaSecret, "foodprint"),
                "UTF-8");

        } catch (UnsupportedEncodingException e) {
            LOGGER.error("UnsupportedEncodingException", e);
        }

        return null;

    }

    public String generateSecret() {
        String secret = Base32.random();
        return secret;
    }

    public boolean validate(String secret, String token) {
        Totp otp = new Totp(secret);
        return otp.verify(token);
    }

    public String setup(Principal principal) {
        String email = principal.getName();
        User user = userRepo.findByEmail(email).orElseThrow(() ->  new NotFoundException("User not found"));

        if (user.isTwoFaSet()) {
            throw new InvalidException("2FA already enabled.");
        }

        String secret = generateSecret();
        user.setTwoFaSecret(secret);
        userRepo.saveAndFlush(user);
        
        String qrUrl = generateQRUrl(email, secret);
        return qrUrl;
    }

    public void confirm(String token, Principal principal) {
        String email = principal.getName();
        User user = userRepo.findByEmail(email).orElseThrow(() ->  new NotFoundException("User not found"));
        String twoFaSecret = user.getTwoFaSecret();

        if (!validToken(token)) {
            throw new InvalidException("Incorrect token format.");
        }
        
        if (user.isTwoFaSet()) {
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
        User user = userRepo.findByEmail(email).orElseThrow(() ->  new NotFoundException("User not found"));
        String twoFaSecret = user.getTwoFaSecret();

        if (!validToken(token)) {
            throw new InvalidException("Incorrect token format.");
        }

        if (!user.isTwoFaSet()) {
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
        if (token.length() != TOKEN_LENGTH || !token.matches("[0-9]+")) {
            return false;
        }
        return true;
    }
}
