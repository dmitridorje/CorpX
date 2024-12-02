package faang.school.projectservice.validator;

import faang.school.projectservice.client.UserServiceClient;
import faang.school.projectservice.exception.DataValidationException;
import faang.school.projectservice.model.dto.UserDto;
import faang.school.projectservice.repository.CampaignRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class DonationValidatorTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private CampaignRepository campaignRepository;

    @InjectMocks
    private DonationValidator donationValidator;

    @BeforeEach
    void setUp() {
        // Вы можете здесь проинициализировать моки, если это необходимо для каждого теста
    }

    @Test
    void validateUserThrowsExceptionForInvalidUserId() {
        assertThrows(DataValidationException.class, () -> donationValidator.validateUser(-1));
    }

    @Test
    void validateUserThrowsExceptionWhenUserDoesNotExist() {
        when(userServiceClient.getUser(anyLong())).thenReturn(null);
        assertThrows(DataValidationException.class, () -> donationValidator.validateUser(1));
    }

    @Test
    void validateUserTheSameThrowsExceptionWhenUserIdsDoNotMatch() {
        assertThrows(DataValidationException.class, () -> donationValidator.validateUserTheSame(1L, 2L));
    }

    @Test
    void validateCampaignExistsThrowsExceptionWhenCampaignDoesNotExist() {
        when(campaignRepository.existsById(anyLong())).thenReturn(false);
        assertThrows(DataValidationException.class, () -> donationValidator.validateCampaignExists(1L));
    }

    // Дополнительные тесты могут быть написаны для проверки корректной работы методов при валидных данных
}
