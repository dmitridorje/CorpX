package faang.school.analytics.config;

import faang.school.analytics.listener.AdBoughtEventListener;
import faang.school.analytics.listener.FundRaisedEventListener;
import faang.school.analytics.listener.LikeEventListener;
import faang.school.analytics.listener.PostViewEventListener;
import faang.school.analytics.listener.PremiumBoughtEventListener;
import faang.school.analytics.listener.ProfileViewEventListener;
import faang.school.analytics.listener.ProjectViewEventListener;
import faang.school.analytics.listener.RecommendationEventListener;
import faang.school.analytics.listener.SearchAppearanceEventListener;
import faang.school.analytics.listener.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.channel.search-appearance}")
    private String searchAppearanceChannel;

    @Value("${spring.data.redis.channel.like}")
    private String likeChannel;

    @Value("${spring.data.redis.channel.recommendation}")
    private String recommendationChannel;

    @Value("${spring.data.redis.channel.ad-bought}")
    private String adBoughtChannel;

    @Value("${spring.data.redis.channel.profile-view}")
    private String profileViewChannel;

    @Value("${spring.data.redis.channel.fund-raised}")
    private String fundRaisedChannel;

    @Value("${spring.data.redis.channel.project-view}")
    private String projectViewChannel;

    @Value("${spring.data.redis.channel.premium-bought}")
    private String premiumBoughtChannel;

    @Value("${spring.data.redis.channel.post_view_channel}")
    private String postViewChannel;


    @Value("${spring.data.redis.channel.user-follower}")
    private String userFollowerChannel;

    @Value("${spring.data.redis.channel.project-follower}")
    private String projectFollowerChannel;

    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(lettuceConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    MessageListenerAdapter likeEvent(LikeEventListener likeEventListener) {
        return new MessageListenerAdapter(likeEventListener);
    }

    @Bean
    MessageListenerAdapter recommendationEvent(RecommendationEventListener recommendationEventListener) {
        return new MessageListenerAdapter(recommendationEventListener);
    }

    @Bean
    MessageListenerAdapter searchAppearanceEvent(SearchAppearanceEventListener searchAppearanceEventListener) {
        return new MessageListenerAdapter(searchAppearanceEventListener);
    }

    @Bean
    MessageListenerAdapter adBoughtEvent(AdBoughtEventListener adBoughtEventListener) {
        return new MessageListenerAdapter(adBoughtEventListener);
    }

    @Bean
    MessageListenerAdapter profileViewEvent(ProfileViewEventListener profileViewEventListener) {
        return new MessageListenerAdapter(profileViewEventListener);
    }

    @Bean
    MessageListenerAdapter fundRaisedEvent(FundRaisedEventListener fundRaisedEventListener) {
        return new MessageListenerAdapter(fundRaisedEventListener);
    }

    @Bean
    MessageListenerAdapter premiumBoughtEvent(PremiumBoughtEventListener premiumBoughtEventListener) {
        return new MessageListenerAdapter(premiumBoughtEventListener);
    }

    @Bean
    MessageListenerAdapter projectViewEvent(ProjectViewEventListener projectViewEventListener) {
        return new MessageListenerAdapter(projectViewEventListener);
    }

    @Bean
    MessageListenerAdapter postViewEvent(PostViewEventListener postViewEventListener) {
        return new MessageListenerAdapter(postViewEventListener);
    }

    @Bean
    public MessageListenerAdapter userFollowerEvent(UserFollowerEventListener userFollowerEventListener) {
        return new MessageListenerAdapter(userFollowerEventListener);
    }

    @Bean
    public MessageListenerAdapter projectFollowerEvent(ProjectFollowerEventListener projectFollowerEventListener) {
        return new MessageListenerAdapter(projectFollowerEventListener);
    }

    @Bean
    ChannelTopic likeTopic() {
        return new ChannelTopic(likeChannel);
    }

    @Bean
    ChannelTopic recommendationTopic() {
        return new ChannelTopic(recommendationChannel);
    }

    @Bean
    ChannelTopic searchAppearanceTopic() {
        return new ChannelTopic(searchAppearanceChannel);
    }

    @Bean
    ChannelTopic adBoughtTopic() {
        return new ChannelTopic(adBoughtChannel);
    }

    @Bean
    ChannelTopic profileViewTopic() {
        return new ChannelTopic(profileViewChannel);
    }

    @Bean
    ChannelTopic postViewTopic() {
        return new ChannelTopic(postViewChannel);
    }

    @Bean
    ChannelTopic fundRaisedTopic() {
        return new ChannelTopic(fundRaisedChannel);
    }

    @Bean
    ChannelTopic premiumBoughtTopic() {
        return new ChannelTopic(premiumBoughtChannel);
    }

    @Bean
    ChannelTopic projectViewTopic() {
        return new ChannelTopic(projectViewChannel);
    }

    @Bean
    public ChannelTopic userFollowerEventTopic() {
        return new ChannelTopic(userFollowerChannel);
    }

    @Bean
    public ChannelTopic projectFollowerEventTopic() {
        return new ChannelTopic(projectFollowerChannel);
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(LettuceConnectionFactory lettuceConnectionFactory,
                                                        MessageListenerAdapter searchAppearanceEvent,
                                                        MessageListenerAdapter likeEvent,
                                                        MessageListenerAdapter recommendationEvent,
                                                        MessageListenerAdapter adBoughtEvent,
                                                        MessageListenerAdapter profileViewEvent,
                                                        MessageListenerAdapter fundRaisedEvent,
                                                        MessageListenerAdapter premiumBoughtEvent,
                                                        MessageListenerAdapter projectViewEvent,
                                                        MessageListenerAdapter postViewEvent,
                                                        MessageListenerAdapter userFollowerEvent,
                                                        MessageListenerAdapter projectFollowerEvent) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(lettuceConnectionFactory);
        container.addMessageListener(likeEvent, likeTopic());
        container.addMessageListener(recommendationEvent, recommendationTopic());
        container.addMessageListener(searchAppearanceEvent, searchAppearanceTopic());
        container.addMessageListener(adBoughtEvent, adBoughtTopic());
        container.addMessageListener(profileViewEvent, profileViewTopic());
        container.addMessageListener(postViewEvent, postViewTopic());
        container.addMessageListener(fundRaisedEvent, fundRaisedTopic());
        container.addMessageListener(premiumBoughtEvent, premiumBoughtTopic());
        container.addMessageListener(projectViewEvent, projectViewTopic());
        container.addMessageListener(userFollowerEvent, userFollowerEventTopic());
        container.addMessageListener(projectFollowerEvent, projectFollowerEventTopic());
        return container;
    }
}