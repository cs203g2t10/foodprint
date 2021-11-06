package foodprint.backend.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import foodprint.backend.exceptions.InvalidException;
import foodprint.backend.model.UserRepo;
import foodprint.backend.service.TwoFaService;
import foodprint.backend.service.UserService;

@RestController
@RequestMapping("/api/v1/twofactor")
public class TwoFaController {

    UserRepo userRepo;

    UserService userService;

    TwoFaService twoFaService;

    @Autowired
    TwoFaController(UserRepo userRepo, UserService userService, TwoFaService twoFaService) {
        this.userRepo = userRepo;
        this.userService = userService;
        this.twoFaService = twoFaService;
    }
    
    @GetMapping({"/enable"})
    public ResponseEntity<String> twoFactorEnable(Principal principal) {
        try {
            return new ResponseEntity<>(twoFaService.setup(principal), HttpStatus.OK);
        } catch (InvalidException e) {
            return new ResponseEntity<>("2FA already enabled.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    

    @PostMapping({ "/confirm/{token}"})
    public ResponseEntity<String> twoFactorConfirm(@PathVariable("token") String token, Principal principal) {
        try {
            twoFaService.confirm(token, principal);
            return new ResponseEntity<>("2FA successfully enabled.", HttpStatus.OK);
        } catch (InvalidException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/disable/{token}")
    public ResponseEntity<String> twoFactorDisable(@PathVariable("token") String token, Principal principal) {
        try {
            twoFaService.disable(token, principal);
            return new ResponseEntity<>("2FA successfully disabled.", HttpStatus.OK);
        } catch (InvalidException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
     
    }
}
