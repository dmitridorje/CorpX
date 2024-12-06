package faang.school.accountservice.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TariffDto {
    @Null(message = "id must be null, when you create tariff", groups = {Create.class})
    private Long id;

    @NotEmpty(message = "name cannot be empty", groups = {Create.class})
    private String name;

    @NotNull(message = "rate cannot be null")
    @Positive(message = "rate must be positive", groups = {Create.class})
    private BigDecimal rate;

    public interface Create {
    }
}
