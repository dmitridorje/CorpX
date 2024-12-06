package faang.school.accountservice.scheduler;

import faang.school.accountservice.config.scheduler.ScheduledAccountConfig;
import faang.school.accountservice.model.enums.AccountType;
import faang.school.accountservice.repository.FreeAccountNumbersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
class FreeNumbersGenerationSchedulerIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:latest")
                    .withDatabaseName("testdb")
                    .withUsername("admin")
                    .withPassword("admin")
                    .withInitScript("schema_for_FreeAccountNumbersService.sql");

    @DynamicPropertySource
    static void overrideSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);
        registry.add("spring.liquibase.enabled", () -> false);
    }

    @Autowired
    private FreeNumbersGenerationScheduler scheduledNumbersGenerationService;

    @Autowired
    private FreeAccountNumbersRepository freeAccountNumbersRepository;

    @Autowired
    private ScheduledAccountConfig scheduledAccountConfig;

    @Test
    public void testScheduledAccountConfigValues() {
        assertThat(scheduledAccountConfig.getAccounts().get("INDIVIDUAL").getTargetAmount()).isEqualTo(4);
        assertThat(scheduledAccountConfig.getAccounts().get("CORPORATE").getTargetAmount()).isEqualTo(3);
        assertThat(scheduledAccountConfig.getAccounts().get("SAVINGS").getTargetAmount()).isEqualTo(2);
    }

    @Test
    public void testGenerateAccountNumberOnSchedule() {
        scheduledNumbersGenerationService.generateAccountNumbersOnSchedule();

        int checkingCount = freeAccountNumbersRepository.countById_Type(AccountType.INDIVIDUAL);
        assertThat(checkingCount).isEqualTo(scheduledAccountConfig.getAccounts().get("INDIVIDUAL").getTargetAmount());

        int corporateCount = freeAccountNumbersRepository.countById_Type(AccountType.CORPORATE);
        assertThat(corporateCount).isEqualTo(scheduledAccountConfig.getAccounts().get("CORPORATE").getTargetAmount());

        int savingsCount = freeAccountNumbersRepository.countById_Type(AccountType.SAVINGS);
        assertThat(savingsCount).isEqualTo(scheduledAccountConfig.getAccounts().get("SAVINGS").getTargetAmount());
    }
}
