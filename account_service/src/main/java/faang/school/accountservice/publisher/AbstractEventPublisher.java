package faang.school.accountservice.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import faang.school.accountservice.config.redis.RedisConfig;
import faang.school.accountservice.exception.EventPublishingException;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractEventPublisher<T> implements RedisConfig.MessagePublisher<T> {

    private final RedisTemplate<String, Object> redisTemplate;
    protected final ObjectMapper objectMapper;
    private final ChannelTopic channelTopic;

    public void publish(T redisEvent) {
        try {
            String message = objectMapper.writeValueAsString(redisEvent);
            redisTemplate.convertAndSend(channelTopic.getTopic(), message);
        } catch (JsonProcessingException exception) {
            String errorMessage = "Failed to serialize event: " + redisEvent;
            log.error(errorMessage, exception);
            throw new EventPublishingException(errorMessage, exception);
        } catch (Exception exception) {
            String errorMessage = "An unexpected error occurred while publishing event: " + redisEvent;
            log.error(errorMessage, exception);
            throw new EventPublishingException(errorMessage, exception);
        }
    }
}
