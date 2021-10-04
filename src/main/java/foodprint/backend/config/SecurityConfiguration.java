package foodprint.backend.config;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@EnableGlobalMethodSecurity(
  prePostEnabled = true, 
  securedEnabled = true, 
  jsr250Enabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private UserRepo userRepo;
    private JwtTokenFilter jwtTokenFilter;

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder(13);
    }
    
    @Autowired
    public SecurityConfiguration(UserRepo userRepo, JwtTokenFilter jwtTokenFilter) {
        this.userRepo = userRepo;
        this.jwtTokenFilter = jwtTokenFilter;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(
            email -> userRepo.findByEmail(email).orElseThrow()
        );
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Enable CORS and disable CSRF
        http = http.cors().and().csrf().disable();

        http.headers().frameOptions().sameOrigin();

        // Set session management to stateless
        http = http
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and();

        // Set unauthorized requests exception handler
        http = http.exceptionHandling()
            .authenticationEntryPoint(
                (request, response, ex) -> {
                    response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        ex.getMessage()
                    );
                }
            )
            .and();

        // Set permissions on endpoints
        http.authorizeRequests()
        
            // Our public endpoints
            .antMatchers(HttpMethod.GET, 
                "/api/v1/restaurant/*/food",
                "/api/v1/restaurant/*",
                "/api/v1/restaurant"
            ).permitAll()

            .antMatchers(
                "/api/v1/auth/login",
                "/api/v1/auth/register",
                "/api/v1/user"
            ).permitAll()

            .antMatchers(
                "/swagger/**",
                "/swagger-ui/**",
                "/swagger-ui/index.html",
                "/v3/api-docs/**",
                "/h2-console/**",
                "/favicon.ico"
            ).permitAll()

            // Our private endpoints
            .anyRequest().authenticated()
            .and();

            // Add JWT token filter
            http.addFilterBefore(
                jwtTokenFilter,
                UsernamePasswordAuthenticationFilter.class
            );
    }

        // Used by spring security if CORS is enabled.
        // @Bean
        // public CorsFilter corsFilter() {
        //     UrlBasedCorsConfigurationSource source =
        //         new UrlBasedCorsConfigurationSource();
        //     CorsConfiguration config = new CorsConfiguration();
        //     config.setAllowCredentials(true);
        //     config.addAllowedOrigin("*");
        //     config.addAllowedHeader("*");
        //     config.addAllowedMethod("*");
        //     source.registerCorsConfiguration("/**", config);
        //     return new CorsFilter(source);
        // }
    
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            final CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOrigins(List.of("http://localhost:3000", "https://foodprint.works", "https://api.foodprint.works"));
            configuration.setAllowedMethods(List.of("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH"));
            // setAllowCredentials(true) is important, otherwise:
            // The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard '*' when the request's credentials mode is 'include'.
            configuration.setAllowCredentials(true);
            // setAllowedHeaders is important! Without it, OPTIONS preflight request
            // will fail with 403 Invalid CORS request
            configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
            final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            return source;
        }
        
}
