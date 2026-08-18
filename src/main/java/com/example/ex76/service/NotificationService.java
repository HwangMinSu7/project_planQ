package com.example.ex76.service;

import com.example.ex76.entity.ClubMember;
import com.example.ex76.entity.UserNotification;
import com.example.ex76.repository.UserNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {
  private final UserNotificationRepository notificationRepository;

  public void send(ClubMember recipient, String message, String linkUrl) {
    notificationRepository.save(UserNotification.builder()
        .recipient(recipient).message(message).linkUrl(linkUrl).build());
  }

  @Transactional(readOnly = true)
  public long unreadCount(String email) {
    return notificationRepository.countByRecipient_EmailAndReadFalse(email);
  }

  @Transactional(readOnly = true)
  public List<UserNotification> getNotifications(String email) {
    return notificationRepository.findTop30ByRecipient_EmailOrderByIdDesc(email);
  }

  public String read(Long id, String email) {
    UserNotification notification = notificationRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));
    if (!notification.getRecipient().getEmail().equals(email)) {
      throw new IllegalStateException("본인의 알림만 확인할 수 있습니다.");
    }
    notification.markRead();
    return notification.getLinkUrl();
  }

  public void readAll(String email) {
    getNotifications(email).forEach(UserNotification::markRead);
  }
}
