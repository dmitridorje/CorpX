package faang.school.accountservice.service.impl;

import faang.school.accountservice.mapper.SavingsAccountMapper;
import faang.school.accountservice.model.dto.SavingsAccountDto;
import faang.school.accountservice.model.entity.*;
import faang.school.accountservice.repository.*;
import faang.school.accountservice.service.SavingsAccountService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavingsAccountServiceImpl implements SavingsAccountService {
    private final SavingsAccountMapper savingsAccountMapper;
    private final AccountRepository accountRepository;
    private final SavingsAccountRepository savingsAccountRepository;
    private final TariffRepository tariffRepository;
    private final TariffHistoryRepository tariffHistoryRepository;
    private final BalanceRepository balanceRepository;

    @Transactional
    @Override
    public SavingsAccountDto openSavingsAccount(SavingsAccountDto savingsAccountDto) {
        Tariff tariff = tariffRepository.findById(savingsAccountDto.getTariffId())
                .orElseThrow(() -> new EntityNotFoundException("Tariff with id " + savingsAccountDto.getTariffId() + " not found"));

        Account account = accountRepository.findById(savingsAccountDto.getAccountId())
                .orElseThrow(() -> new EntityNotFoundException("Account with id " + savingsAccountDto.getAccountId() + " not found"));

        SavingsAccount savingsAccount = SavingsAccount.builder()
                .account(account)
                .build();
        savingsAccount = savingsAccountRepository.save(savingsAccount);

        TariffHistory tariffHistory = TariffHistory.builder()
                .savingsAccount(savingsAccount)
                .tariff(tariff)
                .build();
        tariffHistoryRepository.save(tariffHistory);
        SavingsAccountDto resultDto = savingsAccountMapper.savingsAccountToSavingsAccountDto(savingsAccount);
        resultDto.setTariffId(tariff.getId());
        return resultDto;
    }

    @Override
    public SavingsAccountDto getSavingsAccount(Long id) {
        Optional<SavingsAccountDto> savingsAccountDto = savingsAccountRepository.findSavingsAccountWithDetails(id);
        return savingsAccountDto
                .orElseThrow(() -> new EntityNotFoundException("SavingsAccount with id " + id + " not found"));
    }

    @Override
    public List<SavingsAccountDto> getSavingsAccountByUserId(Long userId) {
        List<String> numbers = accountRepository.findNumbersByUserId(userId);
        if (numbers.isEmpty()) {
            throw new EntityNotFoundException("Accounts with user id " + userId + " not found");
        }

        List<Object[]> savingsAccounts = savingsAccountRepository.getSavingsAccountsWithLastTariffRate(numbers);
        if (savingsAccounts.isEmpty()) {
            throw new EntityNotFoundException("Accounts with user id " + userId + " not found");
        }
        return savingsAccounts.stream()
                .map(this::mapToSavingsAccountDto)
                .toList();
    }

    @Transactional
    @Async("calculatePercentsExecutor")
    @Override
    public void calculatePercent(Long balanceId, BigDecimal rate, Long savingsAccountId) {
        SavingsAccount savingsAccount = savingsAccountRepository.findById(savingsAccountId)
                .orElseThrow(() -> new EntityNotFoundException("SavingsAccount with id " + savingsAccountId + " not found"));

        Balance balance = balanceRepository.findById(balanceId)
                .orElseThrow(() -> new EntityNotFoundException("Balance with id " + balanceId + " not found"));

        int currentYearDays = Year.now().length();
        BigDecimal dailyRate = rate.divide(BigDecimal.valueOf(currentYearDays), 8, RoundingMode.HALF_UP);
        BigDecimal dailyInterest = balance.getActualBalance()
                .multiply((dailyRate.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP)));
        BigDecimal newBalance = balance.getActualBalance().add(dailyInterest);
        balance.setActualBalance(newBalance);
        LocalDateTime currentTime = LocalDateTime.now();
        savingsAccount.setUpdatedAt(currentTime);
        savingsAccount.setLastDatePercent(currentTime);
    }

    private SavingsAccountDto mapToSavingsAccountDto(Object[] obj) {
        Long id = ((Number) obj[0]).longValue();
        Long tariffId = ((Number) obj[1]).longValue();
        BigDecimal rate = (BigDecimal) obj[2];
        LocalDateTime lastDatePercent = convertObjectToLocalDateTime(obj[3]);
        LocalDateTime createdAt = convertObjectToLocalDateTime(obj[4]);
        LocalDateTime updatedAt = convertObjectToLocalDateTime(obj[5]);

        return SavingsAccountDto.builder()
                .id(id).tariffId(tariffId).rate(rate).lastDatePercent(lastDatePercent)
                .createdAt(createdAt).updatedAt(updatedAt).build();
    }

    private LocalDateTime convertObjectToLocalDateTime(Object obj) {
        if (obj == null) return null;
        LocalDateTime createdAt;
        if (obj instanceof Timestamp) {
            createdAt = ((Timestamp) obj).toLocalDateTime();
        } else if (obj instanceof Instant) {
            createdAt = LocalDateTime.ofInstant((Instant) obj, ZoneId.systemDefault());
        } else {
            throw new IllegalArgumentException("Unsupported data type: " + obj.getClass());
        }
        return createdAt;
    }
}
