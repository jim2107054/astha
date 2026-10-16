package com.astha.notifications.repository;

import com.astha.notifications.domain.NotificationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationMessage, String> {
    List<NotificationMessage> findAllByOrderBySentAtDesc();
    List<NotificationMessage> findByRecipientOrderBySentAtDesc(String recipient);
}
