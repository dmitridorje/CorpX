package faang.school.accountservice.config.ratechange;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "rate-change-rules")
@Data
public class RateChangeRulesConfig {
    private Map<String, RateChangeProperties> events;

    public BigDecimal getTargetRateChange(String title) {
        return events.containsKey(title) ? events.get(title).getTargetRateChange() : BigDecimal.ZERO;
    }

    public String getPartialText(String title) {
        return events.containsKey(title) ? events.get(title).getPartialText() : null;
    }

    @Data
    public static class RateChangeProperties {
        private BigDecimal targetRateChange;
        private String partialText;
    }
}
