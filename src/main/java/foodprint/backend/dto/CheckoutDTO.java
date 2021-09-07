package foodprint.backend.dto;

// import java.util.Currency;
import foodprint.backend.dto.ChargeRequest.Currency;

public class CheckoutDTO {

    private String stripePublicKey;
    private Integer orderId;
    private Double amount;
    private ChargeRequest.Currency currency;

    public CheckoutDTO(String stripePublicKey, Integer orderId, Double amount, Currency currency) {
        this.stripePublicKey = stripePublicKey;
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
    }

    public String getStripePublicKey() {
        return stripePublicKey;
    }
    
    public Integer getOrderId() {
        return orderId;
    }
    
    public Double getAmount() {
        return amount;
    }
   
    public Currency getCurrency() {
        return currency;
    }
    
}
