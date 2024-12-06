package faang.school.accountservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BalanceRateDto {
    private Long balanceId;
    private BigDecimal rate;
    private Long savingsAccountId;
}
