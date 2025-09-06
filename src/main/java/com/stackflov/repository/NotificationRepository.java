package com.stackflov.repository;

import com.stackflov.domain.Notification;
import com.stackflov.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByReceiverOrderByCreatedAtDesc(User receiver, Pageable pageable);

    long countByReceiverAndReadFalse(User receiver);
}
