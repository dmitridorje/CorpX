package faang.school.accountservice.service;

import faang.school.accountservice.model.dto.TariffDto;

import java.math.BigDecimal;

public interface TariffService {
    TariffDto createTariff(TariffDto tariffDto);

    TariffDto updateTariff(Long id, BigDecimal rate);

    TariffDto getTariff(Long id);
}
