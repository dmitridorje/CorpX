package faang.school.accountservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.accountservice.config.context.UserContext;
import faang.school.accountservice.model.dto.AccountDto;
import faang.school.accountservice.model.enums.AccountStatus;
import faang.school.accountservice.model.enums.AccountType;
import faang.school.accountservice.model.enums.Currency;
import faang.school.accountservice.service.impl.AccountServiceImpl;
import faang.school.accountservice.validator.AccountControllerValidator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private UserContext userContext;

    @MockBean
    private AccountServiceImpl accountServiceImpl;

    @MockBean
    private AccountControllerValidator validator;

    @Autowired
    private AccountControllerValidator accountControllerValidator;

    private Long id;
    private AccountDto accountDto;
    private AccountDto accountDto2;

    private String number;

    @BeforeEach
    public void setup() {
        id = 1L;
        Long projectId = 2L;
        number = "123456789012345";
        accountDto = new AccountDto();
        accountDto.setId(id);
        accountDto.setProjectId(projectId);
        accountDto.setNumber(number);
        accountDto.setCreatedAt(LocalDateTime.now());

        accountDto2 = new AccountDto();
        accountDto2.setId(2L);
        accountDto2.setUserId(id);
    }

    @Test
    void shouldOpenAccount() throws Exception {

        AccountDto validAccountDto = new AccountDto();
        validAccountDto.setProjectId(1L);
        validAccountDto.setStatus(AccountStatus.ACTIVE);
        validAccountDto.setType(AccountType.SAVINGS);
        validAccountDto.setCurrency(Currency.USD);

        String json = objectMapper.writeValueAsString(validAccountDto);
        when(accountServiceImpl.openAccount(validAccountDto)).thenReturn(validAccountDto);

        accountServiceImpl.openAccount(validAccountDto);

        mockMvc.perform(post("/api/v1/account", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"));
    }


    @Test
    void shouldReturnAccountDto() throws Exception {
        when(accountServiceImpl.getAccount(id)).thenReturn(accountDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/account/{id}", id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.projectId").value(accountDto.getProjectId()))
                .andExpect(jsonPath("$.number").value(accountDto.getNumber()));
    }

    @Test
    public void shouldReturnNotFoundWhenAccountDoesNotExist() throws Exception {
        when(accountServiceImpl.getAccount(1L)).thenThrow(new EntityNotFoundException("Account not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/accounts/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnAccountDtoFindByNumber() throws Exception {
        when(accountServiceImpl.getAccountNumber(number)).thenReturn(accountDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/account/number/{number}", number))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.projectId").value(accountDto.getProjectId()))
                .andExpect(jsonPath("$.number").value(accountDto.getNumber()));
    }

    @Test
    void shouldReturnAccountDtoBlockById() throws Exception {
        accountDto.setStatus(AccountStatus.BLOCKED);
        String json = objectMapper.writeValueAsString(accountDto);

        when(accountServiceImpl.blockAccount(id)).thenReturn(accountDto);

        mockMvc.perform(put("/api/v1/account/block/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.projectId").value(accountDto.getProjectId()))
                .andExpect(jsonPath("$.status").value(accountDto.getStatus().toString()))
                .andExpect(jsonPath("$.number").value(accountDto.getNumber()));
    }

    @Test
    void shouldReturnAccountDtoBlockByNumber() throws Exception {
        accountDto.setStatus(AccountStatus.BLOCKED);
        String json = objectMapper.writeValueAsString(accountDto);

        when(accountServiceImpl.blockAccountNumber(number)).thenReturn(accountDto);

        mockMvc.perform(put("/api/v1/account/block/number/{number}", number)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.projectId").value(accountDto.getProjectId()))
                .andExpect(jsonPath("$.status").value(accountDto.getStatus().toString()))
                .andExpect(jsonPath("$.number").value(accountDto.getNumber()));
    }

    @Test
    public void blockAccountsByUserOrProject_UserIdProvided_ShouldReturnOkAndBlockedAccounts() throws Exception {
        List<AccountDto> blockedAccounts = Arrays.asList(accountDto, accountDto2);
        when(accountServiceImpl.blockAllAccountsByUserOrProject(1L, null)).thenReturn(blockedAccounts);

        mockMvc.perform(put("/api/v1/account/block")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(accountDto.getId()))
                .andExpect(jsonPath("$[0].userId").value(accountDto.getUserId()))
                .andExpect(jsonPath("$[1].id").value(accountDto2.getId()))
                .andExpect(jsonPath("$[1].userId").value(accountDto2.getUserId()));
    }

    @Test
    public void blockAccountsByUserOrProject_ProjectIdProvided_ShouldReturnOkAndBlockedAccounts() throws Exception {
        List<AccountDto> blockedAccounts = Arrays.asList(accountDto, accountDto2);
        when(accountServiceImpl.blockAllAccountsByUserOrProject(null, 1L)).thenReturn(blockedAccounts);

        mockMvc.perform(put("/api/v1/account/block")
                        .param("projectId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(accountDto.getId()))
                .andExpect(jsonPath("$[0].projectId").value(accountDto.getProjectId()))
                .andExpect(jsonPath("$[1].id").value(accountDto2.getId()))
                .andExpect(jsonPath("$[1].projectId").value(accountDto2.getProjectId()));
    }

    @Test
    void shouldReturnAccountDtoUnblockById() throws Exception {
        accountDto.setStatus(AccountStatus.ACTIVE);
        String json = objectMapper.writeValueAsString(accountDto);

        when(accountServiceImpl.unblockAccount(id)).thenReturn(accountDto);

        mockMvc.perform(put("/api/v1/account/unblock/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.projectId").value(accountDto.getProjectId()))
                .andExpect(jsonPath("$.status").value(accountDto.getStatus().toString()))
                .andExpect(jsonPath("$.number").value(accountDto.getNumber()));
    }

    @Test
    void shouldReturnAccountDtoUnBlockByNumber() throws Exception {
        accountDto.setStatus(AccountStatus.ACTIVE);
        String json = objectMapper.writeValueAsString(accountDto);

        when(accountServiceImpl.unblockAccountNumber(number)).thenReturn(accountDto);

        mockMvc.perform(put("/api/v1/account/unblock/number/{number}", number)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.projectId").value(accountDto.getProjectId()))
                .andExpect(jsonPath("$.status").value(accountDto.getStatus().toString()))
                .andExpect(jsonPath("$.number").value(accountDto.getNumber()));
    }

    @Test
    public void unblockAccountsByUserOrProject_UserIdProvided_ShouldReturnOkAndBlockedAccounts() throws Exception {
        List<AccountDto> blockedAccounts = Arrays.asList(accountDto, accountDto2);
        when(accountServiceImpl.unblockAllAccountsByUserOrProject(1L, null)).thenReturn(blockedAccounts);

        mockMvc.perform(put("/api/v1/account/unblock")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(accountDto.getId()))
                .andExpect(jsonPath("$[0].userId").value(accountDto.getUserId()))
                .andExpect(jsonPath("$[1].id").value(accountDto2.getId()))
                .andExpect(jsonPath("$[1].userId").value(accountDto2.getUserId()));
    }

    @Test
    public void unblockAccountsByUserOrProject_ProjectIdProvided_ShouldReturnOkAndBlockedAccounts() throws Exception {
        List<AccountDto> blockedAccounts = Arrays.asList(accountDto, accountDto2);
        when(accountServiceImpl.unblockAllAccountsByUserOrProject(null, 1L)).thenReturn(blockedAccounts);

        mockMvc.perform(put("/api/v1/account/unblock")
                        .param("projectId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(accountDto.getId()))
                .andExpect(jsonPath("$[0].projectId").value(accountDto.getProjectId()))
                .andExpect(jsonPath("$[1].id").value(accountDto2.getId()))
                .andExpect(jsonPath("$[1].projectId").value(accountDto2.getProjectId()));
    }

    @Test
    void shouldReturnAccountDtoClosedById() throws Exception {
        accountDto.setStatus(AccountStatus.CLOSED);
        String json = objectMapper.writeValueAsString(accountDto);

        when(accountServiceImpl.closeAccount(id)).thenReturn(accountDto);

        mockMvc.perform(put("/api/v1/account/close/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.projectId").value(accountDto.getProjectId()))
                .andExpect(jsonPath("$.status").value(accountDto.getStatus().toString()))
                .andExpect(jsonPath("$.number").value(accountDto.getNumber()));
    }

    @Test
    void shouldReturnAccountDtoCloseByNumber() throws Exception {
        accountDto.setStatus(AccountStatus.CLOSED);
        String json = objectMapper.writeValueAsString(accountDto);

        when(accountServiceImpl.closeAccountNumber(number)).thenReturn(accountDto);

        mockMvc.perform(put("/api/v1/account/close/number/{number}", number)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.projectId").value(accountDto.getProjectId()))
                .andExpect(jsonPath("$.status").value(accountDto.getStatus().toString()))
                .andExpect(jsonPath("$.number").value(accountDto.getNumber()));
    }
}