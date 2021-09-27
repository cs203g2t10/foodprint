package foodprint.backend.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.Charge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import foodprint.backend.dto.ChargeDTO;
import foodprint.backend.dto.ChargeRequest;
import foodprint.backend.dto.ChargeRequest.Currency;
import foodprint.backend.service.StripeService;

@RestController
@RequestMapping("/api/v1/charge")
public class ChargeController {
    @Autowired
    private StripeService paymentsService;

    @PostMapping("/")
    public ResponseEntity<ChargeDTO> charge(ChargeRequest chargeRequest)
      throws StripeException {
        chargeRequest.setDescription("Example charge");
        chargeRequest.setCurrency(Currency.EUR);
        Charge charge = paymentsService.charge(chargeRequest);
        ChargeDTO chargeDTO = new ChargeDTO(charge.getId(), charge.getStatus(), charge.getBalanceTransaction());
        return new ResponseEntity<>(chargeDTO, HttpStatus.CREATED);
    }

    // @ExceptionHandler(StripeException.class)
    // public String handleError(Model model, StripeException ex) {
    //     model.addAttribute("error", ex.getMessage());
    //     return "result";
    // }
    
}
