package faang.school.accountservice.repository;

import faang.school.accountservice.model.dto.TariffDto;
import faang.school.accountservice.model.entity.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TariffRepository extends JpaRepository<Tariff, Long> {

    @Query(value = "SELECT new faang.school.accountservice.model.dto.TariffDto(t.id, t.name, sar.rate) FROM Tariff t " +
            "LEFT JOIN SavingsAccountRate sar ON t.id = sar.tariff.id " +
            "WHERE t.id = :id ORDER BY sar.createdAt DESC LIMIT 1")
    Optional<TariffDto> findTariffDtoWithDetails(@Param("id") Long id);
}
