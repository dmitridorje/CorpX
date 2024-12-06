package faang.school.accountservice.repository;

import faang.school.accountservice.model.dto.SavingsAccountDto;
import faang.school.accountservice.model.entity.SavingsAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavingsAccountRepository extends JpaRepository<SavingsAccount, Long> {

    @Query(value = "SELECT new faang.school.accountservice.model.dto.SavingsAccountDto(sa.id, sa.account.id, " +
            "sar.tariff.id, sar.rate, sa.lastDatePercent, sa.createdAt, sa.updatedAt) " +
            "FROM SavingsAccount sa " +
            "LEFT JOIN TariffHistory th ON th.savingsAccount.id = sa.id " +
            "LEFT JOIN SavingsAccountRate sar ON sar.tariff.id = th.tariff.id " +
            "WHERE sa.id  = :id ORDER BY sar.createdAt DESC LIMIT 1")
    Optional<SavingsAccountDto> findSavingsAccountWithDetails(@Param("id") Long id);

    @Query(value = "WITH last_tariff_history AS ( " +
            "    SELECT th.*, " +
            "           ROW_NUMBER() OVER(PARTITION BY th.savings_account_id ORDER BY th.created_at DESC) AS rn " +
            "    FROM tariff_history th " +
            "), " +
            "last_savings_account_rate AS ( " +
            "    SELECT sar.*, " +
            "           ROW_NUMBER() OVER(PARTITION BY sar.tariff_id ORDER BY sar.created_at DESC) AS rn " +
            "    FROM savings_account_rate sar " +
            ") " +
            "SELECT b.id AS balance_id, last_sar.rate, sa.id " +
            "FROM savings_account sa " +
            "LEFT JOIN account a ON a.number = sa.account_number " +
            "LEFT JOIN balance b ON b.account_id = a.id " +
            "LEFT JOIN last_tariff_history last_th ON last_th.savings_account_id = sa.id AND last_th.rn = 1 " +
            "LEFT JOIN last_savings_account_rate last_sar ON last_sar.tariff_id = last_th.savings_account_tariff_id AND last_sar.rn = 1",
            nativeQuery = true)
    List<Object[]> findBalanceAndRate();

    @Query(value = "WITH last_tariff_history AS ( " +
            "    SELECT th.*, " +
            "           ROW_NUMBER() OVER(PARTITION BY th.savings_account_id ORDER BY th.created_at DESC) AS rn " +
            "    FROM tariff_history th " +
            "), " +
            "last_savings_account_rate AS ( " +
            "    SELECT sar.*, " +
            "           ROW_NUMBER() OVER(PARTITION BY sar.tariff_id ORDER BY sar.created_at DESC) AS rn " +
            "    FROM savings_account_rate sar " +
            ") " +
            "SELECT sa.id, last_th.id, last_sar.rate, sa.last_date_percent, sa.created_at, sa.updated_at " +
            "FROM savings_account sa " +
//            "LEFT JOIN account a ON a.number = sa.account_number " +
//            "LEFT JOIN balance b ON b.account_id = a.id " +
            "LEFT JOIN last_tariff_history last_th ON last_th.savings_account_id = sa.id AND last_th.rn = 1 " +
            "LEFT JOIN last_savings_account_rate last_sar ON last_sar.tariff_id = last_th.savings_account_tariff_id AND last_sar.rn = 1 " +
            "WHERE sa.account_number IN :numbers",
            nativeQuery = true)
    List<Object[]> getSavingsAccountsWithLastTariffRate(@Param("numbers") List<String> numbers);

    @Query(value = "SELECT sa.* FROM savings_account sa " +
            "JOIN account a ON sa.account_number = a.number " +
            "WHERE a.user_id = :userId", nativeQuery = true)
    List<SavingsAccount> findSavingsAccountsByUserId(@Param("userId") Long userId);
}
