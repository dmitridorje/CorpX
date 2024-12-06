package faang.school.accountservice.mapper;

import faang.school.accountservice.model.dto.TariffDto;
import faang.school.accountservice.model.entity.Tariff;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface TariffMapper {
    Tariff toEntity(TariffDto tariffDto);

    TariffDto toDto(Tariff tariff);
}
