package com.datn.datnbe.sharedkernel.notification.repository;

import com.datn.datnbe.sharedkernel.notification.entity.AppNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AppNotificationRepository extends JpaRepository<AppNotification, String> {

    Page<AppNotification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    @Query("SELECT COUNT(n) FROM AppNotification n WHERE n.userId = :userId AND n.isRead = false")
    long countUnreadByUserId(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("UPDATE AppNotification n SET n.isRead = true WHERE n.id = :id AND n.userId = :userId")
    int markAsRead(@Param("id") String id, @Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("UPDATE AppNotification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") String userId);
}
