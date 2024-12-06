package faang.school.accountservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.accountservice.config.context.UserContext;
import faang.school.accountservice.model.dto.BalanceDto;
import faang.school.accountservice.service.impl.BalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BalanceController.class)
class BalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BalanceService balanceService;

    @MockBean
    private UserContext userContext;

    private Long accountId;
    private BalanceDto balanceDto;

    @BeforeEach
    public void setup() {
        accountId = 1L;
        balanceDto = new BalanceDto();
        balanceDto.setId(accountId);
        balanceDto.setAuthorizedBalance(BigDecimal.valueOf(100.00));
        balanceDto.setActualBalance(BigDecimal.valueOf(200.00));
    }

    @Test
    void shouldReturnBalanceDtoWhenAccountIdExists() throws Exception {
        when(balanceService.getBalanceByAccountId(accountId)).thenReturn(balanceDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/balance/{accountId}", accountId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(accountId))
                .andExpect(jsonPath("$.authorizedBalance").value(balanceDto.getAuthorizedBalance().doubleValue()))
                .andExpect(jsonPath("$.actualBalance").value(balanceDto.getActualBalance().doubleValue()));
    }

    @Test
    void shouldReturnNotFoundWhenBalanceDoesNotExist() throws Exception {
        when(balanceService.getBalanceByAccountId(accountId)).thenThrow(new RuntimeException("Balance not found for " +
                "account id: " + accountId));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/balance/{accountId}", accountId))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldUpdateBalanceWhenValidRequestProvided() throws Exception {
        String json = objectMapper.writeValueAsString(balanceDto);
        when(balanceService.updateBalance(any(BalanceDto.class))).thenReturn(balanceDto);

        mockMvc.perform(post("/api/v1/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(balanceDto.getId()))
                .andExpect(jsonPath("$.authorizedBalance").value(balanceDto.getAuthorizedBalance().doubleValue()))
                .andExpect(jsonPath("$.actualBalance").value(balanceDto.getActualBalance().doubleValue()));
    }
}