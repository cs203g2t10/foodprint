package foodprint.backend.dto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@PropertySources({
    @PropertySource("file:secrets.properties")
})
public class ServerDetailsDTO {

    @Value("${foodprint.email.address}")
    private String emailSender;

    @Value("${foodprint.email.port}")
    private String emailPort;

    @Value("${foodprint.email.server}")
    private String emailServer;

    @Value("${foodprint.email.password}")
    private String emailPassword;

    public ServerDetailsDTO() {}

    public String getEmailSender() {
        return this.emailSender;
    }

    public void setEmailSender(String emailSender) {
        this.emailSender = emailSender;
    }

    public String getEmailPort() {
        return this.emailPort;
    }

    public void setEmailPort(String emailPort) {
        this.emailPort = emailPort;
    }

    public String getEmailServer() {
        return this.emailServer;
    }

    public void setEmailServer(String emailServer) {
        this.emailServer = emailServer;
    }

    public String getEmailPassword() {
        return this.emailPassword;
    }

    public void setEmailPassword(String emailPassword) {
        this.emailPassword = emailPassword;
    }

    public ServerDetailsDTO emailSender(String emailSender) {
        setEmailSender(emailSender);
        return this;
    }

    public ServerDetailsDTO emailPort(String emailPort) {
        setEmailPort(emailPort);
        return this;
    }

    public ServerDetailsDTO emailServer(String emailServer) {
        setEmailServer(emailServer);
        return this;
    }

    public ServerDetailsDTO emailPassword(String emailPassword) {
        setEmailPassword(emailPassword);
        return this;
    }

}
