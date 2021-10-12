package foodprint.backend.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Optional;

import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.beans.factory.annotation.Autowired;

import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;

public class TwoFaService {
    @Autowired
    UserRepo userRepo;

    public static String QR_PREFIX = "https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=";

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
            e.printStackTrace();
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
}
