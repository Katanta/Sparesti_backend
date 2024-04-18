package org.ntnu.idi.idatt2106.sparesti.sparestibackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

@Data
@Embeddable
public class Account {

    @NotNull
    @Column(updatable = false, nullable = false)
    private String accNumber;

    @ColumnDefault("0.00")
    @Column(nullable = false)
    @NotNull
    private BigDecimal balance;
}