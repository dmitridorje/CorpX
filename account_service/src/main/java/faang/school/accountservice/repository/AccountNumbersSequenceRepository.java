package faang.school.accountservice.repository;

import faang.school.accountservice.exception.SequenceNotFoundException;
import faang.school.accountservice.model.entity.AccountNumbersSequence;
import faang.school.accountservice.model.enums.AccountType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountNumbersSequenceRepository extends JpaRepository<AccountNumbersSequence, Long> {

    Optional<AccountNumbersSequence> findByAccountType(AccountType accountType);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
        INSERT INTO account_numbers_sequence (account_type)
        VALUES (:#{#accountType.toString()})
        """)
    void createSequence(AccountType accountType);

    @Transactional
    default AccountNumbersSequence createAndGetSequence(AccountType accountType) {
        createSequence(accountType);
        return findByAccountType(accountType)
                .orElseThrow(() -> new SequenceNotFoundException("Sequence not found for account type: " + accountType));
    }

    @Transactional
    @Modifying
    @Query("UPDATE AccountNumbersSequence a " +
            "SET a.count = a.count + 1 " +
            "WHERE a.accountType = :accountType AND a.version = :version")
    int incrementCount(@Param("accountType") AccountType accountType, @Param("version") long version);
}
