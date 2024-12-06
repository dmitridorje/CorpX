package faang.school.accountservice.model.entity;

import faang.school.accountservice.model.enums.AccountType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class FreeAccountNumberId {

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 32)
    private AccountType type;

    @Column(name = "number", nullable = false, length = 20)
    @Size(min = 12, max = 20, message = "The number must be 12 to 20 characters long.")
    private String number;
}
