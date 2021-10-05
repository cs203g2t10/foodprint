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
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Service;
//import org.springframework.web.bind.annotation.RequestMapping;

import foodprint.backend.dto.ChargeRequest;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;

@Service
 @RequestMapping("/api/v1/restaurant")
 @PropertySources({
     @PropertySource(value = "file:data/secrets.properties")
 })
public class StripeService {

     @Value("${STRIPE_SECRET_KEY}")
     private String secretKey;
    
     @PostConstruct
     public void init() {
         Stripe.apiKey = secretKey;
     }
    
    public Charge charge(ChargeRequest chargeRequest) 
      throws AuthenticationException, InvalidRequestException,
        ApiConnectionException, CardException, ApiException, StripeException {
         String token = chargeRequest.getStripeToken();
        ChargeCreateParams params =
                ChargeCreateParams.builder()
                .setAmount(999L)
                .setCurrency("usd")
                .setDescription("Example")
                .setSource(token)
                .build();
        Charge charge = Charge.create(params);
        return charge;
//        Map<String, Object> chargeParams = new HashMap<>();
//        chargeParams.put("amount", chargeRequest.getAmount());
//        chargeParams.put("currency", chargeRequest.getCurrency());
//        chargeParams.put("description", chargeRequest.getDescription());
//        chargeParams.put("source", chargeRequest.getStripeToken());
//        return Charge.create(chargeParams);
//        return new Charge();
    }
}
