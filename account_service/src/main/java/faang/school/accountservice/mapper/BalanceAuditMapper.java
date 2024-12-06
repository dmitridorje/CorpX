package faang.school.accountservice.mapper;

import faang.school.accountservice.model.dto.BalanceAuditDto;
import faang.school.accountservice.model.entity.Balance;
import faang.school.accountservice.model.entity.BalanceAudit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BalanceAuditMapper {

    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "account", ignore = true)
//    @Mapping(target = "request", ignore = true)
    @Mapping(target = "balanceVersion", source = "version")
    BalanceAudit toAuditEntity(Balance balance);

    BalanceAuditDto toDto(BalanceAudit balanceAudit);

    List<BalanceAuditDto> toListAuditDto(List<BalanceAudit> balanceAudits);
}
