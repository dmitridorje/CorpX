package faang.school.accountservice.controller;

import faang.school.accountservice.model.dto.AccountDto;
import faang.school.accountservice.model.enums.AccountStatus;
import faang.school.accountservice.model.enums.AccountType;
import faang.school.accountservice.model.enums.Currency;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class AccountControllerIntegrationTest {

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
    void testGetAccountById() throws Exception {
        Long id = 5L;

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/account/{id}", id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.userId").value(3))
                .andExpect(jsonPath("$.number").value("59728975298"))
                .andExpect(jsonPath("$.status").value(AccountStatus.ACTIVE.toString()))
                .andExpect(jsonPath("$.currency").value(Currency.RUB.toString()))
                .andReturn();
    }

    @Test
    void testGetAccountByNumber() throws Exception {
        String number = "2385627836527863";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/account/number/{number}", number))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.projectId").value(2))
                .andExpect(jsonPath("$.number").value(number))
                .andExpect(jsonPath("$.status").value(AccountStatus.ACTIVE.toString()))
                .andExpect(jsonPath("$.currency").value(Currency.RUB.toString()))
                .andExpect(jsonPath("$.type").value(AccountType.BUSINESS.toString()))
                .andReturn();
    }

    @Test
    void testOpenAccountWithUserId() throws Exception {
        AccountDto accountDto = new AccountDto();
        accountDto.setUserId(5L);
        accountDto.setStatus(AccountStatus.ACTIVE);
        accountDto.setCurrency(Currency.EUR);
        accountDto.setType(AccountType.BUSINESS);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(accountDto);

        mockMvc.perform(post("/api/v1/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.userId").value(5L))
                .andExpect(jsonPath("$.status").value(accountDto.getStatus().toString()))
                .andExpect(jsonPath("$.currency").value(accountDto.getCurrency().toString()))
                .andExpect(jsonPath("$.type").value(accountDto.getType().toString()))
                .andReturn();
    }

    @Test
    void testBlockAccountById() throws Exception {
        Long id = 7L;

        mockMvc.perform(put("/api/v1/account/block/{id}", id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.projectId").value(1L))
                .andExpect(jsonPath("$.status").value(AccountStatus.BLOCKED.toString()))
                .andExpect(jsonPath("$.number").value("23892656235"))
                .andReturn();
    }

    @Test
    void testBlockAccountByNumber() throws Exception {
        String number = "59728975298";

        mockMvc.perform(put("/api/v1/account/block/number/{number}", number))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.userId").value(3))
                .andExpect(jsonPath("$.status").value(AccountStatus.BLOCKED.toString()))
                .andExpect(jsonPath("$.number").value(number))
                .andReturn();
    }

    @Test
    void testBlockAccountsByUserId() throws Exception {

        mockMvc.perform(put("/api/v1/account/block")
                        .param("userId", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].status").value(AccountStatus.BLOCKED.toString()))
                .andExpect(jsonPath("$[1].id").value(4))
                .andExpect(jsonPath("$[1].status").value(AccountStatus.BLOCKED.toString()))
                .andReturn();
    }

    @Test
    void testBlockAccountsByProjectId() throws Exception {

        mockMvc.perform(put("/api/v1/account/block")
                        .param("projectId", "3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(11))
                .andExpect(jsonPath("$[0].status").value(AccountStatus.BLOCKED.toString()))
                .andExpect(jsonPath("$[1].id").value(12))
                .andExpect(jsonPath("$[1].status").value(AccountStatus.BLOCKED.toString()))
                .andReturn();
    }

    @Test
    void testUnblockAccountById() throws Exception {
        Long id = 13L;

        mockMvc.perform(put("/api/v1/account/unblock/{id}", id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.projectId").value(4L))
                .andExpect(jsonPath("$.status").value(AccountStatus.ACTIVE.toString()))
                .andExpect(jsonPath("$.number").value("934579038845852"))
                .andReturn();
    }

    @Test
    void testUnblockAccountByNumber() throws Exception {
        String number = "93457903336434";

        mockMvc.perform(put("/api/v1/account/unblock/number/{number}", number))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.projectId").value(4L))
                .andExpect(jsonPath("$.status").value(AccountStatus.ACTIVE.toString()))
                .andExpect(jsonPath("$.number").value(number))
                .andReturn();
    }

    @Test
    void testUnblockAccountsByProjectId() throws Exception {
        Long projectId = 4L;

        mockMvc.perform(put("/api/v1/account/unblock")
                        .param("projectId", projectId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(13))
                .andExpect(jsonPath("$[0].projectId").value(projectId))
                .andExpect(jsonPath("$[0].status").value(AccountStatus.ACTIVE.toString()))
                .andExpect(jsonPath("$[1].id").value(14))
                .andExpect(jsonPath("$[1].projectId").value(projectId))
                .andExpect(jsonPath("$[1].status").value(AccountStatus.ACTIVE.toString()))
                .andReturn();
    }

    @Test
    void testCloseAccountById() throws Exception {
        Long id = 4L;

        mockMvc.perform(put("/api/v1/account/close/{id}", id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.userId").value(2L))
                .andExpect(jsonPath("$.status").value(AccountStatus.CLOSED.toString()))
                .andExpect(jsonPath("$.number").value("328943571239"))
                .andReturn();
    }

    @Test
    void testCloseAccountByNumber() throws Exception {
        String number = "597283728973";

        mockMvc.perform(put("/api/v1/account/close/number/{number}", number))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.status").value(AccountStatus.CLOSED.toString()))
                .andExpect(jsonPath("$.number").value("597283728973"))
                .andReturn();
    }

}
