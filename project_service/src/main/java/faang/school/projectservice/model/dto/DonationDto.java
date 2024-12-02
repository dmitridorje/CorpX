package faang.school.projectservice.model.dto;

import faang.school.projectservice.model.enums.Currency;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DonationDto {
    private Long id;
    private Long paymentNumber;
    @NotNull
    private BigDecimal amount;
    private LocalDateTime donationTime;
    @NotNull
    private Long campaignId;
    @NotNull
    private Currency currency;
    @NotNull
    private Long userId;
    private Currency targetCurrency;
}
