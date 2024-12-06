package faang.school.accountservice.controller;

import faang.school.accountservice.model.dto.SavingsAccountDto;
import faang.school.accountservice.util.ContainerCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class SavingsAccountControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Container
    private static final PostgreSQLContainer<?> postgresContainer = ContainerCreator.POSTGRES_CONTAINER;

    @DynamicPropertySource
    static void overrideSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);
        registry.add("spring.liquibase.enabled", () -> false);
    }

    @Test
    void testGetSavingsAccountById() throws Exception {
        Long id = 2L;

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/savings-account/{id}", id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.accountId").value(2))
                .andExpect(jsonPath("$.rate").value("2.4"))
                .andExpect(jsonPath("$.tariffId").value("3"))
                .andExpect(jsonPath("$.lastDatePercent").hasJsonPath())
                .andReturn();
    }

    @Test
    void testGetSavingsAccountByUserId() throws Exception {
        long userId = 2L;

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/savings-account")
                        .param("userId", Long.toString(userId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].rate").value("2.4"))
                .andExpect(jsonPath("$[0].tariffId").value("3"))
                .andExpect(jsonPath("$[0].lastDatePercent").hasJsonPath())
                .andReturn();
    }

    @Test
    void testOpenSavingsAccount() throws Exception {
        SavingsAccountDto savingsAccountDto = new SavingsAccountDto();
        savingsAccountDto.setAccountId(5L);
        savingsAccountDto.setTariffId(2L);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(savingsAccountDto);

        mockMvc.perform(post("/api/v1/savings-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.accountId").value(5))
                .andExpect(jsonPath("$.tariffId").value(2))
                .andReturn();
    }
}
