package foodprint.backend.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;
import foodprint.backend.service.TwoFaService;
import foodprint.backend.service.UserService;

@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

    UserRepo userRepo;

    UserService userService;

    TwoFaService twoFaService;

    @Autowired
    SettingsController(UserRepo userRepo, UserService userService, TwoFaService twoFaService) {
        this.userRepo = userRepo;
        this.userService = userService;
        this.twoFaService = twoFaService;
    }
    
    @GetMapping({"/twofactor"})
    public ResponseEntity<TwoFaResponseD twofactor_page(TwoFaRequestDTO req) {

        String email = principal.getName();
        boolean has2FA = twoFaService.checkEmailHas2FA(email);

        if (has2FA) {
            model.addAttribute("lead", "You currently have 2FA already set up! You may disable it here.");

            LoginFormDTO fields = new LoginFormDTO();
            model.addAttribute("fields", fields);
            model.addAttribute("has2FA", has2FA);

        } else {
            model.addAttribute("lead", "Your account does not have 2FA, secure it by setting it up.");

            LoginFormDTO fields = new LoginFormDTO();
            
            // Generate new secret
            String secret = twoFaService.generateSecret();
            fields.setPassword(secret);

            model.addAttribute("fields", fields);
            model.addAttribute("has2FA", has2FA);

            String qrUrl = twoFaService.generateQRUrl(email, secret);
            model.addAttribute("qrcodeUrl", qrUrl);
        }



        return "settings/twofactor";
    }

    

    @PreAuthorize("isAuthenticated()")
    @PostMapping({ "twofactor/setup"})
    public String twofactor_setup(Principal principal) {

        String twoFaSecret = fields.getPassword();

        // getPassword gets the otp secret
        if (twoFaSecret == null || twoFaSecret.equals("")) {
            model.addAttribute("error", "Something went wrong, please try again.");
            return twofactor_page(model, principal);
        }

        boolean setupOtpOk = twoFaService.validate(twoFaSecret, fields.getTwoFaToken());

        if (!setupOtpOk) {
            model.addAttribute("error", "You have entered an incorrect OTP, please restart the setup.");
            return twofactor_page(model, principal);
        }

        User user = userRepo.findByEmail(principal.getName());

        if (user == null) {
            model.addAttribute("error", "Something went wrong, please try again.");
            return twofactor_page(model, principal);
        }

        user.setTwoFaSecret(twoFaSecret);
        userRepo.save(user);
        
        return "redirect:/settings/twofactor";

    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("twofactor/disable")
    public String twofactor_disable(Model model, Principal principal, @ModelAttribute("fields") LoginFormDTO fields) {

        User user = userRepo.findByEmail(principal.getName());

        if (user == null) {
            model.addAttribute("error", "Something went wrong, please try again.");
            return twofactor_page(model, principal);
        }

        String twoFaToken = fields.getTwoFaToken();
        String twoFaSecret = user.getTwoFaSecret();

        if (twoFaSecret == null || twoFaSecret.equals("")) {
            model.addAttribute("error", "Something went wrong, please try again.");
            return twofactor_page(model, principal);
        }

        boolean disableOtpOk = twoFaService.validate(twoFaSecret, twoFaToken);

        if (!disableOtpOk) {
            model.addAttribute("error", "You have entered an incorrect OTP, please restart the setup.");
            return twofactor_page(model, principal);
        }

        user.setTwoFaSecret(null);
        userRepo.save(user);

        return "redirect:/settings/twofactor";
    }
}
