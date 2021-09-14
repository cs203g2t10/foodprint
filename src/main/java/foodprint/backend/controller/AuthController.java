package foodprint.backend.controller;

import java.time.LocalDateTime;
import java.util.Optional;

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
import foodprint.backend.dto.RegRequest;
import foodprint.backend.dto.RegResponse;
import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;
import foodprint.backend.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private JwtTokenUtil jwtTokenUtil;

    private AuthenticationService authService;

    private UserRepo repo;

    @Autowired
    public AuthController(JwtTokenUtil jwtTokenUtil, AuthenticationService authService, UserRepo userRepo) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.authService = authService;
        this.repo = userRepo;
    }

    @PostMapping("/login")
    @Operation(summary = "Using the credentials, get a JWT authorization token")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
        try {

            Authentication authenticate = authService.authenticate(
                new UsernamePasswordAuthenticationToken(
                    req.getEmail(), req.getPassword()
                )
            );

            User user = (User) authenticate.getPrincipal();
            user.setLastLogin(LocalDateTime.now());
            repo.save(user);

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

    @PostMapping("/register")
    @Operation(summary = "Register for a new user account")
    public ResponseEntity<RegResponse> register(@RequestBody RegRequest request) {
        
        Optional<User> existingUser = repo.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            RegResponse resp = new RegResponse();
            resp.setStatus("EMAILEXISTS");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);
        }
        
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(authService.encodePassword(request.getPassword()));
        user.setRoles("FP_USER");
        user.setRegisteredOn(LocalDateTime.now());
        user = repo.saveAndFlush(user);
        
        RegResponse resp = new RegResponse();
        resp.setStatus("SUCCESS");

        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    
}
