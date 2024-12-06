package faang.school.accountservice.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BalanceDto {

    @NotNull(groups = {GetResponse.class, UpdateRequest.class})
    private Long id;

    @NotNull(groups = {GetResponse.class})
    @Null(groups = {UpdateRequest.class})
    private Long accountId;

    @NotNull(groups = {GetResponse.class, UpdateRequest.class})
    private BigDecimal authorizedBalance;

    @NotNull(groups = {GetResponse.class, UpdateRequest.class})
    private BigDecimal actualBalance;

    @NotNull(groups = {GetResponse.class})
    @Null(groups = {UpdateRequest.class})
    private LocalDateTime createdAt;

    @NotNull(groups = {GetResponse.class})
    @Null(groups = {UpdateRequest.class})
    private LocalDateTime updatedAt;

    public interface GetResponse {
    }

    public interface UpdateRequest {
    }
}
