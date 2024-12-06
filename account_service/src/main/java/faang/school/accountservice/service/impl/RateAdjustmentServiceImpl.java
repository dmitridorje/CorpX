package faang.school.accountservice.service.impl;

import faang.school.accountservice.model.entity.SavingsAccount;
import faang.school.accountservice.model.entity.SavingsAccountRate;
import faang.school.accountservice.model.entity.Tariff;
import faang.school.accountservice.model.entity.TariffHistory;
import faang.school.accountservice.repository.SavingsAccountRateRepository;
import faang.school.accountservice.repository.SavingsAccountRepository;
import faang.school.accountservice.repository.TariffHistoryRepository;
import faang.school.accountservice.repository.TariffRepository;
import faang.school.accountservice.service.RateAdjustmentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateAdjustmentServiceImpl implements RateAdjustmentService {
    private final SavingsAccountRepository savingsAccountRepository;
    private final TariffHistoryRepository tariffHistoryRepository;
    private final TariffRepository tariffRepository;
    private final SavingsAccountRateRepository savingsAccountRateRepository;

    @Value("${rate-change-rules.max-rate}")
    private BigDecimal maxRate;

    @Override
    @Transactional
    public boolean adjustRate(long userId, BigDecimal rateChange) {
        log.info("Starting rate adjustment for user ID {} with rate change: {}", userId, rateChange);

        List<SavingsAccount> savingsAccounts = savingsAccountRepository.findSavingsAccountsByUserId(userId);

        if (savingsAccounts.isEmpty()) {
            log.info("No SavingsAccount found for user ID {}. Rate adjustment aborted.", userId);
            return false;
        }

        List<SavingsAccountRate> newRateEntries = new ArrayList<>();
        List<TariffHistory> newTariffHistories = new ArrayList<>();

        for (SavingsAccount savingsAccount : savingsAccounts) {
            validateLastBonusUpdate(savingsAccount);

            Tariff tariff = getLatestTariffForSavingsAccount(savingsAccount);
            log.debug("Latest tariff found for savings account {}: {}", savingsAccount.getAccount().getNumber(), tariff);

            BigDecimal currentRate = getCurrentRateForTariff(tariff);
            log.debug("Current rate for tariff ID {}: {}", tariff.getId(), currentRate);

            BigDecimal newRate = calculateAdjustedRate(currentRate, rateChange);
            log.info("Calculated new rate for account {}: {}", savingsAccount.getAccount().getNumber(), newRate);

            updateSavingsAccountLastBonus(savingsAccount);
            newRateEntries.add(createNewRateEntry(tariff, newRate, rateChange));
            newTariffHistories.add(createNewHistoryEntry(savingsAccount, tariff));
        }

        saveAllEntities(savingsAccounts, newRateEntries, newTariffHistories);
        log.info("Rate adjustment completed for user ID {}", userId);
        return true;
    }

    private void validateLastBonusUpdate(SavingsAccount savingsAccount) {
        if (savingsAccount.getLastBonusUpdate() != null &&
                savingsAccount.getLastBonusUpdate().isAfter(LocalDateTime.now().minusDays(1))) {
            throw new IllegalStateException("Rate can only be adjusted once per 24 hours.");
        }
    }

    private Tariff getLatestTariffForSavingsAccount(SavingsAccount savingsAccount) {
        Long latestTariffId = tariffHistoryRepository.findLatestTariffIdBySavingsAccountId(savingsAccount.getId())
                .orElseThrow(() -> new IllegalStateException("No tariff history found for savings account: "
                        + savingsAccount.getAccount().getNumber()));

        return tariffRepository.findById(latestTariffId)
                .orElseThrow(() -> new EntityNotFoundException("Tariff not found for ID: " + latestTariffId));
    }

    private BigDecimal getCurrentRateForTariff(Tariff tariff) {
        return savingsAccountRateRepository.findLatestRateIdByTariffId(tariff.getId())
                .orElseThrow(() -> new EntityNotFoundException("No rate found for tariff ID: " + tariff.getId()));
    }

    private BigDecimal calculateAdjustedRate(BigDecimal currentRate, BigDecimal rateChange) {
        BigDecimal newRate = currentRate.add(rateChange);
        BigDecimal adjustedRate = newRate.max(BigDecimal.ZERO);

        return adjustedRate.min(maxRate);
    }

    private void updateSavingsAccountLastBonus(SavingsAccount savingsAccount) {
        savingsAccount.setLastBonusUpdate(LocalDateTime.now());
    }

    private SavingsAccountRate createNewRateEntry(Tariff tariff, BigDecimal newRate, BigDecimal rateChange) {
        SavingsAccountRate newRateEntry = new SavingsAccountRate();
        newRateEntry.setTariff(tariff);
        newRateEntry.setRate(newRate);
        newRateEntry.setCreatedAt(LocalDateTime.now());
        newRateEntry.setRateBonusAdded(rateChange);
        return newRateEntry;
    }

    private TariffHistory createNewHistoryEntry(SavingsAccount savingsAccount, Tariff tariff) {
        TariffHistory historyEntry = new TariffHistory();
        historyEntry.setSavingsAccount(savingsAccount);
        historyEntry.setTariff(tariff);
        historyEntry.setCreatedAt(LocalDateTime.now());
        return historyEntry;
    }

    private void saveAllEntities(List<SavingsAccount> savingsAccounts, List<SavingsAccountRate> newRateEntries, List<TariffHistory> newTariffHistories) {
        savingsAccountRepository.saveAll(savingsAccounts);
        savingsAccountRateRepository.saveAll(newRateEntries);
        tariffHistoryRepository.saveAll(newTariffHistories);
    }
}
