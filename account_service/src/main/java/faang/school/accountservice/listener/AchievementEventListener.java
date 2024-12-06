package faang.school.accountservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.accountservice.config.ratechange.RateChangeRulesConfig;
import faang.school.accountservice.feign.AchievementServiceClient;
import faang.school.accountservice.model.dto.AchievementDto;
import faang.school.accountservice.model.event.AchievementEvent;
import faang.school.accountservice.model.event.RateChangeEvent;
import faang.school.accountservice.publisher.RateChangeEventPublisher;
import faang.school.accountservice.service.RateAdjustmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class AchievementEventListener extends AbstractEventListener<AchievementEvent> implements MessageListener {

    private final RateAdjustmentService rateAdjustmentService;
    private final RateChangeRulesConfig rateChangeRulesConfig;
    private final AchievementServiceClient achievementServiceClient;
    private final RateChangeEventPublisher rateChangeEventPublisher;

    public AchievementEventListener(ObjectMapper objectMapper,
                                    RateAdjustmentService rateAdjustmentService,
                                    RateChangeRulesConfig rateChangeRulesConfig,
                                    AchievementServiceClient achievementServiceClient,
                                    RateChangeEventPublisher rateChangeEventPublisher) {
        super(objectMapper);
        this.rateAdjustmentService = rateAdjustmentService;
        this.rateChangeRulesConfig = rateChangeRulesConfig;
        this.achievementServiceClient = achievementServiceClient;
        this.rateChangeEventPublisher = rateChangeEventPublisher;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        handleEvent(message, AchievementEvent.class, event -> {
            AchievementDto achievementDto = achievementServiceClient.getAchievement(event.getAchievementId());
            BigDecimal rateChange = rateChangeRulesConfig.getTargetRateChange(achievementDto.getTitle());
            String partialText = rateChangeRulesConfig.getPartialText(achievementDto.getTitle());
            if (rateChange != BigDecimal.ZERO) {
                boolean success = rateAdjustmentService.adjustRate(event.getUserId(), rateChange);
                if (success) {
                   rateChangeEventPublisher.publish(new RateChangeEvent(event.getUserId(), rateChange, partialText));
                    log.info("Publishing RateChangeEvent for user ID {} and rate change of {} {}.",
                            event.getUserId(), rateChange, partialText);
                }
            }
        });
    }
}
