package faang.school.accountservice.model.dto;

import faang.school.accountservice.model.enums.AccountStatus;
import faang.school.accountservice.model.enums.AccountType;
import faang.school.accountservice.model.enums.Currency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AccountDto {

    @Null(groups = {Create.class})
    @NotNull(groups = {Created.class})
    private Long id;

    @Null(groups = {Create.class})
    @NotNull(groups = {Created.class})
    private String number;

    private Long projectId;

    private Long userId;

    @NotNull(groups = {Create.class, Created.class})
    private AccountType type;

    @NotNull(groups = {Create.class, Created.class})
    private Currency currency;

    @NotNull(groups = {Create.class, Created.class})
    private AccountStatus status;

    @Null(groups = {Create.class})
    @NotNull(groups = {Created.class})
    private LocalDateTime createdAt;

    @Null(groups = {Create.class})
    @NotNull(groups = {Created.class})
    private LocalDateTime updatedAt;

    @Null(groups = {Create.class})
    private LocalDateTime closedAt;

    @Null(groups = {Create.class})
    @NotNull(groups = {Created.class})
    private Long balanceId;

    public interface Create {
    }

    public interface Created {
    }
}
