package com.stackflov.repository;

import com.stackflov.domain.Notification;
import com.stackflov.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByReceiverOrderByCreatedAtDesc(User receiver, Pageable pageable);

    long countByReceiverAndIsReadFalse(User receiver);

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.receiver = :receiver")
    void deleteAllByReceiver(User receiver);

    void deleteByIdAndReceiver(Long id, User receiver);
}
