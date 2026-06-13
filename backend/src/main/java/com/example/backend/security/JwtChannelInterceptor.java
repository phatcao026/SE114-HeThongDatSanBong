package com.example.backend.security;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.utils.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public JwtChannelInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            log.info("STOMP CONNECT - Auth Header received: {}", authHeader != null ? "Yes" : "No");
            
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    Long userId = TokenUtils.getUserIdFromToken(token, jwtSecret);
                    log.info("STOMP CONNECT - Resolved userId: {}", userId);
                    if (userId != null) {
                        User user = userRepository.findById(userId).orElse(null);
                        if (user != null) {
                            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
                            UsernamePasswordAuthenticationToken authentication = 
                                    new UsernamePasswordAuthenticationToken(userId, null, List.of(authority));
                            accessor.setUser(authentication);
                            log.info("STOMP CONNECT - Successfully authenticated Principal name: {}", authentication.getName());
                        }
                    }
                } catch (Exception e) {
                    log.error("STOMP CONNECT - Error parsing STOMP JWT token: ", e);
                }
            } else {
                log.warn("STOMP CONNECT - No Bearer token found in headers");
            }
        }
        return message;
    }
}
