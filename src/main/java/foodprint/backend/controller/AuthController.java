package foodprint.backend.controller;

import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import foodprint.backend.config.JwtTokenUtil;
import foodprint.backend.dto.AuthRequestDTO;
import foodprint.backend.dto.AuthResponseDTO;
import foodprint.backend.dto.CurrentUserDetailsDTO;
import foodprint.backend.dto.RegRequestDTO;
import foodprint.backend.exceptions.InvalidException;
import foodprint.backend.exceptions.UserUnverifiedException;
import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;
import foodprint.backend.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

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

    @GetMapping("/checkUser2FA/{email}")
    @Operation(summary = "Using a user's email, check if the user has 2 factor authentication currently enabled")
    public ResponseEntity<Boolean> check2fa(@PathVariable("email") String email) {
        return new ResponseEntity<>(authService.check2faSet(email), HttpStatus.OK);
    }

    @PostMapping("/login")
    @Operation(summary = "Using the credentials, get a JWT authorization token")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO req) {
        try {

            Authentication authenticate = authService.authenticate(
                new UsernamePasswordAuthenticationToken(
                    req.getEmail(), req.getPassword()
                )
            );
            
            User user = (User) authenticate.getPrincipal();
            authService.checkValidToken(req.getToken(), user);
            user.setLastLogin(LocalDateTime.now());
            repo.save(user);

            String jwtToken = jwtTokenUtil.generateAccessToken(user);

            AuthResponseDTO responseBody = new AuthResponseDTO();
            responseBody.setStatus("SUCCESS");
            responseBody.setToken(jwtToken);

            return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, jwtToken).body(responseBody);

        } catch (BadCredentialsException | UsernameNotFoundException ex) {

            AuthResponseDTO responseBody = new AuthResponseDTO();
            responseBody.setStatus("INCORRECT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);

        } catch (UserUnverifiedException ex) {

            AuthResponseDTO responseBody = new AuthResponseDTO();
            responseBody.setStatus("USER_UNVERIFIED");
            repo.findByEmail(req.getEmail()).ifPresent(user -> authService.emailConfirmation(user));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
            
        } catch (InvalidException ex) {

            AuthResponseDTO responseBody = new AuthResponseDTO();
            responseBody.setStatus("INCORRECT OTP");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
            
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Register for a new user account")
    public ResponseEntity<Void> register(@Valid @RequestBody RegRequestDTO request) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(authService.encodePassword(request.getPassword()));
        user.setRoles("FP_UNVERIFIED");
        user.setRegisteredOn(LocalDateTime.now());
        authService.register(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/register/confirm/{token}")
    public ResponseEntity<Void> confirmEmail(@PathVariable("token") String token) {
        authService.confirmRegistration(token);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping({ "/whoami" })
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<CurrentUserDetailsDTO> currentUserDetails() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ModelMapper mapper = new ModelMapper();
        CurrentUserDetailsDTO currentUserDetails = mapper.map(currentUser, CurrentUserDetailsDTO.class);
        currentUserDetails.setUserRoles(currentUser.getRoles().split(","));
        return new ResponseEntity<>(currentUserDetails, HttpStatus.OK);
    }
}
