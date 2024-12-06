package faang.school.accountservice.service.impl;

import faang.school.accountservice.mapper.BalanceAuditMapper;
import faang.school.accountservice.mapper.BalanceMapper;
import faang.school.accountservice.model.dto.BalanceDto;
import faang.school.accountservice.model.entity.Account;
import faang.school.accountservice.model.entity.Balance;
import faang.school.accountservice.repository.BalanceAuditRepository;
import faang.school.accountservice.repository.BalanceRepository;
import faang.school.accountservice.util.ExceptionThrowingValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceServiceImplTest {

    @Mock
    private BalanceRepository balanceRepository;

    @Mock
    private BalanceMapper balanceMapper;

    @Mock
    private ExceptionThrowingValidator validator;

    @Mock
    private BalanceAuditRepository balanceAuditRepository;

    @Mock
    private BalanceAuditMapper balanceAuditMapper;

    @InjectMocks
    private BalanceServiceImpl balanceService;

    private Balance balance;
    private BalanceDto balanceDto;

    @BeforeEach
    void setUp() {
        balance = new Balance();
        balance.setId(1L);
        Account account = new Account();
        account.setId(123L);
        balance.setAccount(account);
        balance.setAuthorizedBalance(BigDecimal.valueOf(100));
        balance.setActualBalance(BigDecimal.valueOf(100));

        balanceDto = new BalanceDto();
        balanceDto.setId(1L);
        balanceDto.setAccountId(123L);
        balanceDto.setAuthorizedBalance(BigDecimal.valueOf(100));
        balanceDto.setActualBalance(BigDecimal.valueOf(100));
    }

    @Test
    void getBalanceByAccountId_Success() {
        when(balanceRepository.findByAccountId(123L)).thenReturn(Optional.of(balance));
        when(balanceMapper.toDto(balance)).thenReturn(balanceDto);

        BalanceDto result = balanceService.getBalanceByAccountId(123L);

        assertNotNull(result);
        assertEquals(123L, result.getAccountId());
        verify(validator, times(1)).validate(balanceDto, BalanceDto.GetResponse.class);
        verify(balanceRepository, times(1)).findByAccountId(123L);
    }

    @Test
    void getBalanceByAccountId_NotFound() {
        when(balanceRepository.findByAccountId(123L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> balanceService.getBalanceByAccountId(123L));

        assertEquals("Balance not found for account id: 123", exception.getMessage());
    }

    @Test
    void updateBalance_Success() {
        balanceDto.setAuthorizedBalance(BigDecimal.valueOf(90));
        balanceDto.setActualBalance(BigDecimal.valueOf(100));

        when(balanceRepository.findById(1L)).thenReturn(Optional.of(balance));
        when(balanceRepository.save(balance)).thenReturn(balance);
        when(balanceMapper.toDto(balance)).thenReturn(balanceDto);

        BalanceDto result = balanceService.updateBalance(balanceDto);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(90), result.getAuthorizedBalance());
        verify(validator, times(1)).validate(balanceDto, BalanceDto.GetResponse.class);
        verify(balanceRepository, times(1)).findById(1L);
        verify(balanceRepository, times(1)).save(balance);
    }

    @Test
    void updateBalance_NotFound() {
        when(balanceRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> balanceService.updateBalance(balanceDto));

        assertEquals("Balance not found for id: 1", exception.getMessage());
    }

    @Test
    void updateBalance_NegativeActualBalance() {
        when(balanceRepository.findById(1L)).thenReturn(Optional.of(balance));
        balanceDto.setActualBalance(BigDecimal.valueOf(-10));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> balanceService.updateBalance(balanceDto));

        assertEquals("Actual balance cannot be negative.", exception.getMessage());
    }

    @Test
    void updateBalance_NegativeAuthorizedBalance() {
        when(balanceRepository.findById(1L)).thenReturn(Optional.of(balance));
        balanceDto.setAuthorizedBalance(BigDecimal.valueOf(-10));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> balanceService.updateBalance(balanceDto));

        assertEquals("Authorized balance cannot be negative.", exception.getMessage());
    }

    @Test
    void updateBalance_AuthorizedExceedsActual() {
        when(balanceRepository.findById(1L)).thenReturn(Optional.of(balance));
        balanceDto.setAuthorizedBalance(BigDecimal.valueOf(150));
        balanceDto.setActualBalance(BigDecimal.valueOf(100));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> balanceService.updateBalance(balanceDto));

        assertEquals("Authorized balance cannot exceed actual balance.", exception.getMessage());
    }
}