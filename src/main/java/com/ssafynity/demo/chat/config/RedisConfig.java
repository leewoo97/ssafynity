package com.ssafynity.demo.chat.config;

import com.ssafynity.demo.chat.RedisSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 설정
 *
 * 역할별 사용처:
 *   ① Pub/Sub  – RedisPublisher → Redis topic → RedisSubscriber → STOMP broker
 *   ② Cache    – chat:history:{roomId}  List<String(JSON)> 최근 100개 메시지
 *
 * 시작 방법 (로컬):
 *   docker run -d -p 6379:6379 redis:7-alpine
 */
@Configuration
public class RedisConfig {

    /**
     * 범용 Redis 템플릿 (key/value 모두 String = JSON 직접 처리).
     * Pub/Sub convertAndSend 와 List(캐시) 모두 이 템플릿 사용.
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        StringRedisSerializer str = new StringRedisSerializer();
        template.setKeySerializer(str);
        template.setValueSerializer(str);
        template.setHashKeySerializer(str);
        template.setHashValueSerializer(str);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Pub/Sub 채널 토픽.
     * 모든 채팅 메시지는 단일 topic "chat" 을 통해 릴레이하고
     * roomId 필드로 구분한다.
     */
    @Bean
    public ChannelTopic chatTopic() {
        return new ChannelTopic("chat");
    }

    /**
     * Redis 메시지 리스너 컨테이너.
     * RedisSubscriber(MessageListener) 를 "chat" topic 에 등록.
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory factory,
            RedisSubscriber redisSubscriber,
            ChannelTopic chatTopic) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(redisSubscriber, chatTopic);
        return container;
    }
}
