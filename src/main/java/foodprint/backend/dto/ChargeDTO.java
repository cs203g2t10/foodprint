package foodprint.backend.dto;

public class ChargeDTO {
    public String Id;
    public String status;
    public String balance_transaction;

    public ChargeDTO(String id, String status, String balance_transaction) {
        this.Id = id;
        this.status = status;
        this.balance_transaction = balance_transaction;
    }

    
}
