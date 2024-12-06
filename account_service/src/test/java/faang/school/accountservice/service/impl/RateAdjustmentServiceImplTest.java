package faang.school.accountservice.service.impl;

import faang.school.accountservice.model.entity.Account;
import faang.school.accountservice.model.entity.SavingsAccount;
import faang.school.accountservice.model.entity.SavingsAccountRate;
import faang.school.accountservice.model.entity.Tariff;
import faang.school.accountservice.model.entity.TariffHistory;
import faang.school.accountservice.repository.AccountRepository;
import faang.school.accountservice.repository.SavingsAccountRateRepository;
import faang.school.accountservice.repository.SavingsAccountRepository;
import faang.school.accountservice.repository.TariffHistoryRepository;
import faang.school.accountservice.repository.TariffRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateAdjustmentServiceImplTest {

    @Mock
    private SavingsAccountRepository savingsAccountRepository;

    @Mock
    private TariffHistoryRepository tariffHistoryRepository;

    @Mock
    private TariffRepository tariffRepository;

    @Mock
    private SavingsAccountRateRepository savingsAccountRateRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private RateAdjustmentServiceImpl rateAdjustmentService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(rateAdjustmentService, "maxRate", BigDecimal.valueOf(10.0));
    }

    @Test
    @DisplayName("Should adjust rate and save all entities")
    @SuppressWarnings("unchecked")
    public void testAdjustRate_Success() {
        SavingsAccount savingsAccount = new SavingsAccount();
        savingsAccount.setId(1L);
        savingsAccount.setAccount(new Account());
        savingsAccount.setLastBonusUpdate(LocalDateTime.now().minusDays(2));

        Tariff tariff = new Tariff();
        tariff.setId(1L);

        TariffHistory tariffHistory = new TariffHistory();
        tariffHistory.setTariff(tariff);

        SavingsAccountRate currentRate = new SavingsAccountRate();
        currentRate.setRate(BigDecimal.valueOf(5.0));

        List<SavingsAccount> savingsAccounts = List.of(savingsAccount);

        when(savingsAccountRepository.findSavingsAccountsByUserId(1L)).thenReturn(savingsAccounts);
        when(tariffRepository.findById(1L)).thenReturn(Optional.of(tariff));
        when(tariffHistoryRepository.findLatestTariffIdBySavingsAccountId(1L)).thenReturn(Optional.of(1L));
        when(savingsAccountRateRepository.findLatestRateIdByTariffId(1L)).thenReturn(Optional.of(currentRate.getRate()));

        boolean result = rateAdjustmentService.adjustRate(1L, new BigDecimal("1.0"));

        assertTrue(result, "Rate adjustment should be successful");

        ArgumentCaptor<List<SavingsAccount>> savingsAccountCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<SavingsAccountRate>> rateCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<TariffHistory>> historyCaptor = ArgumentCaptor.forClass(List.class);

        verify(savingsAccountRepository).saveAll(savingsAccountCaptor.capture());
        verify(savingsAccountRateRepository).saveAll(rateCaptor.capture());
        verify(tariffHistoryRepository).saveAll(historyCaptor.capture());

        List<SavingsAccount> savedSavingsAccounts = savingsAccountCaptor.getValue();
        List<SavingsAccountRate> savedRates = rateCaptor.getValue();
        List<TariffHistory> savedHistories = historyCaptor.getValue();

        assertAll(
                () -> assertEquals(1, savedSavingsAccounts.size()),
                () -> assertNotNull(savedSavingsAccounts.get(0).getLastBonusUpdate()),
                () -> assertTrue(savedSavingsAccounts.get(0).getLastBonusUpdate()
                        .isAfter(LocalDateTime.now().minusMinutes(1))),
                () -> assertEquals(1, savedRates.size())
        );

        SavingsAccountRate savedRate = savedRates.get(0);

        assertAll(
                () -> assertEquals(tariff, savedRate.getTariff()),
                () -> assertEquals(BigDecimal.valueOf(6.0), savedRate.getRate()),
                () -> assertEquals(BigDecimal.valueOf(1.0), savedRate.getRateBonusAdded()),
                () -> assertNotNull(savedRate.getCreatedAt()),
                () -> assertTrue(savedRate.getCreatedAt().isBefore(LocalDateTime.now().plusMinutes(1)))
        );

        assertEquals(1, savedHistories.size());

        TariffHistory savedHistory = savedHistories.get(0);

        assertAll(
                () -> assertEquals(savingsAccount, savedHistory.getSavingsAccount()),
                () -> assertEquals(tariff, savedHistory.getTariff()),
                () -> assertNotNull(savedHistory.getCreatedAt()),
                () -> assertTrue(savedHistory.getCreatedAt().isBefore(LocalDateTime.now().plusMinutes(1)))
        );
    }

    @Test
    @DisplayName("Should throw exception when rate adjustment attempted within 24 hours")
    public void testAdjustRate_InsufficientTimeElapsed() {

        SavingsAccount savingsAccount = new SavingsAccount();
        savingsAccount.setLastBonusUpdate(LocalDateTime.now());

        List<SavingsAccount> savingsAccounts = List.of(savingsAccount);

        when(savingsAccountRepository.findSavingsAccountsByUserId(1L)).thenReturn(savingsAccounts);

        assertThrows(IllegalStateException.class, () -> rateAdjustmentService.adjustRate(1L, new BigDecimal("1.0")),
                "Rate can only be adjusted once per 24 hours.");
    }

    @Test
    @DisplayName("Should throw exception when no tariff history is found")
    public void testAdjustRate_NoHistoryFound() {
        SavingsAccount savingsAccount = new SavingsAccount();
        savingsAccount.setId(1L);
        savingsAccount.setAccount(new Account());
        savingsAccount.setLastBonusUpdate(LocalDateTime.now().minusDays(2));

        List<SavingsAccount> savingsAccounts = List.of(savingsAccount);

        when(savingsAccountRepository.findSavingsAccountsByUserId(1L)).thenReturn(savingsAccounts);
        when(tariffHistoryRepository.findLatestTariffIdBySavingsAccountId(1L)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> rateAdjustmentService.adjustRate(1L, BigDecimal.valueOf(1.0)),
                "No tariff history found for savings account: 12345");

        assertTrue(exception.getMessage().contains("No tariff history found for savings account"));
    }

    @Test
    @DisplayName("Should throw exception when no rate is found for tariff")
    public void testAdjustRate_NoRateFound() {

        Account account = Account.builder()
                .number("1234567890")
                .projectId(1L)
                .userId(1L)
                .build();

        SavingsAccount savingsAccount = new SavingsAccount();
        savingsAccount.setId(1L);
        savingsAccount.setLastBonusUpdate(LocalDateTime.now().minusDays(2));
        savingsAccount.setAccount(account);

        Tariff tariff = new Tariff();
        tariff.setId(1L);

        TariffHistory tariffHistory = new TariffHistory();
        tariffHistory.setTariff(tariff);

        List<SavingsAccount> savingsAccounts = List.of(savingsAccount);

        when(savingsAccountRepository.findSavingsAccountsByUserId(1L)).thenReturn(savingsAccounts);
        when(tariffHistoryRepository.findLatestTariffIdBySavingsAccountId(1L)).thenReturn(Optional.of(1L));
        when(tariffRepository.findById(1L)).thenReturn(Optional.of(tariff));
        when(savingsAccountRateRepository.findLatestRateIdByTariffId(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> rateAdjustmentService.adjustRate(1L, new BigDecimal("1.0")),
                "No rate found for tariff ID: 1");

        assertEquals("No rate found for tariff ID: 1", exception.getMessage());
    }
}
