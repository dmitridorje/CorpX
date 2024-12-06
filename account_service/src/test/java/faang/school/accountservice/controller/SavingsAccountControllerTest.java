package faang.school.accountservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.accountservice.config.context.UserContext;
import faang.school.accountservice.model.dto.SavingsAccountDto;
import faang.school.accountservice.service.impl.SavingsAccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SavingsAccountController.class)
class SavingsAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private UserContext userContext;

    @MockBean
    private SavingsAccountServiceImpl savingsAccountService;

    private SavingsAccountDto savingsAccountDto;
    private Long savingsAccountId;

    @BeforeEach
    public void setUp() {
        savingsAccountId = 1L;
        savingsAccountDto = new SavingsAccountDto();
        savingsAccountDto.setId(savingsAccountId);
        savingsAccountDto.setRate(BigDecimal.valueOf(1.8));
        savingsAccountDto.setTariffId(3L);
    }

    @Test
    public void testOpenSavingsAccount() throws Exception {
        savingsAccountDto.setId(null);
        String json = objectMapper.writeValueAsString(savingsAccountDto);
        when(savingsAccountService.openSavingsAccount(savingsAccountDto)).thenReturn(savingsAccountDto);

        savingsAccountService.openSavingsAccount(savingsAccountDto);

        mockMvc.perform(post("/api/v1/savings-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    public void testGetSavingsAccountShouldReturnSavingsAccountDto() throws Exception {
        when(savingsAccountService.getSavingsAccount(savingsAccountId)).thenReturn(savingsAccountDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/savings-account/{id}", savingsAccountDto.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(savingsAccountDto.getId()))
                .andExpect(jsonPath("$.rate").value(savingsAccountDto.getRate()))
                .andExpect(jsonPath("$.tariffId").value(savingsAccountDto.getTariffId()));
    }

    @Test
    public void testGetSavingsAccountByUserIdShouldReturnListSavingsDtos() throws Exception {
        List<SavingsAccountDto> resultDtos = List.of(new SavingsAccountDto(), new SavingsAccountDto());
        when(savingsAccountService.getSavingsAccountByUserId(savingsAccountId)).thenReturn(resultDtos);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/savings-account")
                        .param("userId", String.valueOf(1L)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)));
    }
}