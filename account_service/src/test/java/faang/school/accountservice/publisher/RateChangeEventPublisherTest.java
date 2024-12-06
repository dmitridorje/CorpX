package faang.school.accountservice.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.accountservice.exception.EventPublishingException;
import faang.school.accountservice.model.event.RateChangeEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateChangeEventPublisherTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ChannelTopic channelTopic;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RateChangeEventPublisher rateChangeEventPublisher;

    private RateChangeEvent event;

    @BeforeEach
    public void setUp() {
        event = new RateChangeEvent(1L, new BigDecimal(0.5), "Promotion Applied");
    }

    @Test
    @DisplayName("Should publish event successfully")
    public void testPublish_Success() throws JsonProcessingException {
        String expectedMessage = "{\"userId\":1,\"rateChangeValue\":0.5,\"rateChangeReason\":\"Promotion Applied\"}";
        when(objectMapper.writeValueAsString(event)).thenReturn(expectedMessage);
        when(channelTopic.getTopic()).thenReturn("rateChangeTopic");

        rateChangeEventPublisher.publish(event);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisTemplate).convertAndSend(eq("rateChangeTopic"), messageCaptor.capture());
        assertEquals(expectedMessage, messageCaptor.getValue());
    }

    @Test
    @DisplayName("Should throw EventPublishingException when JSON processing fails")
    public void testPublish_JsonProcessingException() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(event)).thenThrow(new JsonProcessingException("JSON error") {});

        EventPublishingException exception = assertThrows(EventPublishingException.class, () -> rateChangeEventPublisher.publish(event));
        assertEquals("Failed to serialize event: " + event, exception.getMessage());

        verify(redisTemplate, never()).convertAndSend(anyString(), any());
    }

    @Test
    @DisplayName("Should throw EventPublishingException on unexpected exception")
    public void testPublish_UnexpectedException() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(event))
                .thenReturn("{\"userId\":1,\"rateChangeValue\":0.5,\"rateChangeReason\":\"Promotion Applied\"}");
        doThrow(new RuntimeException("Unexpected error")).when(redisTemplate).convertAndSend(anyString(), any());

        EventPublishingException exception = assertThrows(EventPublishingException.class, () -> rateChangeEventPublisher.publish(event));
        assertEquals("An unexpected error occurred while publishing event: " + event, exception.getMessage());
    }
}
