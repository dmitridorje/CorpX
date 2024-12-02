package faang.school.projectservice.model.dto;

import faang.school.projectservice.model.enums.Currency;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class DonationFilterDto {
    private LocalDateTime donationDateAfter;
    private LocalDateTime donationDateBefore;
    private Currency currency;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;

}