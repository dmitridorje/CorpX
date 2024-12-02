package faang.school.projectservice.specification;

import faang.school.projectservice.model.entity.Donation;
import faang.school.projectservice.model.enums.Currency;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DonationSpecifications {

    public static Specification<Donation> donationDateAfter(LocalDateTime date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("donationTime"), date);
    }

    public static Specification<Donation> donationDateBefore(LocalDateTime date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("donationTime"), date);
    }

    public static Specification<Donation> currencyEquals(Currency currency) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("currency"), currency);
    }

    public static Specification<Donation> amountGreaterThanOrEqualTo(BigDecimal minAmount) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), minAmount);
    }

    public static Specification<Donation> amountLessThanOrEqualTo(BigDecimal maxAmount) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("amount"), maxAmount);
    }
}
