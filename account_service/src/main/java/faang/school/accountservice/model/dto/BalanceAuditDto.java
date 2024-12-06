package faang.school.accountservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class BalanceAuditDto {
    private Long id;
    private Long accountId;
    private Long balanceVersion;
    private Long authorizedBalance;
    private Long actualBalance;
    private Long requestId;
    private LocalDateTime createdAt;
}
