package foodprint.backend.dto;

// import java.util.Currency;
import foodprint.backend.dto.ChargeRequest.Currency;

public class CheckoutDTO {

    private String stripePublicKey;
    private Integer lineItemsId;
    private Double amount;
    private ChargeRequest.Currency currency;

    public CheckoutDTO(String stripePublicKey, Integer lineItemsId, Double amount, Currency currency) {
        this.stripePublicKey = stripePublicKey;
        this.lineItemsId = lineItemsId;
        this.amount = amount;
        this.currency = currency;
    }

    public String getStripePublicKey() {
        return stripePublicKey;
    }
    
    public Integer getLineItemsId() {
        return lineItemsId;
    }
    
    public Double getAmount() {
        return amount;
    }
   
    public Currency getCurrency() {
        return currency;
    }
    
}
