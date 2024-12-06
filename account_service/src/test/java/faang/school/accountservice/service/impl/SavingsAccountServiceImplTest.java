package faang.school.accountservice.service.impl;

import faang.school.accountservice.mapper.SavingsAccountMapperImpl;
import faang.school.accountservice.model.dto.SavingsAccountDto;
import faang.school.accountservice.model.entity.*;
import faang.school.accountservice.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingsAccountServiceImplTest {

    @Spy
    private SavingsAccountMapperImpl savingsAccountMapper;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private SavingsAccountRepository savingsAccountRepository;

    @Mock
    private TariffRepository tariffRepository;

    @Mock
    private TariffHistoryRepository tariffHistoryRepository;

    @Mock
    private BalanceRepository balanceRepository;

    @InjectMocks
    private SavingsAccountServiceImpl savingsAccountService;

    @Captor
    ArgumentCaptor<SavingsAccount> savingsAccountArgumentCaptor;

    @Captor
    ArgumentCaptor<TariffHistory> tariffHistoryArgumentCaptor;

    @Captor
    ArgumentCaptor<BigDecimal> bigDecimalArgumentCaptor;

    @Test
    public void testOpenSavingsAccount() {
        Long tariffId = 1L;
        Long accountId = 2L;
        SavingsAccountDto dto = new SavingsAccountDto();
        dto.setTariffId(tariffId);
        dto.setAccountId(accountId);
        Tariff tariff = Tariff.builder()
                .id(tariffId)
                .name("tariff1").build();
        Account account = Account.builder()
                .id(dto.getAccountId()).build();
        when(tariffRepository.findById(dto.getTariffId())).thenReturn(Optional.of(tariff));
        when(accountRepository.findById(dto.getAccountId())).thenReturn(Optional.of(account));
        when(savingsAccountRepository.save(savingsAccountArgumentCaptor.capture()))
                .thenReturn(SavingsAccount.builder().account(account).build());

        SavingsAccountDto resultDto = savingsAccountService.openSavingsAccount(dto);

        verify(tariffRepository, times(1)).findById(dto.getTariffId());
        verify(accountRepository, times(1)).findById(dto.getAccountId());
        verify(savingsAccountRepository, times(1)).save(savingsAccountArgumentCaptor.capture());
        verify(tariffHistoryRepository, times(1)).save(tariffHistoryArgumentCaptor.capture());
        assertEquals(dto.getAccountId(), resultDto.getAccountId());
    }

    @Test
    public void testOpenSavingsAccountNotFound() {
        assertThrows(EntityNotFoundException.class, () -> savingsAccountService.openSavingsAccount(new SavingsAccountDto()));
    }

    @Test
    public void testGetSavingsAccount() {
        Long savingsAccountId = 1L;
        when(savingsAccountRepository.findSavingsAccountWithDetails(savingsAccountId)).thenReturn(Optional.of(new SavingsAccountDto()));

        savingsAccountService.getSavingsAccount(savingsAccountId);

        verify(savingsAccountRepository, times(1)).findSavingsAccountWithDetails(savingsAccountId);
    }

    @Test
    public void testGetSavingsAccountNotFound() {
        assertThrows(EntityNotFoundException.class, () -> savingsAccountService.getSavingsAccount(1L));
    }

    @Test
    public void testGetSavingsAccountByUserId_Success() {
        Long userId = 1L;
        BigDecimal rate = BigDecimal.valueOf(5.5);
        List<String> numbers = List.of("429346812734628", "38642897364528736");
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        List<Object[]> savingsAccounts = new ArrayList<>();
        savingsAccounts.add(new Object[]{userId, 1L, rate, timestamp, timestamp, timestamp});
        savingsAccounts.add(new Object[]{userId, 2L, rate, timestamp, timestamp, timestamp});
        when(accountRepository.findNumbersByUserId(userId)).thenReturn(numbers);
        when(savingsAccountRepository.getSavingsAccountsWithLastTariffRate(numbers)).thenReturn(savingsAccounts);

        List<SavingsAccountDto> results = savingsAccountService.getSavingsAccountByUserId(userId);

        verify(accountRepository, times(1)).findNumbersByUserId(userId);
        verify(savingsAccountRepository, times(1)).getSavingsAccountsWithLastTariffRate(numbers);
        assertAll(
                () -> assertNotNull(results),
                () -> assertEquals(2, results.size()),
                () -> assertEquals(userId, results.get(0).getId()),
                () -> assertEquals(userId, results.get(1).getId())
        );
    }

    @Test
    public void testGetSavingsAccountByUserIdNotFound() {
        assertThrows(EntityNotFoundException.class, () -> savingsAccountService.getSavingsAccount(1L));
    }

    @Test
    public void calculatePercentSuccess() {
        BigDecimal initBalance = BigDecimal.valueOf(100_000);
        Long balanceId = 1L;
        BigDecimal rate = BigDecimal.valueOf(5.5);
        Long savingsAccountId = 2L;
        SavingsAccount savingsAccount = new SavingsAccount();
        savingsAccount.setId(savingsAccountId);
        Balance balance = mock(Balance.class);
        when(savingsAccountRepository.findById(savingsAccountId)).thenReturn(Optional.of(savingsAccount));
        when(balanceRepository.findById(balanceId)).thenReturn(Optional.of(balance));
        when(balance.getActualBalance()).thenReturn(initBalance);

        savingsAccountService.calculatePercent(balanceId, rate, savingsAccountId);

        verify(savingsAccountRepository, times(1)).findById(savingsAccountId);
        verify(balanceRepository, times(1)).findById(balanceId);
        verify(balance, times(1)).setActualBalance(bigDecimalArgumentCaptor.capture());
        BigDecimal newBalance = bigDecimalArgumentCaptor.getValue();
        assertEquals(1, newBalance.compareTo(initBalance));
    }
}