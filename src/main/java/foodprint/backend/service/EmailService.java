package foodprint.backend.service;

import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    ServerService serverService;

    private Logger logger = LoggerFactory.getLogger(EmailService.class);

    public JavaMailSender javaMailSender() {

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(serverService.getEmailServer());
        mailSender.setPort(Integer.parseInt(serverService.getEmailPort()));
        mailSender.setUsername(serverService.getEmailSender());
        mailSender.setPassword(serverService.getEmailPassword());
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", true);
        
        return mailSender;
    }

    public String foodprintHostname() {
        return serverService.getFoodprintHostname();
    }

    public boolean sendSimpleEmail(User recipient, String subject, String content) {
        String email = recipient.getEmail();
        return sendSimpleEmail(email, subject, content);
    }

    public boolean sendSimpleEmail(String recipientEmail, String subject, String content) {

        if (javaMailSender() == null) {
            logger.info("Mail sender is null, cannot continue...");
            return false;
        }
        
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("cs203foodprint@gmail.com");
        message.setTo(recipientEmail); 
        message.setSubject(subject); 
        message.setText(content);

        try {

            javaMailSender().send(message);

        } catch (MailParseException e) {

            logger.info("An error occurred while parsing email content");
            e.printStackTrace();
            return false;

        } catch (MailAuthenticationException e) {

            logger.info("An error occurred while authenticating mail server credentials");
            e.printStackTrace();
            return false;

        } catch (MailSendException e) {

            logger.info("An error occurred while trying to send the email.");
            e.printStackTrace();
            return false;

        } catch (NullPointerException e) {

            logger.info("Mail sender was uninitialized or null");
            e.printStackTrace();
            return false;

        }
        
        return true;
    }
}
