package faang.school.accountservice.validator;

import faang.school.accountservice.model.dto.AccountDto;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountControllerValidatorTest {
    private final AccountControllerValidator validator = new AccountControllerValidator();
    private AccountDto accountDto;

    @BeforeEach
    void setUp() {
        accountDto = new AccountDto();
    }

    @Test
    public void testNullDto() {
        ValidationException exception = Assertions.assertThrows(ValidationException.class, () -> {
            validator.checkDto(new AccountDto());
        });

        assertEquals("User or project required. They cannot be present at the same time.", exception.getMessage());
    }

    @Test
    public void testUserAndProjectSet() {
        accountDto.setProjectId(1L);
        accountDto.setUserId(1L);

        ValidationException exception = Assertions.assertThrows(ValidationException.class, () -> {
            validator.checkDto(accountDto);
        });

        assertEquals("User or project required. They cannot be present at the same time.", exception.getMessage());
    }

    @Test
    public void testDtoUserSet() {
        accountDto.setUserId(1L);

        validator.checkDto(accountDto);

        assertDoesNotThrow(() -> validator.checkDto(accountDto));
    }

    @Test
    public void testDtoProjectSet() {
        accountDto.setProjectId(1L);

        validator.checkDto(accountDto);

        assertDoesNotThrow(() -> validator.checkDto(accountDto));
    }

}