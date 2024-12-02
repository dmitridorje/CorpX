package faang.school.projectservice.controller;

import faang.school.projectservice.model.dto.DonationDto;
import faang.school.projectservice.model.dto.DonationFilterDto;
import faang.school.projectservice.model.enums.Currency;
import faang.school.projectservice.service.DonationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DonationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DonationService donationService;

    @InjectMocks
    private DonationController donationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(donationController).build();
    }

    @Test
    void createDonationTest() throws Exception {
        DonationDto donationDto = new DonationDto();
        donationDto.setAmount(new BigDecimal("50.00"));
        donationDto.setCurrency(Currency.USD);
        donationDto.setUserId(1L);
        donationDto.setCampaignId(2L);
        donationDto.setDonationTime(LocalDateTime.now());

        given(donationService.create(any(DonationDto.class))).willReturn(donationDto);

        mockMvc.perform(post("/donation")
                .header("x-user-id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":50.00,\"currency\":\"USD\",\"userId\":1,\"campaignId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.currency").value("USD"));

        verify(donationService, times(1)).create(any(DonationDto.class));
    }

    @Test
    void getDonationByIdTest() throws Exception {
        DonationDto donationDto = new DonationDto();
        donationDto.setId(1L);
        donationDto.setAmount(new BigDecimal("100.00"));
        donationDto.setCurrency(Currency.EUR);

        given(donationService.getById(1L)).willReturn(donationDto);

        mockMvc.perform(get("/donation/{id}", 1)
                .header("x-user-id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.currency").value("EUR"));

        verify(donationService, times(1)).getById(1L);
    }

    @Test
    void getDonationsByFilterTest() throws Exception {
        DonationFilterDto filterDto = new DonationFilterDto();
        List<DonationDto> donations = List.of(new DonationDto(), new DonationDto());

        given(donationService.getByFilter(any(DonationFilterDto.class))).willReturn(donations);

        mockMvc.perform(post("/donation/filter")
                .header("x-user-id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(donationService, times(1)).getByFilter(any(DonationFilterDto.class));
    }
}
