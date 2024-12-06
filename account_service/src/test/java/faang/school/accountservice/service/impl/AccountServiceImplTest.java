package faang.school.accountservice.service.impl;

import faang.school.accountservice.mapper.AccountMapperImpl;
import faang.school.accountservice.mapper.BalanceAuditMapper;
import faang.school.accountservice.model.dto.AccountDto;
import faang.school.accountservice.model.entity.Account;
import faang.school.accountservice.model.enums.AccountStatus;
import faang.school.accountservice.model.enums.AccountType;
import faang.school.accountservice.repository.AccountRepository;
import faang.school.accountservice.repository.BalanceAuditRepository;
import faang.school.accountservice.util.ExceptionThrowingValidator;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@RequiredArgsConstructor
@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Captor
    ArgumentCaptor<Account> accountCaptor;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BalanceAuditRepository balanceAuditRepository;

    @Mock
    private BalanceAuditMapper balanceAuditMapper;

    @Mock
    private FreeAccountNumbersServiceImpl freeAccountNumbersService;

    @Mock
    private ExceptionThrowingValidator validator;

    @Spy
    private AccountMapperImpl accountMapper;

    @InjectMocks
    private AccountServiceImpl service;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
    }

    @Test
    void getAccountSuccess() {
        account.setUserId(3L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        AccountDto accountDto = service.getAccount(1L);

        assertEquals(accountDto.getUserId(), account.getUserId());
        verify(accountRepository, times(1)).findById(1L);
    }

    @Test
    void getAccountNumber() {
        account.setNumber("12345678901234");
        when(accountRepository.findAccountByNumber(account.getNumber())).thenReturn(Optional.of(account));

        AccountDto accountDto = service.getAccountNumber(account.getNumber());

        assertEquals(accountDto.getNumber(), account.getNumber());
        verify(accountRepository, times(1)).findAccountByNumber(account.getNumber());
    }

    @Test
    void openAccount() {
        AccountDto accountDto = new AccountDto();
        accountDto.setType(AccountType.BUSINESS);

        service.openAccount(accountDto);

        verify(accountRepository, times(1)).save(accountCaptor.capture());
        Account captureAccount = accountCaptor.getValue();
        verify(freeAccountNumbersService, times(1)).getFreeAccountNumber(eq(captureAccount.getType()), any());
    }

    @Test
    void blockAccount() {
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        AccountDto accountDto = service.blockAccount(anyLong());

        assertEquals(accountDto.getUserId(), account.getUserId());
        assertEquals(account.getStatus(), accountDto.getStatus());
        verify(accountRepository, times(1)).findById(anyLong());
    }

    @Test
    void blockAccountNumber() {
        when(accountRepository.findAccountByNumber(anyString())).thenReturn(Optional.of(account));

        AccountDto accountDto = service.blockAccountNumber(anyString());

        assertEquals(accountDto.getUserId(), account.getUserId());
        assertEquals(account.getStatus(), accountDto.getStatus());
        verify(accountRepository, times(1)).findAccountByNumber(anyString());
    }

    @Test
    void blockAllAccountsByUserId() {
        List<Account> accounts = createAccounts();
        accounts.forEach(account -> account.setStatus(AccountStatus.ACTIVE));

        when(accountRepository.findAllByUserId(anyLong())).thenReturn(accounts);

        List<AccountDto> accountDtos = service.blockAllAccountsByUserOrProject(anyLong(), null);
        assertAll(
                () -> assertEquals(accountDtos.size(), accounts.size()),
                () -> assertEquals(AccountStatus.BLOCKED, accountDtos.get(0).getStatus()),
                () -> assertEquals(AccountStatus.BLOCKED, accountDtos.get(1).getStatus())
        );
    }

    @Test
    void blockAllAccountsByProjectId() {
        List<Account> accounts = createAccounts();
        accounts.forEach(account -> account.setStatus(AccountStatus.ACTIVE));

        when(accountRepository.findAllByProjectId(anyLong())).thenReturn(accounts);

        List<AccountDto> accountDtos = service.blockAllAccountsByUserOrProject(null, anyLong());
        assertAll(
                () -> assertEquals(accountDtos.size(), accounts.size()),
                () -> assertEquals(AccountStatus.BLOCKED, accountDtos.get(0).getStatus()),
                () -> assertEquals(AccountStatus.BLOCKED, accountDtos.get(1).getStatus())
        );
    }

    @Test
    void unblockAccount() {
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        AccountDto accountDto = service.unblockAccount(anyLong());

        assertEquals(accountDto.getUserId(), account.getUserId());
        assertEquals(account.getStatus(), accountDto.getStatus());
        verify(accountRepository, times(1)).findById(anyLong());
    }

    @Test
    void unblockAccountNumber() {
        when(accountRepository.findAccountByNumber(anyString())).thenReturn(Optional.of(account));

        AccountDto accountDto = service.unblockAccountNumber(anyString());

        assertEquals(accountDto.getUserId(), account.getUserId());
        assertEquals(account.getStatus(), accountDto.getStatus());
        verify(accountRepository, times(1)).findAccountByNumber(anyString());
    }

    @Test
    void unblockAllAccountsByUserId() {
        List<Account> accounts = createAccounts();
        accounts.forEach(account -> account.setStatus(AccountStatus.BLOCKED));

        when(accountRepository.findAllByUserId(anyLong())).thenReturn(accounts);

        List<AccountDto> accountDtos = service.unblockAllAccountsByUserOrProject(anyLong(), null);
        assertAll(
                () -> assertEquals(accountDtos.size(), accounts.size()),
                () -> assertEquals(AccountStatus.ACTIVE, accountDtos.get(0).getStatus()),
                () -> assertEquals(AccountStatus.ACTIVE, accountDtos.get(1).getStatus())
        );

    }

    @Test
    void unblockAllAccountsByProjectId() {
        List<Account> accounts = createAccounts();
        accounts.forEach(account -> account.setStatus(AccountStatus.BLOCKED));

        when(accountRepository.findAllByProjectId(anyLong())).thenReturn(accounts);

        List<AccountDto> accountDtos = service.unblockAllAccountsByUserOrProject(null, anyLong());
        assertAll(
                () -> assertEquals(accountDtos.size(), accounts.size()),
                () -> assertEquals(AccountStatus.ACTIVE, accountDtos.get(0).getStatus()),
                () -> assertEquals(AccountStatus.ACTIVE, accountDtos.get(1).getStatus())
        );

    }

    @Test
    void closeAccount() {
        account.setStatus(AccountStatus.CLOSED);
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        AccountDto accountDto = service.closeAccount(anyLong());

        assertEquals(accountDto.getUserId(), account.getUserId());
        assertEquals(account.getStatus(), accountDto.getStatus());
        verify(accountRepository, times(1)).findById(anyLong());
    }

    @Test
    void closeAccountNumber() {
        account.setStatus(AccountStatus.CLOSED);
        when(accountRepository.findAccountByNumber(anyString())).thenReturn(Optional.of(account));

        AccountDto accountDto = service.closeAccountNumber(anyString());

        assertEquals(accountDto.getUserId(), account.getUserId());
        assertEquals(account.getStatus(), accountDto.getStatus());
        verify(accountRepository, times(1)).findAccountByNumber(anyString());
    }

    private List<Account> createAccounts() {
        List<Account> accounts = new ArrayList<>();
        Account account1 = new Account();
        Account account2 = new Account();
        accounts.add(account1);
        accounts.add(account2);
        return accounts;
    }
}