package com.ssafynity.demo.service;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.Notification;
import com.ssafynity.demo.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @Transactional
    public void send(Member receiver, String message, String link) {
        Notification noti = Notification.builder()
                .receiver(receiver)
                .message(message)
                .link(link)
                .build();
        notificationRepository.save(noti);
    }

    public List<Notification> findByReceiver(Member receiver) {
        return notificationRepository.findByReceiverOrderByCreatedAtDesc(receiver);
    }

    public int countUnread(Member receiver) {
        return notificationRepository.countByReceiverAndIsReadFalse(receiver);
    }

    @Transactional
    public void markAllRead(Member receiver) {
        List<Notification> list = notificationRepository.findByReceiverOrderByCreatedAtDesc(receiver);
        list.forEach(n -> n.setRead(true));
    }

    @Transactional
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}
