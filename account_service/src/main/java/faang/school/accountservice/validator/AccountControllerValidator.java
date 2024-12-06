package faang.school.accountservice.validator;

import faang.school.accountservice.model.dto.AccountDto;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class AccountControllerValidator {
    private final String MESSAGE = "User or project required. They cannot be present at the same time.";

    public void checkDto(AccountDto dto) {
        if (dto == null || (dto.getUserId() == null && dto.getProjectId() == null) ||
                (dto.getUserId() != null && dto.getProjectId() != null)) {
            throw new ValidationException(MESSAGE);
        }
    }

    public void checkParams(Long userId, Long projectId) {
        if ((userId == null && projectId == null) || (userId != null && projectId != null)) {
            throw new ValidationException(MESSAGE);
        }
    }
}
