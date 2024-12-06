package faang.school.postservice.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.postservice.model.event.CommentPostEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Component
public class PostCommentEventPublisher extends AbstractEventPublisher<CommentPostEvent> {

    public PostCommentEventPublisher(RedisTemplate<String, Object> redisTemplate,
                                     ObjectMapper objectMapper,
                                     ChannelTopic commentTopic) {
        super(redisTemplate, objectMapper, commentTopic);
    }

    public void publishCommentEvent(CommentPostEvent event) {
        publish(event);
    }
}
