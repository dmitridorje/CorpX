package faang.school.accountservice.repository;

import faang.school.accountservice.model.entity.FreeAccountNumber;
import faang.school.accountservice.model.entity.FreeAccountNumberId;
import faang.school.accountservice.model.enums.AccountType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FreeAccountNumbersRepository extends JpaRepository<FreeAccountNumber, FreeAccountNumberId> {

    @Transactional(readOnly = true)
    @Query(value = """
                SELECT * FROM free_account_number fa
                WHERE fa.account_type = :#{#accountType.toString()}
                ORDER BY fa.number ASC
                LIMIT 1
            """, nativeQuery = true)
    Optional<FreeAccountNumber> getFirstByAccountType(@Param("accountType") AccountType accountType);

    @Transactional
    @Modifying
    @Query(value = """
                DELETE FROM free_account_number 
                WHERE account_type = :#{#type.toString()} AND number = :number
            """, nativeQuery = true)
    int deleteByIdAndReturnRowsAffected(@Param("type") AccountType type, @Param("number") String number);

    @Transactional
    default Optional<FreeAccountNumber> getAndDeleteFirst(AccountType accountType) {
        Optional<FreeAccountNumber> optionalFreeAccountNumber = getFirstByAccountType(accountType);

        if (optionalFreeAccountNumber.isEmpty()) {
            return Optional.empty();
        }

        FreeAccountNumber freeAccountNumber = optionalFreeAccountNumber.get();
        FreeAccountNumberId id = freeAccountNumber.getId();

        int rowsAffected = deleteByIdAndReturnRowsAffected(id.getType(), id.getNumber());

        if (rowsAffected > 0) {
            return Optional.of(freeAccountNumber);
        } else {
            throw new RuntimeException("Failed to delete free account number with id: " + id);
        }
    }

    int countById_Type(AccountType type);
}
