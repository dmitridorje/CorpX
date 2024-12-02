package faang.school.projectservice.mapper;

import faang.school.projectservice.model.dto.DonationDto;
import faang.school.projectservice.model.entity.Donation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DonationMapper {

    @Mapping(source = "campaign.id", target = "campaignId")
    DonationDto toDto(Donation donation);

    @Mapping(source = "campaignId", target = "campaign.id")
    Donation toEntity(DonationDto donationDto);

    List<DonationDto> toDtoList(List<Donation> donations);

    List<Donation> toEntityList(List<DonationDto> donationDtos);
}
