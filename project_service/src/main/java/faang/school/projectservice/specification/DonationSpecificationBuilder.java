package faang.school.projectservice.specification;

import faang.school.projectservice.model.dto.DonationFilterDto;
import faang.school.projectservice.model.entity.Donation;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

public class DonationSpecificationBuilder {

    public static Specification<Donation> build(DonationFilterDto filterDto) {
        Specification<Donation> specification = Specification.where(null);

        if (filterDto.getDonationDateAfter() != null) {
            specification = specification.and(DonationSpecifications.donationDateAfter(filterDto.getDonationDateAfter()));
        }

        if (filterDto.getDonationDateBefore() != null) {
            specification = specification.and(DonationSpecifications.donationDateBefore(filterDto.getDonationDateBefore()));
        }

        if (filterDto.getCurrency() != null) {
            specification = specification.and(DonationSpecifications.currencyEquals(filterDto.getCurrency()));
        }

        if (filterDto.getMinAmount() != null) {
            specification = specification.and(DonationSpecifications.amountGreaterThanOrEqualTo(filterDto.getMinAmount()));
        }

        if (filterDto.getMaxAmount() != null) {
            specification = specification.and(DonationSpecifications.amountLessThanOrEqualTo(filterDto.getMaxAmount()));
        }

        return specification;
    }
}
