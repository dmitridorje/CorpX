package faang.school.accountservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.accountservice.config.ratechange.RateChangeRulesConfig;
import faang.school.accountservice.exception.EventProcessingException;
import faang.school.accountservice.feign.AchievementServiceClient;
import faang.school.accountservice.model.event.RateChangeEvent;
import faang.school.accountservice.model.event.RateDecreaseEvent;
import faang.school.accountservice.publisher.RateChangeEventPublisher;
import faang.school.accountservice.service.RateAdjustmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.Message;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateDecreaseEventListenerTest {

    @Mock
    private RateAdjustmentService rateAdjustmentService;

    @Mock
    private RateChangeRulesConfig rateChangeRulesConfig;

    @Mock
    private Message message;

    @Mock
    RateChangeEventPublisher rateChangeEventPublisher;

    @InjectMocks
    private RateDecreaseEventListener rateDecreaseEventListener;

    private ObjectMapper objectMapper;
    private RateDecreaseEvent event;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();

        event = new RateDecreaseEvent();
        event.setUserIds(Arrays.asList(1L, 2L));
        event.setTitle("ban");

        rateDecreaseEventListener = new RateDecreaseEventListener(
                objectMapper,
                rateAdjustmentService,
                rateChangeRulesConfig,
                rateChangeEventPublisher
        );
    }

    @Test
    @DisplayName("Should adjust rate and publish event for each successful user adjustment")
    public void testOnMessage_Success() throws IOException {
        String eventJson = objectMapper.writeValueAsString(event);

        when(message.getBody()).thenReturn(eventJson.getBytes(StandardCharsets.UTF_8));
        when(rateChangeRulesConfig.getTargetRateChange(event.getTitle())).thenReturn(BigDecimal.valueOf(0.2));
        when(rateChangeRulesConfig.getPartialText(event.getTitle())).thenReturn("Sample text");

        when(rateAdjustmentService.adjustRate(1L, BigDecimal.valueOf(0.2))).thenReturn(true);
        when(rateAdjustmentService.adjustRate(2L, BigDecimal.valueOf(0.2))).thenReturn(true);

        rateDecreaseEventListener.onMessage(message, null);

        verify(rateAdjustmentService).adjustRate(1L, BigDecimal.valueOf(0.2));
        verify(rateAdjustmentService).adjustRate(2L, BigDecimal.valueOf(0.2));

        ArgumentCaptor<RateChangeEvent> captor = ArgumentCaptor.forClass(RateChangeEvent.class);
        verify(rateChangeEventPublisher, times(2)).publish(captor.capture());

        List<RateChangeEvent> publishedEvents = captor.getAllValues();

        assertEquals(1L, publishedEvents.get(0).getUserId());
        assertEquals(BigDecimal.valueOf(0.2), publishedEvents.get(0).getRateChangeValue());
        assertEquals("Sample text", publishedEvents.get(0).getRateChangeReason());

        assertEquals(2L, publishedEvents.get(1).getUserId());
        assertEquals(BigDecimal.valueOf(0.2), publishedEvents.get(1).getRateChangeValue());
        assertEquals("Sample text", publishedEvents.get(1).getRateChangeReason());
    }

    @Test
    @DisplayName("Should skip rate adjustment when rate change is zero")
    public void testOnMessage_SkipOperation() throws IOException {
        String eventJson = objectMapper.writeValueAsString(event);

        when(message.getBody()).thenReturn(eventJson.getBytes(StandardCharsets.UTF_8));
        when(rateChangeRulesConfig.getTargetRateChange(event.getTitle())).thenReturn(BigDecimal.valueOf(0.0));

        rateDecreaseEventListener.onMessage(message, null);

        verify(rateAdjustmentService, never()).adjustRate(anyLong(), BigDecimal.valueOf(anyDouble()));
        verify(rateChangeEventPublisher, never()).publish(any(RateChangeEvent.class));
    }

    @Test
    @DisplayName("Should handle IOException gracefully")
    public void testOnMessage_IOException() {
        when(message.getBody()).thenReturn("invalid json".getBytes(StandardCharsets.UTF_8));

        try {
            rateDecreaseEventListener.onMessage(message, null);
        } catch (EventProcessingException e) {
            verify(rateAdjustmentService, never()).adjustRate(anyLong(), BigDecimal.valueOf(anyDouble()));
            verify(rateChangeEventPublisher, never()).publish(any(RateChangeEvent.class));
        }
    }
}
