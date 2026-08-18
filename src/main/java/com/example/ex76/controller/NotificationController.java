package com.example.ex76.controller;

import com.example.ex76.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
  private final NotificationService notificationService;

  @GetMapping
  public String list(Authentication auth, Model model) {
    model.addAttribute("notifications", notificationService.getNotifications(auth.getName()));
    return "notification/list";
  }

  @PostMapping("/{id}/read")
  public String read(@PathVariable Long id, Authentication auth) {
    return "redirect:" + notificationService.read(id, auth.getName());
  }

  @PostMapping("/read-all")
  public String readAll(Authentication auth) {
    notificationService.readAll(auth.getName());
    return "redirect:/notifications";
  }
}
