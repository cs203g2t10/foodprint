package foodprint.backend.dto;


public class ChargeRequest {

    public enum Currency {
        EUR, USD;
    }

    private String description;
    private Integer amount;
    private Currency currency;
    private String stripeEmail;
    private String stripeToken;

    public ChargeRequest(String description, Integer amount, Currency currency, String stripeEmail, String stripeToken) {
        this.description = description;
        this.amount = amount;
        this.currency = currency;
        this.stripeEmail = stripeEmail;
        this.stripeToken = stripeToken;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public Integer getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getStripeEmail() {
        return stripeEmail;
    }

    public String getStripeToken() {
        return stripeToken;
    }

}
