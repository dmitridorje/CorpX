package faang.school.accountservice.repository;

import faang.school.accountservice.model.entity.TariffHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TariffHistoryRepository extends JpaRepository<TariffHistory, Long> {

    @Query(value = "SELECT th.savings_account_tariff_id FROM tariff_history th " +
            "WHERE th.savings_account_id = :accountId ORDER BY th.created_at DESC LIMIT 1", nativeQuery = true)
    Optional<Long> findLatestTariffIdBySavingsAccountId(@Param("accountId") Long accountId);
}
