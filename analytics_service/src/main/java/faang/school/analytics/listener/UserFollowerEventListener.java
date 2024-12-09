package faang.school.analytics.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.analytics.model.entity.AnalyticsEvent;
import faang.school.analytics.model.enums.EventType;
import faang.school.analytics.model.event.ProjectFollowerEvent;
import faang.school.analytics.model.event.UserFollowerEvent;
import faang.school.analytics.service.impl.AnalyticsEventServiceImpl;
import org.springframework.data.redis.connection.Message;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserFollowerEventListener extends AbstractRedisListener<UserFollowerEvent> {

    public UserFollowerEventListener(ObjectMapper objectMapper, AnalyticsEventServiceImpl analyticsEventService) {
        super(objectMapper, analyticsEventService);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        handleEvent(UserFollowerEvent.class, message, this::convertToAnalyticsEvent);
    }

    private AnalyticsEvent convertToAnalyticsEvent(UserFollowerEvent userFollowerEvent) {
        return AnalyticsEvent.builder()
                .receiverId(userFollowerEvent.getFollowedUserId())
                .actorId(userFollowerEvent.getFollowerId())
                .eventType(EventType.FOLLOWER)
                .receivedAt(LocalDateTime.now())
                .build();
    }
}