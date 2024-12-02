package faang.school.projectservice.service;

import faang.school.projectservice.model.dto.DonationDto;
import faang.school.projectservice.model.dto.DonationFilterDto;

import java.util.List;

public interface DonationService {

    DonationDto create(DonationDto donationDto);

    DonationDto getById(Long id);

    List<DonationDto> getByFilter(DonationFilterDto filterDto);
}
