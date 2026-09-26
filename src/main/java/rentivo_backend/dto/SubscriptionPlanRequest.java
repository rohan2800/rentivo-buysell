package rentivo_backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record SubscriptionPlanRequest(
        @NotBlank String name,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @NotNull @Min(1) Integer validityDays,
        @NotNull @Min(1) Integer contactLimit,
        String description,
        Boolean active
) {}
