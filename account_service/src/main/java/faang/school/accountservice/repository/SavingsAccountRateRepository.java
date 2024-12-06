package faang.school.accountservice.repository;

import faang.school.accountservice.model.entity.SavingsAccountRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface SavingsAccountRateRepository extends JpaRepository<SavingsAccountRate, Long> {
    @Query(value = "SELECT sar.rate FROM savings_account_rate sar " +
            "WHERE sar.tariff_id = :tariffId ORDER BY sar.created_at DESC LIMIT 1", nativeQuery = true)
    Optional<BigDecimal> findLatestRateIdByTariffId(@Param("tariffId") Long tariffId);
}
