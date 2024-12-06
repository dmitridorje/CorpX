package faang.school.accountservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.accountservice.config.context.UserContext;
import faang.school.accountservice.model.dto.TariffDto;
import faang.school.accountservice.service.impl.TariffServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TariffController.class)
class TariffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private UserContext userContext;

    @MockBean
    private TariffServiceImpl tariffService;

    private TariffDto tariffDto;
    private Long tariffId;

    @BeforeEach
    public void setUp() {
        tariffDto = new TariffDto();
        tariffDto.setName("tariff1");
        tariffDto.setRate(BigDecimal.valueOf(5.5));
        tariffId = 1L;
    }

    @Test
    public void testCreateTariff() throws Exception {
        String json = objectMapper.writeValueAsString(tariffDto);
        when(tariffService.createTariff(tariffDto)).thenReturn(tariffDto);

        tariffService.createTariff(tariffDto);

        mockMvc.perform(post("/api/v1/tariff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    public void getTariffShouldReturnTariffDto() throws Exception {
        tariffDto.setId(tariffId);
        when(tariffService.getTariff(tariffId)).thenReturn(tariffDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/tariff/{id}", tariffId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(tariffId))
                .andExpect(jsonPath("$.rate").value(tariffDto.getRate()))
                .andExpect(jsonPath("$.name").value(tariffDto.getName()));
    }

    @Test
    public void updateTariffShouldReturnTariffDto() throws Exception {
        double rate = 6.1;
        tariffDto.setId(tariffId);
        when(tariffService.getTariff(tariffId)).thenReturn(tariffDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/tariff/{id}", tariffId)
                        .param("rate", String.valueOf(rate)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(tariffId))
                .andExpect(jsonPath("$.rate").value(tariffDto.getRate()))
                .andExpect(jsonPath("$.name").value(tariffDto.getName()));
    }

}