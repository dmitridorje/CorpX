package faang.school.accountservice.scheduler;

import faang.school.accountservice.config.scheduler.ScheduledAccountConfig;
import faang.school.accountservice.model.enums.AccountType;
import faang.school.accountservice.service.FreeAccountNumbersService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FreeNumbersGenerationScheduler {
    private final ScheduledAccountConfig scheduledAccountConfig;
    private final FreeAccountNumbersService freeAccountNumbersService;

    @Scheduled(cron = "${scheduler.account-numbers-generation}")
    public void generateAccountNumbersOnSchedule() {
        for (AccountType accountType : AccountType.values()) {
            Integer targetAmount = getTargetAmountForAccountType(accountType);
            if (targetAmount != null) {
              freeAccountNumbersService.ensureMinimumAccountNumbers(accountType, targetAmount);
            }
        }
    }

    private Integer getTargetAmountForAccountType(AccountType accountType) {
        ScheduledAccountConfig.AccountConfig config = scheduledAccountConfig.getAccounts().get(accountType.name());
        return config != null ? config.getTargetAmount() : null;
    }
}
