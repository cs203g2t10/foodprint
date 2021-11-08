package foodprint.backend.dto;

public class ChargeDTO {
    private String id;
    private String status;
    private String balanceTransaction;

    public ChargeDTO(String id, String status, String balanceTransaction) {
        this.id = id;
        this.status = status;
        this.balanceTransaction = balanceTransaction;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBalanceTransaction() {
        return this.balanceTransaction;
    }

    public void setBalanceTransaction(String balanceTransaction) {
        this.balanceTransaction = balanceTransaction;
    }

}
