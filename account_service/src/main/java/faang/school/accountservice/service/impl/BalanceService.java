package faang.school.accountservice.service.impl;

import faang.school.accountservice.model.dto.BalanceDto;

public interface BalanceService {

    BalanceDto getBalanceByAccountId(Long accountId);

    BalanceDto updateBalance(BalanceDto balanceToUpdateDto);
}
