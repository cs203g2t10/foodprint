package foodprint.backend.service;

import foodprint.backend.exceptions.AlreadyExistsException;
import foodprint.backend.exceptions.InvalidException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import foodprint.backend.exceptions.MailException;
import foodprint.backend.exceptions.RegistrationException;
import foodprint.backend.exceptions.UserUnverifiedException;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.model.Token;
import foodprint.backend.model.TokenRepo;
import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;

@Service
public class AuthenticationService {

    private static final String EMAIL_REGEX = "^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*(\\+[a-z0-9-]+)?@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{1,6}))?$";

    private UserDetailsService userDetailsService;
    
    private PasswordEncoder passwordEncoder;

    private TokenRepo tokenRepo;

    private EmailService emailService;

    private UserRepo userRepo;

    private TwoFaService twoFaService;

    @Autowired
    AuthenticationService(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder, TokenRepo tokenRepo, EmailService emailService, UserRepo userRepo, TwoFaService twoFaService) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepo = tokenRepo;
        this.emailService = emailService;
        this.userRepo = userRepo;
        this.twoFaService = twoFaService;
    }

    
    /**
     * Validates the authentication token (username and password)
     * @param token
     * @return
     */
    public Authentication authenticate(UsernamePasswordAuthenticationToken token) {

        User user = (User) userDetailsService.loadUserByUsername((String) token.getPrincipal());
        if (user.getRoles().contains("FP_UNVERIFIED")) {
            throw new UserUnverifiedException();
        }

        if (passwordEncoder.matches((String) token.getCredentials(), user.getPassword())) {
            return new UsernamePasswordAuthenticationToken(user, null);
        }

        throw new BadCredentialsException("Incorrect credentials provided");
    }

    /**
     * Registers a given user and triggers a confirmation email send
     * @param user
     */
    public void register(User user) {
        if (userRepo.findByEmail(user.getEmail()).isPresent()) {
            throw new AlreadyExistsException("Registration with this email already exists!");
        }
        user = userRepo.saveAndFlush(user);
        emailConfirmation(user);
    }

    /**
     * Sends a confirmation email for a given user's registration
     * @param user
     */
    public void emailConfirmation(User user) {
        //generate email token
        Token confirmToken = new Token(Token.EMAIL_CONFIRMATION_TOKEN, user);

        tokenRepo.saveAndFlush(confirmToken);
        
        String emailBody = String.format(
            "Hi %s, \n\n" +    
            "Thank you for registering with Foodprint, you can confirm your email at http://ui.foodprint.works/verifyaccount?token=%s \n\n" +
            "This verification token will expire in 48 hours. \n\n" +
            "Regards,\nFoodprint Support",
            user.getFirstName(),
            confirmToken.getToken()
        );
        
        try {
            emailService.sendSimpleEmail(user, "Foodprint Email Verification", emailBody);
        } catch (MailException e){ //rollback when there is an error with sending email
            userRepo.delete(user);
            tokenRepo.delete(confirmToken);
            throw new RegistrationException("An error has occured, please contact us.");
        }
    }

    /**
     * Confirms a registration for a given token
     * @param tok
     */
    public void confirmRegistration(String tok) {
        Token token = tokenRepo.findByToken(tok).orElseThrow(() -> new NotFoundException("Specified token does not exist!"));
        User requestor = token.getRequestor();

        if (!token.isValid() || token.getType() != Token.EMAIL_CONFIRMATION_TOKEN) {
            throw new RegistrationException("Invalid token");
        }

        requestor.setRoles("FP_USER");
        token.setUsed(true);
        userRepo.saveAndFlush(requestor);
        tokenRepo.saveAndFlush(token);
    }

    /**
     * Checks if 2FA is set for a given user
     * @param email
     * @return
     */
    public Boolean check2faSet(String email) {
        if (!email.matches(EMAIL_REGEX)) {
            return false;
        }
        User user = userRepo.findByEmail(email).orElse(null);
        if (user == null) {
            return false;
        }
        return user.isTwoFaSet();
    }

    /**
     * Encodes the password into the Bcrypt form
     * @param password
     * @return
     */
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * Checks if the token is valid for a given user
     * @param token
     * @param user
     */
    public void checkValidToken(String token, User user) {
        if (user.isTwoFaSet() == null || !user.isTwoFaSet()) { //if user doesn't have 2fa set, dont check for valid token (token should be null)
            return;
        }
        if (!twoFaService.validToken(token)) {
            throw new InvalidException("Invalid token format.");
        }
        String twoFaSecret = user.getTwoFaSecret();
        if (!twoFaService.validate(twoFaSecret, token)) {
            throw new InvalidException("Incorrect OTP entered.");
        }
    }
}
