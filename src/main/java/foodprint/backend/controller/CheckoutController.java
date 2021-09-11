package foodprint.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import foodprint.backend.dto.ChargeRequest;
import foodprint.backend.dto.ChargeRequest.Currency;
import foodprint.backend.dto.CheckoutDTO;

import foodprint.backend.model.Reservation;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {

    @Value("${STRIPE_PUBLIC_KEY}")
    private String stripePublicKey;

    @GetMapping("/checkout")
    public ResponseEntity<CheckoutDTO> checkout() {
        // need frontend to implement

        // model.addAttribute("amount", 50 * 100); // in cents
        // model.addAttribute("stripePublicKey", stripePublicKey);
        // model.addAttribute("currency", ChargeRequest.Currency.EUR);
        // Integer lineItemsId = reservation.getReservationId();
        Integer lineItemsId = 12;
        Double amount = 1000.0;
        Currency currency = Currency.EUR;
        CheckoutDTO checkoutDTO = new CheckoutDTO(stripePublicKey, lineItemsId, amount, currency);
        return new ResponseEntity<>(checkoutDTO, HttpStatus.OK);
    }

}
