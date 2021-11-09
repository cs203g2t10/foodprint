package foodprint.backend.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    private static final String MODULE_NAME = "Foodprint";
    private static final String API_VERSION = "1";

    @Bean
    public OpenAPI openApi() {
      final String securitySchemeName = "jwtAuthentication";
      final String apiTitle = String.format("%s API", StringUtils.capitalize(MODULE_NAME));
      final Server server = new Server();
      server.setUrl("/");
      return new OpenAPI()
          .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
          .components(
                new Components()
                    .addSecuritySchemes(securitySchemeName,
                        new SecurityScheme()
                            .name(securitySchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                  )
          )
          .info(
              new Info()
                .title(apiTitle)
                .version(API_VERSION)
            )
          .servers(List.of(server));
    }
}
