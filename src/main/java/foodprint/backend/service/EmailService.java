package foodprint.backend.service;

import foodprint.backend.exceptions.MailException;
import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailService {

    @Autowired
    UserRepo userRepo;

    @Value("${foodprint.email.address}")
    private String emailSender;

    @Value("${foodprint.email.port}")
    private String emailPort;

    @Value("${foodprint.email.server}")
    private String emailServer;

    @Value("${FOODPRINT_EMAIL_PASSWORD}")
    private String emailPassword;

    private Logger loggr = LoggerFactory.getLogger(EmailService.class);

    public JavaMailSender javaMailSender() {

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(emailServer);
        mailSender.setPort(Integer.parseInt(emailPort));
        mailSender.setUsername(emailSender);
        mailSender.setPassword(emailPassword);
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", true);
        
        return mailSender;
    }

    
    /**
     * Wrapper function for sending an email to a Foodprint user
     * @param recipient
     * @param subject
     * @param content
     */
    public void sendSimpleEmail(User recipient, String subject, String content) {
        String email = recipient.getEmail();
        sendSimpleEmail(email, subject, content);
    }

    
    /**
     * Actual function for sending emails
     * @param recipientEmail
     * @param subject
     * @param content
     */
    private void sendSimpleEmail(String recipientEmail, String subject, String content) {

        if (javaMailSender() == null) {
            loggr.info("Mail sender is null, cannot continue...");
        }
        
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("cs203foodprint@gmail.com");
        message.setTo(recipientEmail); 
        message.setSubject(subject); 
        message.setText(content);

        try {
            javaMailSender().send(message);
        } catch (MailParseException e) {
            loggr.info("An error occurred while parsing email content");
            throw new MailException("An error occurred while parsing email content");
        } catch (MailAuthenticationException e) {
            loggr.info("An error occurred while authenticating mail server credentials"); 
            throw new MailException("An error occurred while authenticating mail server credentials");
        } catch (MailSendException e) {
            loggr.info("An error occurred while trying to send the email");
            throw new MailException("An error occurred while trying to send the email");
        } catch (NullPointerException e) {
            loggr.info("Mail sender was uninitialized or null");
            throw new MailException("Mail sender was uninitialized or null");
        }
    }
}
