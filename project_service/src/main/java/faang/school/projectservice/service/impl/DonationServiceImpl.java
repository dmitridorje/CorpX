package faang.school.projectservice.service.impl;

import faang.school.projectservice.client.PaymentServiceClient;
import faang.school.projectservice.config.context.UserContext;
import faang.school.projectservice.mapper.DonationMapper;
import faang.school.projectservice.model.dto.DonationDto;
import faang.school.projectservice.model.dto.DonationFilterDto;
import faang.school.projectservice.model.dto.PaymentRequestDto;
import faang.school.projectservice.model.dto.PaymentResponseDto;
import faang.school.projectservice.model.entity.Campaign;
import faang.school.projectservice.model.entity.Donation;
import faang.school.projectservice.model.entity.Project;
import faang.school.projectservice.model.event.FundRaisedEvent;
import faang.school.projectservice.publisher.FundRaisedEventPublisher;
import faang.school.projectservice.repository.CampaignRepository;
import faang.school.projectservice.repository.DonationRepository;
import faang.school.projectservice.service.DonationService;
import faang.school.projectservice.specification.DonationSpecificationBuilder;
import faang.school.projectservice.validator.DonationValidator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DonationServiceImpl implements DonationService {
    private final DonationRepository donationRepository;
    private final PaymentServiceClient paymentServiceClient;
    private final DonationValidator validator;
    private final UserContext userContext;
    private final CampaignRepository campaignRepository;
    private final DonationMapper donationMapper;
    private final FundRaisedEventPublisher fundRaisedEventPublisher;

    @Transactional
    @Override
    public DonationDto create(DonationDto donationDto) {
        long userId = userContext.getUserId();
        validator.validateUser(userId);
        validator.validateUserTheSame(userId, donationDto.getUserId());
        validator.validateCampaignExists(donationDto.getCampaignId());

        Campaign campaign = campaignRepository.findById(donationDto.getCampaignId()).orElseThrow(() ->
                new EntityNotFoundException(String.format("Campaign with id = %d not found", donationDto.getCampaignId())));

        PaymentResponseDto paymentResponseDto = sendPayment(donationDto, campaign);

        donationDto.setPaymentNumber(paymentResponseDto.getPaymentNumber());
        donationDto.setTargetCurrency(paymentResponseDto.getTargetCurrency());
        Donation savedDonation = donationRepository.save(donationMapper.toEntity(donationDto));

        publishFundRaisedEvent(donationDto, campaign);
        return donationMapper.toDto(savedDonation);
    }

    @Override
    public DonationDto getById(Long id) {
        long userId = userContext.getUserId();
        validator.validateUser(userId);
        Donation donation = donationRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(String.format("Donation with id = %d not found", id)));
        return donationMapper.toDto(donation);
    }

    @Override
    public List<DonationDto> getByFilter(DonationFilterDto filterDto) {
        Specification<Donation> spec = DonationSpecificationBuilder.build(filterDto);
        Sort sort = Sort.by(Sort.Direction.DESC, "donationTime");
        List<Donation> donations = donationRepository.findAll(spec, sort);
        return donationMapper.toDtoList(donations);
    }

    private void publishFundRaisedEvent(DonationDto donationDto, Campaign campaign) {
        Project project = campaign.getProject();
        FundRaisedEvent fundRaisedEvent = new FundRaisedEvent(
                project.getId(),
                donationDto.getUserId(),
                donationDto.getAmount().longValue(),
                LocalDateTime.now());
        fundRaisedEventPublisher.publish(fundRaisedEvent);
    }

    private PaymentResponseDto sendPayment(DonationDto donationDto, Campaign campaign) {
        PaymentRequestDto paymentRequestDto = new PaymentRequestDto(
                donationRepository.getDonationPaymentNumber(),
                donationDto.getAmount(),
                donationDto.getCurrency(),
                campaign.getCurrency());
        return paymentServiceClient.sendPayment(paymentRequestDto);
    }
}
