package rentivo_backend.dto;
import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.util.*;
public final class ListingDtos { private ListingDtos(){}
 public record CreateListingRequest(@NotBlank String title,@NotNull Long categoryId,@NotBlank String listingType,@NotNull @DecimalMin("0.00") BigDecimal price,@NotBlank String priceUnit,@NotBlank String description,@NotBlank String state,@NotBlank String city,@NotBlank String locality,String pincode,String address,Double latitude,Double longitude,@NotBlank String contactPhone,Map<Long,String> fields){}
 public record ListingSummary(Long id,String title,String category,String listingType,BigDecimal price,String priceUnit,String city,String locality,boolean contactLocked,String status){}
}
