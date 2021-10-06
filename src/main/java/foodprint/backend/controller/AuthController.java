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
import foodprint.backend.dto.RegResponseDTO;
import foodprint.backend.exceptions.RegistrationException;
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
            user.setLastLogin(LocalDateTime.now());
            repo.save(user);

            String jwtToken = jwtTokenUtil.generateAccessToken(user);

            AuthResponseDTO responseBody = new AuthResponseDTO();
            responseBody.setStatus("SUCCESS");
            responseBody.setToken(jwtToken);

            return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, jwtToken).body(responseBody);

        } catch (BadCredentialsException ex) {

            AuthResponseDTO responseBody = new AuthResponseDTO();
            responseBody.setStatus("INCORRECT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);

        } catch (UsernameNotFoundException ex) {
        
            AuthResponseDTO responseBody = new AuthResponseDTO();
            responseBody.setStatus("INCORRECT");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);

        } catch (UserUnverifiedException ex) {

            AuthResponseDTO responseBody = new AuthResponseDTO();
            responseBody.setStatus("USER_UNVERIFIED");
            authService.emailConfirmation(repo.findByEmail(req.getEmail()).get());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
            
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Register for a new user account")
    public ResponseEntity<RegResponseDTO> register(@Valid @RequestBody RegRequestDTO request) {
        
        Optional<User> existingUser = repo.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            RegResponseDTO resp = new RegResponseDTO();
            resp.setStatus("EMAILEXISTS");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);
        }
        
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(authService.encodePassword(request.getPassword()));
        // user.setRoles("FP_USER");
        user.setRoles("FP_UNVERIFIED");
        user.setRegisteredOn(LocalDateTime.now());
        user = repo.saveAndFlush(user);

        RegResponseDTO resp = new RegResponseDTO();

        try {
            authService.emailConfirmation(user);
            resp.setStatus("SUCCESS");
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (RegistrationException ex) {
            resp.setStatus("EMAIL_ERROR");
            return new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/register/confirm/{token}")
    public ResponseEntity<RegResponseDTO> confirmEmail(@PathVariable("token") String token) {
        RegResponseDTO resp = new RegResponseDTO();
        try {

            authService.confirmRegistration(token);
            resp.setStatus("SUCCESS");
            return new ResponseEntity<>(resp, HttpStatus.OK);

        } catch (RegistrationException ex) {

            resp.setStatus("VERIFICATION_UNSUCCESSFUL");
            return new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @GetMapping({"/whoami"})
	@ResponseStatus(code = HttpStatus.OK)
	public ResponseEntity<CurrentUserDetailsDTO> currentUserDetails() {
		User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CurrentUserDetailsDTO currentUserDetails = new CurrentUserDetailsDTO();
        currentUserDetails.setEmail(currentUser.getEmail());
        currentUserDetails.setFirstName(currentUser.getFirstName());
        currentUserDetails.setLastName(currentUser.getLastName());
        currentUserDetails.setUserId(currentUser.getId());
		return new ResponseEntity<>(currentUserDetails, HttpStatus.OK);
	}
}
