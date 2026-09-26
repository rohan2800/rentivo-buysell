package rentivo_backend.dto;
import jakarta.validation.constraints.*; import java.math.BigDecimal;
public final class SubscriptionDtos { private SubscriptionDtos(){}
 public record PlanRequest(@NotBlank String name,@NotNull @DecimalMin("0.00") BigDecimal price,@NotNull @Min(1) Integer validityDays,@NotNull @Min(1) Integer contactLimit,String description,Boolean active){}
 public record Status(boolean active,String planName,java.time.LocalDateTime startAt,java.time.LocalDateTime endAt,int contactLimit,int contactsUsed,int contactsRemaining,String message){}
 public record ContactResponse(boolean unlocked,String ownerName,String ownerPhone,int contactsUsed,int contactsRemaining,String message){}
}
