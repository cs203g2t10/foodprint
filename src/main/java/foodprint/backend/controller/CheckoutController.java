package foodprint.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import foodprint.backend.dto.CheckoutDTO;
import foodprint.backend.model.Reservation;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {
    @Value("${STRIPE_PUBLIC_KEY}")
    private String stripePublicKey;

    @RequestMapping("/checkout")
    public String checkout(Reservation reservation) {
        // model.addAttribute("amount", 50 * 100); // in cents
        // model.addAttribute("stripePublicKey", stripePublicKey);
        // model.addAttribute("currency", ChargeRequest.Currency.EUR);

        return "checkout";
    }




}
