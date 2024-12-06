package faang.school.accountservice.service.impl;

import faang.school.accountservice.model.entity.AccountNumbersSequence;
import faang.school.accountservice.model.entity.FreeAccountNumber;
import faang.school.accountservice.model.entity.FreeAccountNumberId;
import faang.school.accountservice.model.enums.AccountType;
import faang.school.accountservice.repository.AccountNumbersSequenceRepository;
import faang.school.accountservice.repository.FreeAccountNumbersRepository;
import faang.school.accountservice.service.FreeAccountNumbersService;
import faang.school.accountservice.util.FreeNumberGeneratorUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class FreeAccountNumbersServiceImpl implements FreeAccountNumbersService {

    private final FreeAccountNumbersRepository freeAccountNumbersRepository;
    private final AccountNumbersSequenceRepository accountNumbersSequenceRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    @Retryable(retryFor = OptimisticLockingFailureException.class, backoff = @Backoff(delay = 5000L))
    public FreeAccountNumber addFreeAccountNumber(AccountType accountType, boolean saveToDatabase) {
        log.info("Attempting to add a new free account number for account type: {}", accountType);

        AccountNumbersSequence accountNumbersSequence = accountNumbersSequenceRepository.findByAccountType(accountType)
                .orElseGet(() -> {
                    log.info("Account numbers sequence not found. Creating a new one for account type: {}", accountType);
                    return accountNumbersSequenceRepository.createAndGetSequence(accountType);
                });

        long version = accountNumbersSequence.getVersion();
        int rowsAffected = accountNumbersSequenceRepository.incrementCount(accountType, version);

        if (rowsAffected == 0) {
            log.error("Unable to increment account number for account type: {}", accountType);
            throw new OptimisticLockingFailureException(
                    "Unable to increment account number for accountType: " + accountType);
        }

        int accountCode = accountType.getType();
        int accountLength = accountType.getLength();
        long count = accountNumbersSequence.getCount() + 1;

        String value = FreeNumberGeneratorUtil.generateAccountNumber(accountCode, accountLength, count);
        FreeAccountNumber newFreeAccountNumber = new FreeAccountNumber(new FreeAccountNumberId(accountType, value));

        if (saveToDatabase) {
            freeAccountNumbersRepository.save(newFreeAccountNumber);
        }

        return newFreeAccountNumber;
    }

    @Override
    @Transactional
    @Retryable(retryFor = OptimisticLockingFailureException.class, backoff = @Backoff(delay = 5000L))
    public void getFreeAccountNumber(AccountType accountType, Consumer<FreeAccountNumber> action) {
        log.info("Attempting to retrieve a free account number for account type: {}", accountType);

        FreeAccountNumber freeAccountNumber = freeAccountNumbersRepository.getAndDeleteFirst(accountType)
                .orElseGet(() -> {
                    log.info("No free account number found. Adding a new one for account type: {}", accountType);
                    return addFreeAccountNumber(accountType, false);
                });

        action.accept(freeAccountNumber);
        log.info("Successfully retrieved free account number: {} for account type: {}",
                freeAccountNumber.getId().getNumber(), accountType);
    }

    @Transactional
    public void generateAndSaveAccountNumbers(AccountType accountType, int amount) {
        List<FreeAccountNumber> freeAccountNumbers = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
                FreeAccountNumber newAccountNumber = addFreeAccountNumber(accountType, false);
                freeAccountNumbers.add(newAccountNumber);
                entityManager.clear();
        }

        freeAccountNumbersRepository.saveAll(freeAccountNumbers);
        log.info("Successfully generated {} free account numbers for account type: {}",
                freeAccountNumbers.size(), accountType);
    }

    @Override
    @Transactional
    public void ensureMinimumAccountNumbers(AccountType accountType, int minAmount) {
        int existingCount = freeAccountNumbersRepository.countById_Type(accountType);
        int toGenerate = minAmount - existingCount;

        if (toGenerate > 0) {
            log.info("Generating {} free account numbers for account type: {}", toGenerate, accountType);
            generateAndSaveAccountNumbers(accountType, toGenerate);
        } else {
            log.info("No additional free account numbers need to be generated for account type: {}", accountType);
        }
    }
}
