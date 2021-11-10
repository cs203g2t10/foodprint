package foodprint.backend.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.Charge;

import foodprint.backend.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import foodprint.backend.config.AuthHelper;
import foodprint.backend.dto.ChargeDTO;
import foodprint.backend.dto.ChargeRequest;
import foodprint.backend.model.User;
import foodprint.backend.service.StripeService;

@RestController
@RequestMapping("/api/v1/charge")
public class ChargeController {

    private StripeService paymentsService;
    private ReservationService reservationService;

    @Autowired
    public ChargeController(StripeService paymentsService, ReservationService reservationService) {
        this.paymentsService = paymentsService;
        this.reservationService = reservationService;
    }

    @PostMapping("/")
    public ResponseEntity<ChargeDTO> charge(@RequestBody ChargeRequest chargeRequest) throws StripeException {
        User user = AuthHelper.getCurrentUser();
        Charge charge = paymentsService.charge(chargeRequest);
        reservationService.setPaid(chargeRequest.getReservationId(), user.getId());
        ChargeDTO chargeDTO = new ChargeDTO(charge.getId(), charge.getStatus(), charge.getBalanceTransaction());
        return new ResponseEntity<>(chargeDTO, HttpStatus.CREATED);
    }
}
