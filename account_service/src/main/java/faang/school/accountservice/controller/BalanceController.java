package faang.school.accountservice.controller;

import faang.school.accountservice.model.dto.BalanceDto;
import faang.school.accountservice.service.impl.BalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/balance")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping("/{accountId}")
    public BalanceDto getBalance(@PathVariable Long accountId) {
        log.info("Try get balance for account with id: '{}'", accountId);
        BalanceDto balanceDto = balanceService.getBalanceByAccountId(accountId);
        log.info("Balance was received: {}", balanceDto);
        return balanceDto;
    }

    @PostMapping()
    public BalanceDto updateBalance(@RequestBody @Validated(BalanceDto.UpdateRequest.class) BalanceDto balanceToUpdateDto) {
        log.info("Try update balance with ID: {}", balanceToUpdateDto.getId());
        BalanceDto balanceDto = balanceService.updateBalance(balanceToUpdateDto);
        log.info("Balance updated: {}", balanceDto);
        return balanceDto;
    }
}
