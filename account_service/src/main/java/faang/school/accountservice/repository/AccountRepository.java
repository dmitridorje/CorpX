package faang.school.accountservice.repository;

import faang.school.accountservice.model.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findAccountByNumber(String number);

    List<Account> findAllByUserId(Long id);

    List<Account> findAllByProjectId(Long id);

    @Query(value = "SELECT a.number FROM account a WHERE a.user_id = :userId", nativeQuery = true)
    List<String> findNumbersByUserId(@Param("userId") Long userId);

}
