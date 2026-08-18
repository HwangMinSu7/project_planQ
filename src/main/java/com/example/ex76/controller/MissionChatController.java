package com.example.ex76.controller;

import com.example.ex76.dto.MissionChatMessageDTO;
import com.example.ex76.dto.MissionChatRequest;
import com.example.ex76.service.MissionSuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class MissionChatController {
  private final MissionSuggestionService suggestionService;

  @MessageMapping("/suggestions/{id}/chat")
  @SendTo("/topic/suggestions/{id}")
  public MissionChatMessageDTO chat(@DestinationVariable Long id,
                                    MissionChatRequest request,
                                    Principal principal) {
    return suggestionService.addChatMessage(id, principal.getName(), request.content());
  }
}
