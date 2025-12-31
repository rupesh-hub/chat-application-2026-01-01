package com.alfarays.configuration;

import com.alfarays.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if(accessor == null) {
            return message;
        }

        if(StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if(authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("WebSocket CONNECT without Authorization header");
                return message;
            }

            String token = authHeader.substring(7);

            try {
                String username = jwtTokenService.extractUsername(token);

                if(username != null && jwtTokenService.isTokenValid(token, username)) {

                    UserDetails userDetails =
                            userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    accessor.setUser(authentication); // 🔥 THIS IS THE KEY
                    log.info("✅ WebSocket authenticated user: {}", username);

                } else {
                    log.warn("Invalid JWT token for WebSocket");
                }

            } catch(Exception ex) {
                log.error("WebSocket authentication failed", ex);
            }
        }

        return message;
    }
}
