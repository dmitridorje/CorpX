package faang.school.accountservice.scheduler;

import faang.school.accountservice.model.dto.BalanceRateDto;
import faang.school.accountservice.model.enums.AccountType;
import faang.school.accountservice.repository.SavingsAccountRepository;
import faang.school.accountservice.service.FreeAccountNumbersService;
import faang.school.accountservice.service.SavingsAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SavingsAccountScheduler {
    private final FreeAccountNumbersService freeAccountNumbersServiceImpl;
    private final SavingsAccountService savingsAccountService;
    private final SavingsAccountRepository savingsAccountRepository;

    @Scheduled(cron = "0 0 1 * * *")
    @Retryable(backoff = @Backoff(delay = 5000))
    protected void savingsAccountNumberGenerator() {
        freeAccountNumbersServiceImpl.ensureMinimumAccountNumbers(AccountType.SAVINGS, 100);
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Retryable(backoff = @Backoff(delay = 5000))
    protected void calculatePercents() {
        List<BalanceRateDto> dtos = getBalanceAndRate();
        dtos.forEach(dto -> savingsAccountService.calculatePercent(dto.getBalanceId(), dto.getRate(), dto.getSavingsAccountId()));
    }

    private List<BalanceRateDto> getBalanceAndRate() {
        List<Object[]> results = savingsAccountRepository.findBalanceAndRate();

        return results.stream()
                .map(this::mapToBalanceRateDto)
                .collect(Collectors.toList());
    }

    private BalanceRateDto mapToBalanceRateDto(Object[] obj) {
        Long balanceId = ((Number) obj[0]).longValue();
        BigDecimal rate = (BigDecimal) obj[1];
        Long savingsAccountId = ((Number) obj[2]).longValue();

        return new BalanceRateDto(balanceId, rate, savingsAccountId);
    }
}
