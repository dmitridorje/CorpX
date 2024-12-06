package faang.school.accountservice.mapper;

import faang.school.accountservice.model.dto.AccountDto;
import faang.school.accountservice.model.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {

    @Mapping(target = "balanceId", source = "balance.id")
    AccountDto accountToAccountDto(Account account);

    Account accountDtoToAccount(AccountDto accountDto);

    List<AccountDto> accountListToAccountDtoList(List<Account> accountList);
}
