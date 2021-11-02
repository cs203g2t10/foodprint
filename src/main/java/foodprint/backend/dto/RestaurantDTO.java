package foodprint.backend.dto;

import java.util.List;
import java.util.Objects;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import com.stripe.model.Discount;

import org.hibernate.validator.constraints.Length;

import io.swagger.v3.oas.annotations.media.Schema;

public class RestaurantDTO {

    private Long restaurantId;
    
    @Schema(defaultValue="Sushi Tei")
    @NotEmpty
    @Length(min = 1, max = 40)
    private String restaurantName;

    @Schema(defaultValue="Serangoon")
    private String restaurantLocation;

    @Schema(defaultValue="Japanese restaurant")
    @Length(min = 1)
    @NotEmpty
    private String restaurantDesc;

    @Schema(defaultValue="3")
    private Integer restaurantPriceRange;

    @Schema(defaultValue="15")
    @Min(0)
    @Max(999)
    private Integer restaurantTableCapacity = 15;

    //what time the restaurant is open for reservation - assume 24 hour clock e.g "13:00" for 1pm -> values of 0- 23
    @Schema(defaultValue ="9")
    @Min(0)
    @Max(23)
    private Integer restaurantWeekdayOpeningHour;

    //what time in minutes the restaurant is open for reservation -> should take in values of between 0 and 59 only
    @Schema(defaultValue = "15")
    @Min(0)
    @Max(59)
    private Integer restaurantWeekdayOpeningMinutes;

    //what time reservation slots close
    @Schema(defaultValue = "22")
    @Min(0)
    @Max(23)
    private Integer restaurantWeekdayClosingHour;

    @Schema(defaultValue = "30")
    @Min(0)
    @Max(59)
    private Integer restaurantWeekdayClosingMinutes;

    @Schema(defaultValue = "11")
    @Min(0)
    @Max(23)
    private Integer restaurantWeekendOpeningHour;

    @Schema(defaultValue = "00")
    @Min(0)
    @Max(59)
    private Integer restaurantWeekendOpeningMinutes;

    @Schema(defaultValue = "22")
    @Min(0)
    @Max(23)
    private Integer restaurantWeekendClosingHour;

    @Schema(defaultValue = "30")
    @Min(0)
    @Max(59)
    private Integer restaurantWeekendClosingMinutes;

    private List<@NotEmpty @Length(min=1) String> restaurantCategory;

    private PictureDTO picture;

    private List<DiscountDTO> discounts;

    public RestaurantDTO() {
    }

    public RestaurantDTO(Long restaurantId, String restaurantName, String restaurantLocation, String restaurantDesc, Integer restaurantPriceRange, Integer restaurantTableCapacity, Integer restaurantWeekdayOpeningHour, Integer restaurantWeekdayOpeningMinutes, Integer restaurantWeekdayClosingHour, Integer restaurantWeekdayClosingMinutes, Integer restaurantWeekendOpeningHour, Integer restaurantWeekendOpeningMinutes, Integer restaurantWeekendClosingHour, Integer restaurantWeekendClosingMinutes, PictureDTO picture, List<DiscountDTO> discounts) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.restaurantLocation = restaurantLocation;
        this.restaurantDesc = restaurantDesc;
        this.restaurantPriceRange = restaurantPriceRange;
        this.restaurantTableCapacity = restaurantTableCapacity;
        this.restaurantWeekdayOpeningHour = restaurantWeekdayOpeningHour;
        this.restaurantWeekdayOpeningMinutes = restaurantWeekdayOpeningMinutes;
        this.restaurantWeekdayClosingHour = restaurantWeekdayClosingHour;
        this.restaurantWeekdayClosingMinutes = restaurantWeekdayClosingMinutes;
        this.restaurantWeekendOpeningHour = restaurantWeekendOpeningHour;
        this.restaurantWeekendOpeningMinutes = restaurantWeekendOpeningMinutes;
        this.restaurantWeekendClosingHour = restaurantWeekendClosingHour;
        this.restaurantWeekendClosingMinutes = restaurantWeekendClosingMinutes;
        this.picture = picture;
        this.discounts = discounts;
    }

    public Long getRestaurantId() {
        return this.restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getRestaurantName() {
        return this.restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantLocation() {
        return this.restaurantLocation;
    }

    public void setRestaurantLocation(String restaurantLocation) {
        this.restaurantLocation = restaurantLocation;
    }

    public String getRestaurantDesc() {
        return this.restaurantDesc;
    }

    public void setRestaurantDesc(String restaurantDesc) {
        this.restaurantDesc = restaurantDesc;
    }

    public Integer getRestaurantPriceRange() {
        return this.restaurantPriceRange;
    }

    public void setRestaurantPriceRange(Integer restaurantPriceRange) {
        this.restaurantPriceRange = restaurantPriceRange;
    }

    public Integer getRestaurantTableCapacity() {
        return this.restaurantTableCapacity;
    }

    public void setRestaurantTableCapacity(Integer restaurantTableCapacity) {
        this.restaurantTableCapacity = restaurantTableCapacity;
    }

    public Integer getRestaurantWeekdayOpeningHour() {
        return this.restaurantWeekdayOpeningHour;
    }

    public void setRestaurantWeekdayOpeningHour(Integer restaurantWeekdayOpeningHour) {
        this.restaurantWeekdayOpeningHour = restaurantWeekdayOpeningHour;
    }

    public Integer getRestaurantWeekdayOpeningMinutes() {
        return this.restaurantWeekdayOpeningMinutes;
    }

    public void setRestaurantWeekdayOpeningMinutes(Integer restaurantWeekdayOpeningMinutes) {
        this.restaurantWeekdayOpeningMinutes = restaurantWeekdayOpeningMinutes;
    }

    public Integer getRestaurantWeekdayClosingHour() {
        return this.restaurantWeekdayClosingHour;
    }

    public void setRestaurantWeekdayClosingHour(Integer restaurantWeekdayClosingHour) {
        this.restaurantWeekdayClosingHour = restaurantWeekdayClosingHour;
    }

    public Integer getRestaurantWeekdayClosingMinutes() {
        return this.restaurantWeekdayClosingMinutes;
    }

    public void setRestaurantWeekdayClosingMinutes(Integer restaurantWeekdayClosingMinutes) {
        this.restaurantWeekdayClosingMinutes = restaurantWeekdayClosingMinutes;
    }

    public Integer getRestaurantWeekendOpeningHour() {
        return this.restaurantWeekendOpeningHour;
    }

    public void setRestaurantWeekendOpeningHour(Integer restaurantWeekendOpeningHour) {
        this.restaurantWeekendOpeningHour = restaurantWeekendOpeningHour;
    }

    public Integer getRestaurantWeekendOpeningMinutes() {
        return this.restaurantWeekendOpeningMinutes;
    }

    public void setRestaurantWeekendOpeningMinutes(Integer restaurantWeekendOpeningMinutes) {
        this.restaurantWeekendOpeningMinutes = restaurantWeekendOpeningMinutes;
    }

    public Integer getRestaurantWeekendClosingHour() {
        return this.restaurantWeekendClosingHour;
    }

    public void setRestaurantWeekendClosingHour(Integer restaurantWeekendClosingHour) {
        this.restaurantWeekendClosingHour = restaurantWeekendClosingHour;
    }

    public Integer getRestaurantWeekendClosingMinutes() {
        return this.restaurantWeekendClosingMinutes;
    }

    public void setRestaurantWeekendClosingMinutes(Integer restaurantWeekendClosingMinutes) {
        this.restaurantWeekendClosingMinutes = restaurantWeekendClosingMinutes;
    }

    public List<String> getRestaurantCategory() {
        return this.restaurantCategory;
    }

    public PictureDTO getPicture() {
        return this.picture;
    }

    public void setPicture(PictureDTO picture) {
        this.picture = picture;
    }

    public List<DiscountDTO> getDiscounts() {
        return this.discounts;
    }

    public void setDiscounts(List<DiscountDTO> discounts) {
        this.discounts = discounts;
    }

    public RestaurantDTO restaurantId(Long restaurantId) {
        setRestaurantId(restaurantId);
        return this;
    }

    public RestaurantDTO restaurantName(String restaurantName) {
        setRestaurantName(restaurantName);
        return this;
    }

    public RestaurantDTO restaurantLocation(String restaurantLocation) {
        setRestaurantLocation(restaurantLocation);
        return this;
    }

    public RestaurantDTO restaurantDesc(String restaurantDesc) {
        setRestaurantDesc(restaurantDesc);
        return this;
    }

    public RestaurantDTO restaurantPriceRange(Integer restaurantPriceRange) {
        setRestaurantPriceRange(restaurantPriceRange);
        return this;
    }

    public RestaurantDTO restaurantTableCapacity(Integer restaurantTableCapacity) {
        setRestaurantTableCapacity(restaurantTableCapacity);
        return this;
    }

    public RestaurantDTO restaurantWeekdayOpeningHour(Integer restaurantWeekdayOpeningHour) {
        setRestaurantWeekdayOpeningHour(restaurantWeekdayOpeningHour);
        return this;
    }

    public RestaurantDTO restaurantWeekdayOpeningMinutes(Integer restaurantWeekdayOpeningMinutes) {
        setRestaurantWeekdayOpeningMinutes(restaurantWeekdayOpeningMinutes);
        return this;
    }

    public RestaurantDTO restaurantWeekdayClosingHour(Integer restaurantWeekdayClosingHour) {
        setRestaurantWeekdayClosingHour(restaurantWeekdayClosingHour);
        return this;
    }

    public RestaurantDTO restaurantWeekdayClosingMinutes(Integer restaurantWeekdayClosingMinutes) {
        setRestaurantWeekdayClosingMinutes(restaurantWeekdayClosingMinutes);
        return this;
    }

    public RestaurantDTO restaurantWeekendOpeningHour(Integer restaurantWeekendOpeningHour) {
        setRestaurantWeekendOpeningHour(restaurantWeekendOpeningHour);
        return this;
    }

    public RestaurantDTO restaurantWeekendOpeningMinutes(Integer restaurantWeekendOpeningMinutes) {
        setRestaurantWeekendOpeningMinutes(restaurantWeekendOpeningMinutes);
        return this;
    }

    public RestaurantDTO restaurantWeekendClosingHour(Integer restaurantWeekendClosingHour) {
        setRestaurantWeekendClosingHour(restaurantWeekendClosingHour);
        return this;
    }

    public RestaurantDTO restaurantWeekendClosingMinutes(Integer restaurantWeekendClosingMinutes) {
        setRestaurantWeekendClosingMinutes(restaurantWeekendClosingMinutes);
        return this;
    }

    public RestaurantDTO pictures(PictureDTO picture) {
        setPicture(picture);
        return this;
    }

    public RestaurantDTO discounts(List<DiscountDTO> discounts) {
        setDiscounts(discounts);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof RestaurantDTO)) {
            return false;
        }
        RestaurantDTO restaurantDTO = (RestaurantDTO) o;
        return Objects.equals(restaurantId, restaurantDTO.restaurantId) && Objects.equals(restaurantName, restaurantDTO.restaurantName) && Objects.equals(restaurantLocation, restaurantDTO.restaurantLocation) && Objects.equals(restaurantDesc, restaurantDTO.restaurantDesc) && Objects.equals(restaurantPriceRange, restaurantDTO.restaurantPriceRange) && Objects.equals(restaurantTableCapacity, restaurantDTO.restaurantTableCapacity) && Objects.equals(restaurantWeekdayOpeningHour, restaurantDTO.restaurantWeekdayOpeningHour) && Objects.equals(restaurantWeekdayOpeningMinutes, restaurantDTO.restaurantWeekdayOpeningMinutes) && Objects.equals(restaurantWeekdayClosingHour, restaurantDTO.restaurantWeekdayClosingHour) && Objects.equals(restaurantWeekdayClosingMinutes, restaurantDTO.restaurantWeekdayClosingMinutes) && Objects.equals(restaurantWeekendOpeningHour, restaurantDTO.restaurantWeekendOpeningHour) && Objects.equals(restaurantWeekendOpeningMinutes, restaurantDTO.restaurantWeekendOpeningMinutes) && Objects.equals(restaurantWeekendClosingHour, restaurantDTO.restaurantWeekendClosingHour) && Objects.equals(restaurantWeekendClosingMinutes, restaurantDTO.restaurantWeekendClosingMinutes) && Objects.equals(picture, restaurantDTO.picture);
    }

    @Override
    public int hashCode() {
        return Objects.hash(restaurantId, restaurantName, restaurantLocation, restaurantDesc, restaurantPriceRange, restaurantTableCapacity, restaurantWeekdayOpeningHour, restaurantWeekdayOpeningMinutes, restaurantWeekdayClosingHour, restaurantWeekdayClosingMinutes, restaurantWeekendOpeningHour, restaurantWeekendOpeningMinutes, restaurantWeekendClosingHour, restaurantWeekendClosingMinutes, picture);
    }

    @Override
    public String toString() {
        return "{" +
            " restaurantId='" + getRestaurantId() + "'" +
            ", restaurantName='" + getRestaurantName() + "'" +
            ", restaurantLocation='" + getRestaurantLocation() + "'" +
            ", restaurantDesc='" + getRestaurantDesc() + "'" +
            ", restaurantPriceRange='" + getRestaurantPriceRange() + "'" +
            ", restaurantTableCapacity='" + getRestaurantTableCapacity() + "'" +
            ", restaurantWeekdayOpeningHour='" + getRestaurantWeekdayOpeningHour() + "'" +
            ", restaurantWeekdayOpeningMinutes='" + getRestaurantWeekdayOpeningMinutes() + "'" +
            ", restaurantWeekdayClosingHour='" + getRestaurantWeekdayClosingHour() + "'" +
            ", restaurantWeekdayClosingMinutes='" + getRestaurantWeekdayClosingMinutes() + "'" +
            ", restaurantWeekendOpeningHour='" + getRestaurantWeekendOpeningHour() + "'" +
            ", restaurantWeekendOpeningMinutes='" + getRestaurantWeekendOpeningMinutes() + "'" +
            ", restaurantWeekendClosingHour='" + getRestaurantWeekendClosingHour() + "'" +
            ", restaurantWeekendClosingMinutes='" + getRestaurantWeekendClosingMinutes() + "'" +
            ", picture='" + getPicture() + "'" +
            "}";
    }

    public void setRestaurantCategory(List<String> restaurantCategory) {
        this.restaurantCategory = restaurantCategory;
    }
}
