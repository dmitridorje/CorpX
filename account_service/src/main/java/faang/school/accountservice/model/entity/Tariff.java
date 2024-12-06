package faang.school.accountservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tariff")
public class Tariff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tariff_name")
    private String name;

    @OneToMany(mappedBy = "tariff", cascade = CascadeType.ALL)
    private List<SavingsAccountRate> savingsAccountRates;

    @OneToMany(mappedBy = "tariff")
    List<TariffHistory> tariffHistories;
}
