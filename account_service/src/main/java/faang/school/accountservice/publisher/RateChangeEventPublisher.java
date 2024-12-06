package faang.school.accountservice.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.accountservice.model.event.RateChangeEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Component
public class RateChangeEventPublisher extends AbstractEventPublisher<RateChangeEvent>{
    public RateChangeEventPublisher(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper,
                                    @Qualifier("rateChangeTopic") ChannelTopic channelTopic) {
        super(redisTemplate, objectMapper, channelTopic);
    }
}
