package faang.school.accountservice.service.impl;

import faang.school.accountservice.model.entity.AccountNumbersSequence;
import faang.school.accountservice.model.entity.FreeAccountNumber;
import faang.school.accountservice.model.entity.FreeAccountNumberId;
import faang.school.accountservice.model.enums.AccountType;
import faang.school.accountservice.repository.AccountNumbersSequenceRepository;
import faang.school.accountservice.repository.FreeAccountNumbersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FreeAccountNumbersServiceImplTest {
    @Mock
    private FreeAccountNumbersRepository freeAccountNumbersRepository;

    @Mock
    private AccountNumbersSequenceRepository accountNumbersSequenceRepository;

    @InjectMocks
    private FreeAccountNumbersServiceImpl freeAccountNumbersService;

    private AccountType accountType;

    @BeforeEach
    public void setUp() {
        accountType = AccountType.INDIVIDUAL;
    }

    @Test
    @DisplayName("Should save and return new account number")
    public void testAddFreeAccountNumber_Success() {
        when(accountNumbersSequenceRepository.findByAccountType(accountType)).thenReturn(Optional.empty());
        when(accountNumbersSequenceRepository.createAndGetSequence(accountType)).thenReturn(new AccountNumbersSequence());
        when(freeAccountNumbersRepository.save(any())).thenReturn(new FreeAccountNumber());
        when(accountNumbersSequenceRepository.incrementCount(any(), anyLong())).thenReturn(1);

        FreeAccountNumber result = freeAccountNumbersService.addFreeAccountNumber(accountType, true);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("42000000000000000001", result.getId().getNumber()),
                () -> assertEquals(AccountType.INDIVIDUAL, result.getId().getType())
        );

        verify(freeAccountNumbersRepository).save(any());
    }

    @Test
    @DisplayName("Should return existing number")
    public void testGetFreeAccountNumber_NumberExist() {
        FreeAccountNumber existingNumber = new FreeAccountNumber(new FreeAccountNumberId(accountType, "1234567890"));
        when(freeAccountNumbersRepository.getAndDeleteFirst(accountType)).thenReturn(Optional.of(existingNumber));

        freeAccountNumbersService.getFreeAccountNumber(accountType, accountNumber -> {

            assertNotNull(accountNumber);
            assertEquals("1234567890", accountNumber.getId().getNumber());
            assertEquals(AccountType.INDIVIDUAL, accountNumber.getId().getType());
        });
    }

    @Test
    @DisplayName("Should add new number when none exists")
    public void testGetFreeAccountNumber_NoneExists() {
        when(freeAccountNumbersRepository.getAndDeleteFirst(accountType)).thenReturn(Optional.empty());
        when(accountNumbersSequenceRepository.findByAccountType(accountType)).thenReturn(Optional.empty());
        when(accountNumbersSequenceRepository.createAndGetSequence(accountType)).thenReturn(new AccountNumbersSequence());
        when(accountNumbersSequenceRepository.incrementCount(any(), anyLong())).thenReturn(1);

        freeAccountNumbersService.getFreeAccountNumber(accountType, accountNumber -> {

            assertNotNull(accountNumber);
            assertEquals("42000000000000000001", accountNumber.getId().getNumber());
            assertEquals(AccountType.INDIVIDUAL, accountNumber.getId().getType());
        });
    }
}
