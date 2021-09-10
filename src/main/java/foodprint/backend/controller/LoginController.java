package foodprint.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import foodprint.backend.config.JwtTokenUtil;
import foodprint.backend.dto.AuthRequest;
import foodprint.backend.dto.AuthResponse;
import foodprint.backend.model.User;
import foodprint.backend.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/login")
public class LoginController {

    private JwtTokenUtil jwtTokenUtil;

    private AuthenticationService authService;

    @Autowired
    public LoginController(JwtTokenUtil jwtTokenUtil, AuthenticationService authService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.authService = authService;
    }

    @PostMapping
    @Operation(summary = "Using the credentials, get a JWT authorization token")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
        try {

            Authentication authenticate = authService.authenticate(
                new UsernamePasswordAuthenticationToken(
                    req.getEmail(), req.getPassword()
                )
            );

            User user = (User) authenticate.getPrincipal();
            String jwtToken = jwtTokenUtil.generateAccessToken(user);

            AuthResponse responseBody = new AuthResponse();
            responseBody.setStatus("SUCCESS");
            responseBody.setToken(jwtToken);

            return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, jwtToken).body(responseBody);

        } catch (BadCredentialsException ex) {
            
            AuthResponse responseBody = new AuthResponse();
            responseBody.setStatus("INCORRECT");
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);

        }
    }

    
}
