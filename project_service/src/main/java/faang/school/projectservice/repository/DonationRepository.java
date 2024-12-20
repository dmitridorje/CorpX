package faang.school.projectservice.repository;

import faang.school.projectservice.model.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long>, JpaSpecificationExecutor<Donation> {
    Optional<Donation> findByIdAndUserId(Long id, Long userId);

    List<Donation> findAllByUserId(Long userId);

    @Query(nativeQuery = true, value = "SELECT nextval('donation_payment_number_sequence')")
    Long getDonationPaymentNumber();
}
