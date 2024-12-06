package faang.school.accountservice.service.impl;

import faang.school.accountservice.mapper.TariffMapper;
import faang.school.accountservice.model.dto.TariffDto;
import faang.school.accountservice.model.entity.SavingsAccountRate;
import faang.school.accountservice.model.entity.Tariff;
import faang.school.accountservice.repository.SavingsAccountRateRepository;
import faang.school.accountservice.repository.TariffRepository;
import faang.school.accountservice.service.TariffService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TariffServiceImpl implements TariffService {
    private static final String TARIFF_ID = "Tariff with id ";
    private static final String NOT_FOUND = " not found";
    private final TariffRepository tariffRepository;
    private final TariffMapper tariffMapper;
    private final SavingsAccountRateRepository savingsAccountRateRepository;

    @Transactional
    @Override
    public TariffDto createTariff(TariffDto tariffDto) {
        Tariff tariff = tariffMapper.toEntity(tariffDto);
        tariff = tariffRepository.save(tariff);
        SavingsAccountRate savingsAccountRate = SavingsAccountRate.builder()
                .tariff(tariff)
                .rate(tariffDto.getRate())
                .build();

        tariffDto = tariffMapper.toDto(tariff);
        tariffDto.setRate(savingsAccountRateRepository.save(savingsAccountRate).getRate());

        return tariffDto;
    }

    @Transactional
    @Override
    public TariffDto updateTariff(Long id, BigDecimal rate) {
        Tariff tariff = tariffRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(TARIFF_ID + id + NOT_FOUND));

        SavingsAccountRate savingsAccountRate = SavingsAccountRate.builder()
                .rate(rate)
                .tariff(tariff)
                .build();

        TariffDto tariffDto = tariffMapper.toDto(tariff);
        tariffDto.setRate(savingsAccountRateRepository.save(savingsAccountRate).getRate());

        return tariffDto;
    }

    @Override
    public TariffDto getTariff(Long id) {
        Optional<TariffDto> tariffDto = tariffRepository.findTariffDtoWithDetails(id);
        return tariffDto.orElseThrow(() -> new EntityNotFoundException(TARIFF_ID + id + NOT_FOUND));
    }
}
