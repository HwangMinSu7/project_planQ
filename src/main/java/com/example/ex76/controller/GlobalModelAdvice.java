package com.example.ex76.controller;

import com.example.ex76.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {
  private final NotificationService notificationService;

  @ModelAttribute("notificationUnreadCount")
  public long notificationUnreadCount(Authentication authentication) {
    return authentication == null || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken
        ? 0 : notificationService.unreadCount(authentication.getName());
  }
}
