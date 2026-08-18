package com.example.ex76.config;

import com.example.ex76.service.MissionSuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
  private static final Pattern SUGGESTION_DESTINATION =
      Pattern.compile("/(?:topic|app)/suggestions/(\\d+)(?:/chat)?");
  private final MissionSuggestionService suggestionService;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic");
    registry.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws-mission");
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new ChannelInterceptor() {
      @Override
      public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (accessor.getCommand() != StompCommand.SEND
            && accessor.getCommand() != StompCommand.SUBSCRIBE) return message;
        String destination = accessor.getDestination();
        if (destination == null) return message;
        Matcher matcher = SUGGESTION_DESTINATION.matcher(destination);
        if (!matcher.matches()) return message;
        Principal user = accessor.getUser();
        if (user == null || !suggestionService.canChat(
            Long.parseLong(matcher.group(1)), user.getName())) {
          throw new org.springframework.security.access.AccessDeniedException(
              "미션 채팅에 참여할 권한이 없습니다.");
        }
        return message;
      }
    });
  }
}
