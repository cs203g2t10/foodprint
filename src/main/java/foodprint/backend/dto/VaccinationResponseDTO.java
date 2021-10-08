package foodprint.backend.dto;

import java.util.Objects;

public class VaccinationResponseDTO {
    
    private String result;

    private String reason;

    public VaccinationResponseDTO() {
    }

    public VaccinationResponseDTO(String result, String reason) {
        this.result = result;
        this.reason = reason;
    }

    public String getResult() {
        return this.result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getReason() {
        return this.reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public VaccinationResponseDTO result(String result) {
        setResult(result);
        return this;
    }

    public VaccinationResponseDTO reason(String reason) {
        setReason(reason);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof VaccinationResponseDTO)) {
            return false;
        }
        VaccinationResponseDTO vaccinationResponseDTO = (VaccinationResponseDTO) o;
        return Objects.equals(result, vaccinationResponseDTO.result) && Objects.equals(reason, vaccinationResponseDTO.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, reason);
    }

    @Override
    public String toString() {
        return "{" +
            " result='" + getResult() + "'" +
            ", reason='" + getReason() + "'" +
            "}";
    }

}
