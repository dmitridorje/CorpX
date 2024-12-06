package faang.school.accountservice.service;

import faang.school.accountservice.model.dto.SavingsAccountDto;

import java.math.BigDecimal;
import java.util.List;

public interface SavingsAccountService {
    SavingsAccountDto openSavingsAccount(SavingsAccountDto savingsAccountDto);

    SavingsAccountDto getSavingsAccount(Long id);

    List<SavingsAccountDto> getSavingsAccountByUserId(Long userId);

    void calculatePercent(Long balanceId, BigDecimal rate, Long savingsAccountId);
}
