package faang.school.accountservice.service;

import faang.school.accountservice.model.dto.AccountDto;

import java.util.List;

public interface AccountService {
    AccountDto getAccount(Long id);

    AccountDto getAccountNumber(String number);

    AccountDto openAccount(AccountDto accountDto);

    AccountDto blockAccount(Long id);

    AccountDto blockAccountNumber(String number);

    List<AccountDto> blockAllAccountsByUserOrProject(Long userId, Long projectId);

    AccountDto unblockAccount(Long id);

    AccountDto unblockAccountNumber(String number);

    List<AccountDto> unblockAllAccountsByUserOrProject(Long userId, Long projectId);

    AccountDto closeAccount(Long id);

    AccountDto closeAccountNumber(String number);
}
