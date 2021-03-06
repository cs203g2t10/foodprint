package foodprint.backend.config;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import foodprint.backend.model.UserRepo;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private UserRepo userRepo;
    private JwtTokenFilter jwtTokenFilter;
    private BypassSecurityFilter bypassSecurityFilter;
    private final Logger loggr = LoggerFactory.getLogger(this.getClass());

    @Value("${security.bypass}")
    private boolean securityBypassed;

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder(13);
    }

    @Autowired
    public SecurityConfiguration(UserRepo userRepo, JwtTokenFilter jwtTokenFilter, @Lazy BypassSecurityFilter bypassSecurityFilter) {
        this.userRepo = userRepo;
        this.jwtTokenFilter = jwtTokenFilter;
        this.bypassSecurityFilter = bypassSecurityFilter;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        auth
            .userDetailsService(email -> userRepo.findByEmail(email).orElseThrow())
            .passwordEncoder(new BCryptPasswordEncoder(13));
            
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Enable CORS and disable CSRF
        http = http.cors().and().csrf().disable();

        http.headers().frameOptions().sameOrigin();

        // Set session management to stateless
        http = http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and();

        // Set unauthorized requests exception handler
        http = http.exceptionHandling().authenticationEntryPoint((request, response, ex) ->
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage())
        ).and();

        if (securityBypassed) {

            loggr.info("SECURITY IS BYPASSED!!!");

            http.authorizeRequests()
                .anyRequest()
                .permitAll();

            http.addFilterBefore(bypassSecurityFilter, UsernamePasswordAuthenticationFilter.class);

        } else {

            // Set permissions on endpoints
            http.authorizeRequests()

            // Our public endpoints
            .antMatchers(HttpMethod.GET,
                    "/api/v1/restaurant/*/food",
                    "/api/v1/restaurant/*",
                    "/api/v1/restaurant",
                    "/api/v1/restaurant/categories/**",
                    "/api/v1/reservation/slots/*")
            .permitAll()

            .antMatchers("/api/v1/auth/login/**",
                    "/api/v1/auth/register",
                    "/api/v1/auth/checkUser2FA/**",
                    "/api/v1/user",
                    "/api/v1/user/auth/**",
                    "/api/v1/auth/register/confirm/*")
            .permitAll()

            .antMatchers("/swagger/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/h2-console/**",
                    "/favicon.ico")
            .permitAll()

            // Our private endpoints
            .anyRequest().authenticated().and();

            // Add JWT token filter
            http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(
                List.of(
                    "http://localhost:3000",
                    "https://foodprint.works",
                    "https://ui.foodprint.works/",
                    "https://rest.foodprint.works/"
                )
            );
        configuration.setAllowedMethods(List.of("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH"));
        // This allow us to expose the headers
        configuration.setExposedHeaders(List.of("Access-Control-Allow-Headers", 
            "Authorization, x-xsrf-token, Access-Control-Allow-Headers, Origin, Accept, X-Requested-With, " +
            "Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
