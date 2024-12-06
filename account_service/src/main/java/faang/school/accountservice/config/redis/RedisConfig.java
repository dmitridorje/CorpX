package faang.school.accountservice.config.redis;

import faang.school.accountservice.listener.AchievementEventListener;
import faang.school.accountservice.listener.BanedUserEventListener;
import faang.school.accountservice.listener.RateDecreaseEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
public class RedisConfig {

    private static final String DEFAULT_LISTENER_METHOD = "onMessage";

    @Value("${redis.channels.achievement}")
    private String achievementEventChannel;

    @Value("${redis.channels.rate-change}")
    private String rateChangeEventChannel;

    @Value("${redis.channels.user-ban}")
    private String bannedUserEventChannel;

    @Value("${redis.channels.rate-decrease}")
    private String rateDecreaseEventChannel;

    public interface MessagePublisher<T> {
        void publish(T redisEvent);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(lettuceConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean(name = "achievementTopic")
    public ChannelTopic achievementChannelTopic() {
        return new ChannelTopic(achievementEventChannel);
    }

    @Bean(name = "rateChangeTopic")
    public ChannelTopic rateChangeChannelTopic() {
        return new ChannelTopic(rateChangeEventChannel);
    }

    @Bean(name = "userBanTopic")
    public ChannelTopic userBanTopic() {
        return new ChannelTopic(bannedUserEventChannel);
    }

    @Bean(name = "rateDecreaseTopic")
    public ChannelTopic rateDecreaseChannelTopic() {
        return new ChannelTopic(rateDecreaseEventChannel);
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(
            LettuceConnectionFactory lettuceConnectionFactory,
            AchievementEventListener achievementEventListener,
            BanedUserEventListener banedUserEventListener,
            RateDecreaseEventListener rateDecreaseEventListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(lettuceConnectionFactory);
        container.addMessageListener(achievementEventListenerAdapter(achievementEventListener), achievementChannelTopic());
        container.addMessageListener(banedUserEventListenerAdapter(banedUserEventListener), userBanTopic());
        container.addMessageListener(rateDecreaseEventListenerAdapter(rateDecreaseEventListener), rateDecreaseChannelTopic());
        return container;
    }

    @Bean
    public MessageListenerAdapter achievementEventListenerAdapter(AchievementEventListener achievementEventListener) {
        return new MessageListenerAdapter(achievementEventListener, DEFAULT_LISTENER_METHOD);
    }

    @Bean
    public MessageListenerAdapter banedUserEventListenerAdapter(BanedUserEventListener banedUserEventListener) {
        return new MessageListenerAdapter(banedUserEventListener, DEFAULT_LISTENER_METHOD);
    }

    @Bean
    public MessageListenerAdapter rateDecreaseEventListenerAdapter(RateDecreaseEventListener rateDecreaseEventListener) {
        return new MessageListenerAdapter(rateDecreaseEventListener, DEFAULT_LISTENER_METHOD);
    }
}
