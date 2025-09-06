package com.stackflov.service;

import com.stackflov.domain.Notification;
import com.stackflov.domain.NotificationType;
import com.stackflov.domain.User;
import com.stackflov.dto.NotificationDto;
import com.stackflov.repository.NotificationRepository;
import com.stackflov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessageSendingOperations messagingTemplate;

    @Transactional
    public void notify(User receiver, NotificationType type, String message, String link) {
        Notification n = Notification.builder()
                .receiver(receiver)
                .type(type)
                .message(message)
                .link(link)
                .build();
        Notification saved = notificationRepository.save(n);

        // 웹소켓 푸시: /sub/notifications/{userId}
        messagingTemplate.convertAndSend("/sub/notifications/" + receiver.getId(), new NotificationDto(saved));
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getMyNotifications(String email, Pageable pageable) {
        User me = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 비활성화된 사용자입니다."));
        return notificationRepository.findByReceiverOrderByCreatedAtDesc(me, pageable)
                .map(NotificationDto::new);
    }

    @Transactional
    public void markRead(String email, Long notificationId) {
        User me = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 비활성화된 사용자입니다."));

        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));
        if (!n.getReceiver().getId().equals(me.getId())) {
            throw new IllegalArgumentException("본인 알림만 읽음 처리할 수 있습니다.");
        }
        n.markRead();
    }

    @Transactional
    public void markAllRead(String email) {
        // 간단 구현: 페이지 없이 전부 불러와서 처리 (규모 커지면 벌크 업데이트로 개선)
        User me = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 비활성화된 사용자입니다."));
        notificationRepository.findByReceiverOrderByCreatedAtDesc(me, Pageable.unpaged())
                .forEach(n -> { if (!n.isRead()) n.markRead(); });
    }
}
