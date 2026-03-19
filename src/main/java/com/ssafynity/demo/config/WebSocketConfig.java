package com.ssafynity.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/**
 * Spring WebSocket + STOMP 설정
 *
 * 메시지 흐름:
 *   Client ──STOMP──▶ /ws (SockJS)
 *     └─▶ /app/chat.message   →  ChatController.sendMessage()
 *                                 └─▶ RedisPublisher.publish()
 *                                       └─▶ Redis topic "chat"
 *                                             └─▶ RedisSubscriber.onMessage()
 *                                                   └─▶ /topic/chat/{roomId}
 *     └─▶ /app/chat.join      →  ChatController.joinRoom()
 *     └─▶ /app/chat.leave     →  ChatController.leaveRoom()
 *
 * 구독:
 *   Client subscribes to /topic/chat/{roomId}
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트에게 메시지 브로드캐스트할 경로 prefix
        // /topic  → 1:N 브로드캐스트 (채팅방)
        // /queue  → 1:1 개인 메시지 (향후 DM 기능 확장용)
        config.enableSimpleBroker("/topic", "/queue");

        // @MessageMapping 메서드를 라우팅할 prefix
        config.setApplicationDestinationPrefixes("/app");

        // @SendToUser 등 1:1 메시지 prefix
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                // HTTP 세션의 loginMember 속성을 WebSocket 세션으로 복사
                // → ChatController에서 @MessageMapping 내부에서 세션 정보 접근 가능
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOriginPatterns("*")
                .withSockJS();   // SockJS 폴백 (IE, 방화벽 환경 대응)
    }
}
