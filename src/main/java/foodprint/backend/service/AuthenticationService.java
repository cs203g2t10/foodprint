package foodprint.backend.service;

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
import foodprint.backend.model.Token;
import foodprint.backend.model.TokenRepo;
import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;

@Service
public class AuthenticationService {

    private UserDetailsService userDetailsService;
    
    private PasswordEncoder passwordEncoder;

    private TokenRepo tokenRepo;

    private EmailService emailService;

    private UserRepo userRepo;

    @Autowired
    AuthenticationService(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder, TokenRepo tokenRepo, EmailService emailService, UserRepo userRepo) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepo = tokenRepo;
        this.emailService = emailService;
        this.userRepo = userRepo;
    }

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

    public void emailConfirmation(User user) {
        //generate email token
        Token confirmToken = new Token(Token.EMAIL_CONFIRMATION_TOKEN, user);

        tokenRepo.save(confirmToken);
        
        String emailBody = String.format(
            "Hi %s, \n\n" +    
            "Thank you for registering with Foodprint, you can confirm your email at http://foodprint.works/verifyaccount?token=%s \n\n" +
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

    public void confirmRegistration(String tok) {
        Token token = tokenRepo.findByToken(tok);
        User requestor = token.getRequestor();

        if (!token.isValid() || token.getType() != Token.EMAIL_CONFIRMATION_TOKEN) {
            throw new RegistrationException("Invalid token");
        }

        if (requestor == null) {
            throw new RegistrationException("Requestor not found");
        }

        requestor.setRoles("FP_USER");
        token.setUsed(true);
        userRepo.saveAndFlush(requestor);
        tokenRepo.saveAndFlush(token);
    }

 

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
