package faang.school.analytics.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.analytics.model.dto.GoalCompletedEvent;
import faang.school.analytics.model.entity.AnalyticsEvent;
import faang.school.analytics.model.enums.EventType;
import faang.school.analytics.model.event.ProjectFollowerEvent;
import faang.school.analytics.service.impl.AnalyticsEventServiceImpl;
import org.springframework.data.redis.connection.Message;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ProjectFollowerEventListener extends AbstractRedisListener<ProjectFollowerEvent> {

    public ProjectFollowerEventListener(ObjectMapper objectMapper, AnalyticsEventServiceImpl analyticsEventService) {
        super(objectMapper, analyticsEventService);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        handleEvent(ProjectFollowerEvent.class, message, this::convertToAnalyticsEvent);
    }

    private AnalyticsEvent convertToAnalyticsEvent(ProjectFollowerEvent projectFollowerEvent) {
        return AnalyticsEvent.builder()
                .receiverId(projectFollowerEvent.getCreatorId())
                .actorId(projectFollowerEvent.getFollowerId())
                .eventType(EventType.FOLLOWER)
                .receivedAt(LocalDateTime.now())
                .build();
    }
}