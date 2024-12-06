package faang.school.accountservice.config.scheduler;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "free-account-numbers")
@Data
public class ScheduledAccountConfig {

    private Map<String, AccountConfig> accounts;

    @Data
    public static class AccountConfig {
        private int targetAmount;
    }
}
