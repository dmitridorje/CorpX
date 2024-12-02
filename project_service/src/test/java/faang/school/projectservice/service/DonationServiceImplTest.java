package faang.school.projectservice.service;

import faang.school.projectservice.client.PaymentServiceClient;
import faang.school.projectservice.config.context.UserContext;
import faang.school.projectservice.mapper.DonationMapper;
import faang.school.projectservice.model.dto.DonationDto;
import faang.school.projectservice.model.dto.PaymentRequestDto;
import faang.school.projectservice.model.dto.PaymentResponseDto;
import faang.school.projectservice.model.entity.Campaign;
import faang.school.projectservice.model.entity.Donation;
import faang.school.projectservice.model.entity.Project;
import faang.school.projectservice.model.enums.Currency;
import faang.school.projectservice.publisher.FundRaisedEventPublisher;
import faang.school.projectservice.repository.CampaignRepository;
import faang.school.projectservice.repository.DonationRepository;
import faang.school.projectservice.repository.ProjectRepository;
import faang.school.projectservice.service.impl.DonationServiceImpl;
import faang.school.projectservice.validator.DonationValidator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DonationServiceImplTest {

    @Mock
    private DonationRepository donationRepository;
    @Mock
    private PaymentServiceClient paymentServiceClient;
    @Mock
    private DonationValidator validator;
    @Mock
    private UserContext userContext;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private DonationMapper donationMapper;
    @Mock
    private FundRaisedEventPublisher fundRaisedEventPublisher;
    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private DonationServiceImpl donationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateDonationSuccess() {
        long userId = 1L;
        DonationDto donationDto = new DonationDto();
        donationDto.setUserId(userId);
        donationDto.setCampaignId(2L);
        donationDto.setAmount(new BigDecimal("100.00"));
        donationDto.setCurrency(Currency.USD);

        Campaign campaign = new Campaign();
        Project project = new Project();
        project.setId(1L);
        campaign.setProject(project);
        PaymentResponseDto paymentResponseDto = new PaymentResponseDto();
        paymentResponseDto.setPaymentNumber(12345L);
        paymentResponseDto.setPaymentCurrency(Currency.USD);

        when(userContext.getUserId()).thenReturn(userId);
        when(campaignRepository.findById(donationDto.getCampaignId())).thenReturn(Optional.of(campaign));
        when(paymentServiceClient.sendPayment(any(PaymentRequestDto.class))).thenReturn(paymentResponseDto);

        Donation donationEntity = new Donation();
        when(donationMapper.toEntity(donationDto)).thenReturn(donationEntity);
        when(donationRepository.save(donationEntity)).thenReturn(donationEntity);
        when(donationMapper.toDto(donationEntity)).thenReturn(donationDto);

        DonationDto result = donationService.create(donationDto);

        assertEquals(donationDto, result);
        verify(validator).validateUser(userId);
        verify(validator).validateUserTheSame(userId, donationDto.getUserId());
        verify(validator).validateCampaignExists(donationDto.getCampaignId());

        ArgumentCaptor<PaymentRequestDto> captor = ArgumentCaptor.forClass(PaymentRequestDto.class);
        verify(paymentServiceClient).sendPayment(captor.capture());
        PaymentRequestDto capturedRequest = captor.getValue();
        assertEquals(donationDto.getAmount(), capturedRequest.amount());
    }

    @Test
    void testCreateDonationCampaignNotFound() {
        long userId = 1L;
        DonationDto donationDto = new DonationDto();
        donationDto.setCampaignId(2L);

        when(userContext.getUserId()).thenReturn(userId);
        when(campaignRepository.findById(donationDto.getCampaignId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> donationService.create(donationDto));
    }

    @Test
    void testGetByIdSuccess() {
        long donationId = 1L;
        long userId = 1L;
        Donation donation = new Donation();
        DonationDto donationDto = new DonationDto();
        donationDto.setId(donationId);

        when(userContext.getUserId()).thenReturn(userId);
        when(donationRepository.findById(donationId)).thenReturn(Optional.of(donation));
        when(donationMapper.toDto(donation)).thenReturn(donationDto);

        DonationDto result = donationService.getById(donationId);

        assertEquals(donationDto, result);
        verify(validator).validateUser(userId);
        verify(donationRepository).findById(donationId);
    }

    @Test
    void testGetByIdDonationNotFound() {
        long donationId = 1L;
        long userId = 1L;

        when(userContext.getUserId()).thenReturn(userId);
        when(donationRepository.findById(donationId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> donationService.getById(donationId));
    }
}
