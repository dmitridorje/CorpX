package faang.school.accountservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.accountservice.config.ratechange.RateChangeRulesConfig;
import faang.school.accountservice.model.event.RateChangeEvent;
import faang.school.accountservice.model.event.RateDecreaseEvent;
import faang.school.accountservice.publisher.RateChangeEventPublisher;
import faang.school.accountservice.service.RateAdjustmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class RateDecreaseEventListener extends AbstractEventListener<RateDecreaseEvent> implements MessageListener {

    private final RateAdjustmentService rateAdjustmentService;
    private final RateChangeRulesConfig rateChangeRulesConfig;
    private final RateChangeEventPublisher rateChangeEventPublisher;

    @Autowired
    public RateDecreaseEventListener(ObjectMapper objectMapper,
                                     RateAdjustmentService rateAdjustmentService,
                                     RateChangeRulesConfig rateChangeRulesConfig,
                                     RateChangeEventPublisher rateChangeEventPublisher) {
        super(objectMapper);
        this.rateAdjustmentService = rateAdjustmentService;
        this.rateChangeRulesConfig = rateChangeRulesConfig;
        this.rateChangeEventPublisher = rateChangeEventPublisher;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        handleEvent(message, RateDecreaseEvent.class, event -> {
            BigDecimal rateChange = rateChangeRulesConfig.getTargetRateChange(event.getTitle());
            String partialText = rateChangeRulesConfig.getPartialText(event.getTitle());
            if (rateChange != BigDecimal.ZERO) {
                event.getUserIds().forEach(userId -> {
                    boolean success = rateAdjustmentService.adjustRate(userId, rateChange);
                    if (success) {
                        rateChangeEventPublisher.publish(new RateChangeEvent(userId, rateChange, partialText));
                        log.info("Publishing RateChangeEvent for user ID {} and rate change of {} {}.",
                                userId, rateChange, partialText);
                    }
                });
            }
        });
    }
}
