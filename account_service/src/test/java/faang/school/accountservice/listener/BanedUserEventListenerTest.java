package faang.school.accountservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.accountservice.config.ratechange.RateChangeRulesConfig;
import faang.school.accountservice.exception.EventProcessingException;
import faang.school.accountservice.model.event.BanedUserEvent;
import faang.school.accountservice.model.event.RateChangeEvent;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BanedUserEventListenerTest {

    @Mock
    private RateAdjustmentService rateAdjustmentService;

    @Mock
    private RateChangeRulesConfig rateChangeRulesConfig;

    @Mock
    private Message message;

    @Mock
    private RateChangeEventPublisher rateChangeEventPublisher;

    @InjectMocks
    private BanedUserEventListener banedUserEventListener;

    private ObjectMapper objectMapper;
    private BanedUserEvent event;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();

        event = new BanedUserEvent();
        event.setUserId(1L);

        banedUserEventListener = new BanedUserEventListener(
                objectMapper,
                rateAdjustmentService,
                rateChangeRulesConfig,
                rateChangeEventPublisher
        );
    }

    @Test
    @DisplayName("Should adjust rate and publish event when rate change is not zero and adjustment is successful")
    public void testOnMessage_Success() throws IOException {
        String eventJson = objectMapper.writeValueAsString(event);

        when(message.getBody()).thenReturn(eventJson.getBytes(StandardCharsets.UTF_8));
        when(rateChangeRulesConfig.getTargetRateChange("ban")).thenReturn(BigDecimal.valueOf(0.1));
        when(rateChangeRulesConfig.getPartialText("ban")).thenReturn("User banned");
        when(rateAdjustmentService.adjustRate(event.getUserId(), BigDecimal.valueOf(0.1))).thenReturn(true);

        ArgumentCaptor<RateChangeEvent> captor = ArgumentCaptor.forClass(RateChangeEvent.class);

        banedUserEventListener.onMessage(message, null);

        verify(rateAdjustmentService).adjustRate(event.getUserId(), BigDecimal.valueOf(0.1));
        verify(rateChangeEventPublisher).publish(captor.capture());

        RateChangeEvent capturedEvent = captor.getValue();
        assertEquals(event.getUserId(), capturedEvent.getUserId());
        assertEquals(BigDecimal.valueOf(0.1), capturedEvent.getRateChangeValue());
        assertEquals("User banned", capturedEvent.getRateChangeReason());
    }

    @Test
    @DisplayName("Should not publish event when rate adjustment fails")
    public void testOnMessage_AdjustmentFails() throws IOException {
        String eventJson = objectMapper.writeValueAsString(event);

        when(message.getBody()).thenReturn(eventJson.getBytes(StandardCharsets.UTF_8));
        when(rateChangeRulesConfig.getTargetRateChange("ban")).thenReturn(BigDecimal.valueOf(0.1));
        when(rateAdjustmentService.adjustRate(event.getUserId(), BigDecimal.valueOf(0.1))).thenReturn(false);

        banedUserEventListener.onMessage(message, null);

        verify(rateAdjustmentService).adjustRate(event.getUserId(), BigDecimal.valueOf(0.1));
        verify(rateChangeEventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should not adjust rate when rate change is zero")
    public void testOnMessage_SkipOperation() throws IOException {
        String eventJson = objectMapper.writeValueAsString(event);

        when(message.getBody()).thenReturn(eventJson.getBytes(StandardCharsets.UTF_8));
        when(rateChangeRulesConfig.getTargetRateChange("ban")).thenReturn(BigDecimal.valueOf(0.0));

        banedUserEventListener.onMessage(message, null);

        verify(rateAdjustmentService, never()).adjustRate(anyLong(), BigDecimal.valueOf(anyDouble()));
        verify(rateChangeEventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should handle IOException gracefully")
    public void testOnMessage_IOException() {
        when(message.getBody()).thenReturn("invalid json".getBytes(StandardCharsets.UTF_8));

        assertThrows(EventProcessingException.class, () -> {
            banedUserEventListener.onMessage(message, null);
        });

        verify(rateAdjustmentService, never()).adjustRate(anyLong(), BigDecimal.valueOf(anyDouble()));
        verify(rateChangeEventPublisher, never()).publish(any());
    }
}
