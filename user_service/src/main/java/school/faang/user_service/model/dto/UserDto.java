package school.faang.user_service.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.faang.user_service.model.enums.PreferredContact;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    @NotNull
    private Long id;
    private boolean active;
    private String email;
    private String telegramUserId;
    private String username;
    private String phone;
    private PreferredContact preference;
    private List<Long> followees;
}
