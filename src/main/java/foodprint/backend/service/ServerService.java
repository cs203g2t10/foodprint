package foodprint.backend.service;

import foodprint.backend.dto.ServerDetailsDTO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Service;

@Service("serverService")
@PropertySources({
    @PropertySource("file:application.properties")
})
public class ServerService {

    @Value("${foodprint.hostname}")
    private String foodprintHostname;

    @Value("${foodprint.email.address}")
    private String emailSender;

    @Value("${foodprint.email.port}")
    private String emailPort;

    @Value("${foodprint.email.server}")
    private String emailServer;

    @Value("${foodprint.email.password}")
    private String emailPassword;

    @Value("${foodprint.bypasslogin.allowed:false}")
    private boolean bypassLogin;

    public ServerDetailsDTO get() {
        ServerDetailsDTO dto = new ServerDetailsDTO();
        dto.emailPassword(emailPassword)
            .emailPort(emailPort)
            .emailServer(emailServer)
            .emailSender(emailSender);
        return dto;
    }    

    public void set(ServerDetailsDTO dto) {
        this.emailPort = dto.getEmailPort();
        this.emailServer = dto.getEmailServer();
        this.emailSender = dto.getEmailSender();
        this.emailPassword = dto.getEmailPassword();
    }

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

    public String getFoodprintHostname() {
        return this.foodprintHostname;
    }

    public void setFoodprintHostname(String foodprintHostname) {
        this.foodprintHostname = foodprintHostname;
    }

    public boolean isBypassLogin() {
        return this.bypassLogin;
    }

    public boolean getBypassLogin() {
        return this.bypassLogin;
    }
}