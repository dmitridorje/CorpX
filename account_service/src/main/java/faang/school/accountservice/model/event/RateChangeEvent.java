package faang.school.accountservice.model.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RateChangeEvent {
    long userId;
    BigDecimal rateChangeValue;
    String rateChangeReason;
}
