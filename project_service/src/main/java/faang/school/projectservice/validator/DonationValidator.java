package faang.school.projectservice.validator;

import faang.school.projectservice.client.UserServiceClient;
import faang.school.projectservice.exception.DataValidationException;
import faang.school.projectservice.model.dto.UserDto;
import faang.school.projectservice.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DonationValidator {
    private final UserServiceClient userServiceClient;
    private final CampaignRepository campaignRepository;

    public void validateUser(long userId) {
        if (userId <= 0) {
            throw new DataValidationException(String.format("User's id can't be equal %d", userId));
        }
        UserDto user = userServiceClient.getUser(userId);
        if (user == null) {
            throw new DataValidationException(String.format("The user must exist in the system, userId = %d", userId));
        }
    }

    public void validateUserTheSame(long userId, Long userIdFromDto) {
        if (userId != userIdFromDto) {
            throw new DataValidationException("You can donation only from your name");
        }
    }

    public void validateCampaignExists(Long campaignId) {
        if (!campaignRepository.existsById(campaignId)) {
            throw new DataValidationException(String.format("Campaign with id = %d doesn't exist", campaignId));
        }
    }
}
