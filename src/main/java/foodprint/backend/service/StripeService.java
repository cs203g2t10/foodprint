package foodprint.backend.service;

import com.stripe.Stripe;
import com.stripe.exception.ApiConnectionException;
import com.stripe.exception.ApiException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;

import com.stripe.param.ChargeCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import foodprint.backend.dto.ChargeRequest;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;

@Service
@RequestMapping("/api/v1/restaurant")
public class StripeService {

     @Value("${STRIPE_SECRET_KEY}")
     private String secretKey;
    
     @PostConstruct
     public void init() {
         Stripe.apiKey = secretKey;
     }
    
    public Charge charge(ChargeRequest chargeRequest)
      throws StripeException {

        String token = chargeRequest.getStripeToken();
        ChargeCreateParams params =
                ChargeCreateParams.builder()
                .setAmount(chargeRequest.getAmount())
                .setCurrency(chargeRequest.getCurrency().toString())
                .setDescription(chargeRequest.getDescription())
                .setSource(token)
                .build();
        return Charge.create(params);

    }
}
