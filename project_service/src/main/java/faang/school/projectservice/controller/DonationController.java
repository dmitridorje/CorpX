package faang.school.projectservice.controller;

import faang.school.projectservice.model.dto.DonationDto;
import faang.school.projectservice.model.dto.DonationFilterDto;
import faang.school.projectservice.service.DonationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Tag(name = "Donation", description = "API for donations")
@RequestMapping("/donation")
public class DonationController {
    private final DonationService donationService;

    @PostMapping
    @Operation(summary = "Create a donation", description = "Create a new donation")
    public DonationDto create(
            @Parameter(description = "ID of the user", required = true)
            @RequestHeader("x-user-id") String userId,
            @Valid @RequestBody DonationDto donationDto) {
        return donationService.create(donationDto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get donation", description = "Get donation by id")
    public DonationDto getById(
            @Parameter(description = "ID of the user", required = true)
            @RequestHeader("x-user-id") String userId,
            @Parameter(description = "ID of the donation", required = true)
            @PathVariable Long id) {
        return donationService.getById(id);
    }

    @PostMapping("/filter")
    @Operation(summary = "Get donations by filter", description = "Retrieve donations by filters: donation date, " +
            "currency, min/max amount. Donations ordered by creation date from new to old")
    public List<DonationDto> getDonationsByFilter(
            @Parameter(description = "ID of the user", required = true)
            @RequestHeader("x-user-id") String userId,
            @Parameter(description = "Filter for donations", required = true)
            @RequestBody DonationFilterDto filterDto) {
        return donationService.getByFilter(filterDto);
    }
}
