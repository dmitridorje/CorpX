package faang.school.notificationservice.service.impl;

import faang.school.notificationservice.feign.UserServiceClient;
import faang.school.notificationservice.model.dto.UserDto;
import faang.school.notificationservice.model.event.RateChangeEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateChangeMessageBuilderTest {

    @Mock
    private UserServiceClient userServiceClient;
    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private RateChangeMessageBuilder rateChangeMessageBuilder;

    private RateChangeEvent rateChangeEvent;
    private UserDto receiverDto;

    @BeforeEach
    public void setUp() {
        receiverDto = new UserDto();
        receiverDto.setId(1L);
        receiverDto.setUsername("Receiver");

        rateChangeEvent = new RateChangeEvent();
        rateChangeEvent.setUserId(1L);
        rateChangeEvent.setRateChangeValue(0.2);
        rateChangeEvent.setRateChangeReason("for not being smart enough");
    }

    @Test
    @DisplayName("Should build correct message")
    public void testBuildMessage_Success() {
        when(userServiceClient.getUser(receiverDto.getId())).thenReturn(receiverDto);
        when(messageSource.getMessage(eq("rate.change"), any(Object[].class), eq(Locale.UK)))
                .thenReturn("Receiver's account rate has been changed");

        String message = rateChangeMessageBuilder.buildMessage(rateChangeEvent, Locale.UK);

        verify(userServiceClient).getUser(receiverDto.getId());
        verify(messageSource).getMessage(eq("rate.change"),
                eq(new Object[]{receiverDto.getUsername(), rateChangeEvent.getRateChangeValue(),
                        rateChangeEvent.getRateChangeReason()}) , eq(Locale.UK));

        assertEquals("Receiver's account rate has been changed", message);

        verify(messageSource).getMessage(eq("rate.change"),
                argThat(args -> args[0].equals(receiverDto.getUsername())
                        && args[1].equals(rateChangeEvent.getRateChangeValue())
                        && args[2].equals(rateChangeEvent.getRateChangeReason())),
                eq(Locale.UK));
    }

    @Test
    @DisplayName("Should return correct supported class")
    public void testGetSupportedClass_Success() {
        assertEquals(RateChangeEvent.class, rateChangeMessageBuilder.getSupportedClass());
    }

    @Test
    @DisplayName("Should build message for a different locale")
    public void testBuildMessage_FrenchLocale() {
        when(userServiceClient.getUser(receiverDto.getId())).thenReturn(receiverDto);
        when(messageSource.getMessage(eq("rate.change"), any(Object[].class), eq(Locale.FRANCE)))
                .thenReturn("Message en français");

        String message = rateChangeMessageBuilder.buildMessage(rateChangeEvent, Locale.FRANCE);

        assertEquals("Message en français", message);

        verify(messageSource).getMessage(eq("rate.change"),
                argThat(args -> args[0].equals(receiverDto.getUsername())
                        && args[1].equals(rateChangeEvent.getRateChangeValue())
                        && args[2].equals(rateChangeEvent.getRateChangeReason())),
                eq(Locale.FRANCE ));
    }

    @Test
    @DisplayName("Should handle exception when fetching user data")
    public void testBuildMessage_UserServiceError() {
        when(userServiceClient.getUser(receiverDto.getId()))
                .thenThrow(new RuntimeException("User service error"));

        assertThrows(RuntimeException.class,
                () -> rateChangeMessageBuilder.buildMessage(rateChangeEvent, Locale.UK));
    }
}
