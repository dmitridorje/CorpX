package faang.school.postservice.config.redis;

import faang.school.postservice.listener.HashtagListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisProperties redisProperties;

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties.getPort());
        return new LettuceConnectionFactory(configuration);
    }

    public interface MessagePublisher<T> {
        void publish(T redisEvent);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public MessageListenerAdapter hashtagListenerAdapter(HashtagListener hashtagListener) {
        return new MessageListenerAdapter(hashtagListener);
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(
            LettuceConnectionFactory lettuceConnectionFactory,
            HashtagListener hashtagListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(lettuceConnectionFactory);
        container.addMessageListener(hashtagListenerAdapter(hashtagListener), hashtagTopic());
        return container;
    }

    @Bean
    public ChannelTopic hashtagTopic() {
        return new ChannelTopic(redisProperties.getChannels().get("hashtag"));
    }

    @Bean
    public ChannelTopic likeEventTopic() {
        return new ChannelTopic(redisProperties.getChannels().get("like_post"));
    }

    @Bean
    public ChannelTopic viewProfileTopic() {
        return new ChannelTopic(redisProperties.getChannels().get("post_view"));
    }

    @Bean
    public ChannelTopic adBoughtTopic() {
        return new ChannelTopic(redisProperties.getChannels().get("ad_bought"));
    }

    @Bean
    public ChannelTopic postViewTopic() {
        return new ChannelTopic(redisProperties.getChannels().get("post_view"));
    }

    @Bean
    public ChannelTopic bannedUserTopic() {
        return new ChannelTopic(redisProperties.getChannels().get("user_ban"));
    }

    @Bean
    public ChannelTopic commentTopic() {
        return new ChannelTopic(redisProperties.getChannels().get("comment_channel"));
    }

    @Bean
    public ChannelTopic postCommentChannelTopic() {
        return new ChannelTopic(redisProperties.getChannels().get("post-comment"));
    }
}

