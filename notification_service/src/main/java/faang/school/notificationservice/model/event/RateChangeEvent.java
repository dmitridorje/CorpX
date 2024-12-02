package faang.school.notificationservice.model.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RateChangeEvent {
    long userId;
    double rateChangeValue;
    String rateChangeReason;
}
