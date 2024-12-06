package faang.school.accountservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.accountservice.exception.EventProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;

import java.io.IOException;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractEventListener<T> {

    private final ObjectMapper objectMapper;

    protected void handleEvent(Message message, Class<T> eventClass, Consumer<T> consumer) {
        try {
            T event = objectMapper.readValue(message.getBody(), eventClass);
            log.info("Event processed successfully: eventType={}", eventClass.getSimpleName());
            consumer.accept(event);
        } catch (IOException ex) {
            log.error("Failed to process event: eventType={}, messageBody={}",
                    eventClass.getSimpleName(), new String(message.getBody()), ex);
            throw new EventProcessingException("Failed to process event of type " + eventClass.getSimpleName());
        }
    }
}
