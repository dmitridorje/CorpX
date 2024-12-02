package faang.school.projectservice.model.dto;

import faang.school.projectservice.model.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PaymentResponseDto {
    private String status;
    private int verificationCode;
    private long paymentNumber;
    private BigDecimal amount;
    private Currency paymentCurrency;
    private Currency targetCurrency;
    private String message;
}