package org.ntnu.idi.idatt2106.sparesti.sparestibackend.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Value;

/**
 * DTO for {@link org.ntnu.idi.idatt2106.sparesti.sparestibackend.model.Challenge}
 */
@Value
@Builder
// @JsonIgnoreProperties(ignoreUnknown = true)
public class ChallengeDto implements Serializable {
    @NotNull @NotEmpty @NotBlank String title;
    @NotNull @PositiveOrZero BigDecimal saved;
    @NotNull @PositiveOrZero BigDecimal target;
    @NotNull @NotEmpty @NotBlank String description;
    @NotNull @Past LocalDateTime createdOn;
}
