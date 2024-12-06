package faang.school.accountservice.service.impl;

import faang.school.accountservice.mapper.TariffMapperImpl;
import faang.school.accountservice.model.dto.TariffDto;
import faang.school.accountservice.model.entity.SavingsAccountRate;
import faang.school.accountservice.model.entity.Tariff;
import faang.school.accountservice.repository.SavingsAccountRateRepository;
import faang.school.accountservice.repository.TariffRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TariffServiceImplTest {

    @Spy
    private TariffMapperImpl tariffMapper;

    @Mock
    private TariffRepository tariffRepository;

    @Mock
    private SavingsAccountRateRepository savingsAccountRateRepository;

    @InjectMocks
    private TariffServiceImpl tariffService;

    @Captor
    private ArgumentCaptor<SavingsAccountRate> savingsAccountRateArgumentCaptor;

    private Tariff tariff;
    private final Long tariffId = 1L;
    private final String tariffName = "tariff1";

    @BeforeEach
    void setUp() {
        tariff = new Tariff();
        tariff.setId(tariffId);
        tariff.setName(tariffName);
    }

    @Test
    public void testCreateTariff() {
        BigDecimal rate = BigDecimal.valueOf(5.5);
        TariffDto tariffDto = new TariffDto();
        tariffDto.setRate(rate);
        tariffDto.setName("tariff1");
        tariffDto.setId(tariffId);
        SavingsAccountRate savingsAccountRate = new SavingsAccountRate();
        savingsAccountRate.setRate(rate);

        when(tariffRepository.save(tariff)).thenReturn(tariff);
        when(savingsAccountRateRepository.save(savingsAccountRateArgumentCaptor.capture()))
                .thenReturn(savingsAccountRate);

        TariffDto resultDto = tariffService.createTariff(tariffDto);

        verify(tariffRepository, times(1)).save(tariff);
        verify(savingsAccountRateRepository, times(1)).save(savingsAccountRateArgumentCaptor.capture());
        assertAll(
                () -> assertEquals(tariffDto.getName(), resultDto.getName()),
                () -> assertEquals(tariffDto.getRate(), resultDto.getRate())
        );
    }

    @Test
    public void testUpdateTariffSuccess() {
        BigDecimal rate = BigDecimal.valueOf(6.0);
        SavingsAccountRate savingsAccountRate = new SavingsAccountRate();
        savingsAccountRate.setRate(rate);
        when(tariffRepository.findById(tariffId)).thenReturn(Optional.ofNullable(tariff));
        when(savingsAccountRateRepository.save(savingsAccountRateArgumentCaptor.capture())).thenReturn(savingsAccountRate);

        TariffDto tariffDto = tariffService.updateTariff(tariffId, rate);

        verify(savingsAccountRateRepository, times(1)).save(savingsAccountRateArgumentCaptor.capture());
        verify(tariffRepository, times(1)).findById(tariffId);
        assertEquals(rate, tariffDto.getRate());
    }

    @Test
    public void testUpdateTariffUnSuccess() {
        Long wrongId = 100_000L;
        BigDecimal rate = BigDecimal.valueOf(6.0);
        when(tariffRepository.findById(wrongId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> tariffService.updateTariff(wrongId, rate));
    }

    @Test
    public void getTariffSuccess() {
        BigDecimal rate = BigDecimal.valueOf(6.7);
        TariffDto tariffDto = new TariffDto();
        tariffDto.setId(tariffId);
        tariffDto.setName(tariffName);
        tariffDto.setRate(rate);
        when(tariffRepository.findTariffDtoWithDetails(tariffId)).thenReturn(Optional.of(tariffDto));

        TariffDto resultDto = tariffService.getTariff(tariffId);

        verify(tariffRepository, times(1)).findTariffDtoWithDetails(tariffId);
        assertAll(
                () -> assertEquals(rate, resultDto.getRate()),
                () -> assertEquals(tariff.getName(), resultDto.getName())
        );
    }

    @Test
    public void getTariffWrongTariffId() {
        Long wrongId = 100_000L;

        assertThrows(EntityNotFoundException.class, () -> tariffService.getTariff(wrongId));
    }

}