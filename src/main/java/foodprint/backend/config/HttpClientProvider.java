package foodprint.backend.config;

import java.net.http.HttpClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientProvider {
    
    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

}
