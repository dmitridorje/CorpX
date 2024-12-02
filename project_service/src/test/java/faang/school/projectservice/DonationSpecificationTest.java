package faang.school.projectservice;

import faang.school.projectservice.model.entity.Donation;
import faang.school.projectservice.model.enums.Currency;
import faang.school.projectservice.model.dto.DonationFilterDto;
import faang.school.projectservice.specification.DonationSpecificationBuilder;
import faang.school.projectservice.specification.DonationSpecifications;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class DonationSpecificationTest {
    @Test
    void testDonationDateAfterSpecification() {
        LocalDateTime testDate = LocalDateTime.now().minusDays(1);
        Specification<Donation> spec = DonationSpecifications.donationDateAfter(testDate);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<?> cq = mock(CriteriaQuery.class);
        Root<Donation> root = mock(Root.class);
        Path path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("donationTime")).thenReturn(path);
        when(cb.greaterThanOrEqualTo(path, testDate)).thenReturn(predicate);

        assertNotNull(spec.toPredicate(root, cq, cb));
    }

    @Test
    void testBuildSpecificationWithAllFilters() {
        DonationFilterDto filterDto = new DonationFilterDto();
        filterDto.setDonationDateAfter(LocalDateTime.now().minusDays(2));
        filterDto.setDonationDateBefore(LocalDateTime.now());
        filterDto.setCurrency(Currency.USD);
        filterDto.setMinAmount(new BigDecimal("50"));
        filterDto.setMaxAmount(new BigDecimal("500"));

        Specification<Donation> spec = DonationSpecificationBuilder.build(filterDto);

        assertNotNull(spec);
    }

    @Test
    void testCurrencyEqualsSpecification() {
        Currency testCurrency = Currency.USD;
        Specification<Donation> spec = DonationSpecifications.currencyEquals(testCurrency);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<?> cq = mock(CriteriaQuery.class);
        Root<Donation> root = mock(Root.class);
        Path path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("currency")).thenReturn(path);
        when(cb.equal(path, testCurrency)).thenReturn(predicate);

        assertNotNull(spec.toPredicate(root, cq, cb));
    }

    @Test
    void testAmountGreaterThanOrEqualToSpecification() {
        BigDecimal minAmount = new BigDecimal("100");
        Specification<Donation> spec = DonationSpecifications.amountGreaterThanOrEqualTo(minAmount);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<?> cq = mock(CriteriaQuery.class);
        Root<Donation> root = mock(Root.class);
        Path path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("amount")).thenReturn(path);
        when(cb.greaterThanOrEqualTo(path, minAmount)).thenReturn(predicate);

        assertNotNull(spec.toPredicate(root, cq, cb));
    }

    @Test
    void testAmountLessThanOrEqualToSpecification() {
        BigDecimal maxAmount = new BigDecimal("200");
        Specification<Donation> spec = DonationSpecifications.amountLessThanOrEqualTo(maxAmount);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<?> cq = mock(CriteriaQuery.class);
        Root<Donation> root = mock(Root.class);
        Path path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("amount")).thenReturn(path);
        when(cb.lessThanOrEqualTo(path, maxAmount)).thenReturn(predicate);

        assertNotNull(spec.toPredicate(root, cq, cb));
    }
}
