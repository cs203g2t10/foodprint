package foodprint.backend.dto;


public class ChargeRequest {

    public enum Currency {
        EUR, USD, SGD;
    }

    private Long reservationId;
    private String description;
    private Long amount;
    private Currency currency;
    private String stripeEmail;
    private String stripeToken;


    public ChargeRequest(Long reservationId, String description, Long amount, Currency currency, String stripeEmail, String stripeToken) {
        this.reservationId = reservationId;
        this.description = description;
        this.amount = amount;
        this.currency = currency;
        this.stripeEmail = stripeEmail;
        this.stripeToken = stripeToken;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public Long getAmount() {
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
