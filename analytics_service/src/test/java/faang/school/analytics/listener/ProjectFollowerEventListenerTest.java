package faang.school.analytics.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.analytics.model.entity.AnalyticsEvent;
import faang.school.analytics.model.enums.EventType;
import faang.school.analytics.model.event.ProjectFollowerEvent;
import faang.school.analytics.service.impl.AnalyticsEventServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.Message;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectFollowerEventListenerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AnalyticsEventServiceImpl analyticsEventService;

    @InjectMocks
    private ProjectFollowerEventListener projectFollowerEventListener;

    @Test
    void testOnMessage_Success() throws Exception {
        Long followerId = 1L;
        Long projectId = 2L;
        Long creatorId = 3L;
        LocalDateTime subscriptionDate = LocalDateTime.now();

        ProjectFollowerEvent projectFollowerEvent = new ProjectFollowerEvent(followerId, projectId, creatorId, subscriptionDate);

        String messageBody = "{\"followerId\":" + followerId + ", \"projectId\":" + projectId + ", \"creatorId\":" + creatorId + ", \"subscriptionDate\":\"" + subscriptionDate + "\"}";

        Message message = mock(Message.class);
        when(message.getBody()).thenReturn(messageBody.getBytes());

        when(objectMapper.readValue(any(byte[].class), eq(ProjectFollowerEvent.class))).thenReturn(projectFollowerEvent);

        projectFollowerEventListener.onMessage(message, null);

        ArgumentCaptor<AnalyticsEvent> eventCaptor = ArgumentCaptor.forClass(AnalyticsEvent.class);
        verify(analyticsEventService, times(1)).saveEvent(eventCaptor.capture());

        AnalyticsEvent savedEvent = eventCaptor.getValue();
        assertEquals(creatorId, savedEvent.getReceiverId());
        assertEquals(followerId, savedEvent.getActorId());
        assertEquals(EventType.FOLLOWER, savedEvent.getEventType());
    }

    @Test
    void testOnMessage_throwsException() throws Exception {
        Message message = mock(Message.class);
        when(message.getBody()).thenReturn("invalid".getBytes());

        when(objectMapper.readValue(any(byte[].class), eq(ProjectFollowerEvent.class))).thenThrow(new IOException());

        assertThrows(RuntimeException.class, () -> projectFollowerEventListener.onMessage(message, null));
        verify(objectMapper, times(1)).readValue(message.getBody(), ProjectFollowerEvent.class);
        verifyNoMoreInteractions(analyticsEventService);
    }
}