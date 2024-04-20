package org.ntnu.idi.idatt2106.sparesti.sparestibackend.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for {@link org.ntnu.idi.idatt2106.sparesti.sparestibackend.model.Goal}
 */
@Value
@Builder
// @JsonIgnoreProperties(ignoreUnknown = true)
public class GoalDTO implements Serializable {
    @NotNull @NotEmpty @NotBlank String title;
    @NotNull @PositiveOrZero BigDecimal saved;
    @NotNull @Positive BigDecimal target;
    @NotNull @PositiveOrZero BigDecimal completion;
    @NotNull @NotEmpty @NotBlank String description;
    @NotNull @PositiveOrZero Long priority;
    @Past LocalDateTime createdOn;
}
