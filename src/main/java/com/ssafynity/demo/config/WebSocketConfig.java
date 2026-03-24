package com.ssafynity.demo.config;

import com.ssafynity.demo.security.CustomUserDetails;
import com.ssafynity.demo.security.CustomUserDetailsService;
import com.ssafynity.demo.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.*;

/**
 * Spring WebSocket + STOMP 설정
 *
 * 인증 흐름:
 *   STOMP CONNECT 프레임 헤더에 Authorization: Bearer {token} 를 실어서 전송
 *   → ChannelInterceptor가 JWT 검증 → SecurityContext 에 Authentication 주입
 *
 * 메시지 흐름:
 *   Client ──STOMP──▶ /ws (SockJS)
 *     └─▶ /app/chat.message   →  ChatController   → Redis → /topic/chat/{roomId}
 *     └─▶ /app/dm.send        →  DmChatController → Redis → /topic/dm/{roomId}
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /** STOMP CONNECT 시 JWT 검증 */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String bearer = accessor.getFirstNativeHeader("Authorization");
                    if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
                        String token = bearer.substring(7);
                        if (jwtTokenProvider.validateToken(token)) {
                            String username = jwtTokenProvider.getUsernameFromToken(token);
                            CustomUserDetails userDetails =
                                    (CustomUserDetails) userDetailsService.loadUserByUsername(username);
                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails, null, userDetails.getAuthorities());
                            accessor.setUser(auth);
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        }
                    }
                }
                return message;
            }
        });
    }
}
