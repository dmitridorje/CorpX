package faang.school.accountservice.controller;

import faang.school.accountservice.model.dto.TariffDto;
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

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class TariffControllerIntegrationTest {

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
    void testGetTariffById() throws Exception {
        Long id = 2L;

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/tariff/{id}", id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("SUBSCRIPTION"))
                .andExpect(jsonPath("$.rate").value("3.4"))
                .andReturn();
    }

    @Test
    void testCreateTariff() throws Exception {
        String name = "NewTariff";
        BigDecimal rate = BigDecimal.valueOf(8.1);
        TariffDto tariffDto = new TariffDto();
        tariffDto.setName(name);
        tariffDto.setRate(rate);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(tariffDto);

        mockMvc.perform(post("/api/v1/tariff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.rate").value(rate))
                .andReturn();
    }

    @Test
    void testUpdateTariff() throws Exception {
        Long id = 3L;
        double rate = 1.1;

        mockMvc.perform(put("/api/v1/tariff/{id}", id)
                        .param("rate", String.valueOf(rate)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("BASIC"))
                .andExpect(jsonPath("$.rate").value(rate))
                .andReturn();
    }
}
