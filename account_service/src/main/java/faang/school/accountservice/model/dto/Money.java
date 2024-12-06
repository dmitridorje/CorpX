package faang.school.accountservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import faang.school.accountservice.model.enums.Currency;

import java.math.BigDecimal;

public record Money(
        @JsonProperty(value = "amount", required = true)
        BigDecimal amount,
        @JsonProperty(value = "currency", required = true)
        Currency currency
) {
}
