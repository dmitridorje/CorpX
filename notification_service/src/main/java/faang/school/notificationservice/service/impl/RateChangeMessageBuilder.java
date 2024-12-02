package faang.school.notificationservice.service.impl;

import faang.school.notificationservice.feign.UserServiceClient;
import faang.school.notificationservice.model.dto.UserDto;
import faang.school.notificationservice.model.event.RateChangeEvent;
import faang.school.notificationservice.service.MessageBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class RateChangeMessageBuilder implements MessageBuilder<RateChangeEvent> {
    private final UserServiceClient userServiceClient;
    private final MessageSource messageSource;

    @Override
    public Class<RateChangeEvent> getSupportedClass() {
        return RateChangeEvent.class;
    }

    @Override
    public String buildMessage(RateChangeEvent event, Locale locale) {
        UserDto userDto = userServiceClient.getUser(event.getUserId());
        return messageSource.getMessage("rate.change",
                new Object[]{userDto.getUsername(), event.getRateChangeValue(), event.getRateChangeReason()}, locale);
    }
}

