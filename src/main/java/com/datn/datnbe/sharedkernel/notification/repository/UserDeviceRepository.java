package com.datn.datnbe.sharedkernel.notification.repository;

import com.datn.datnbe.sharedkernel.notification.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, String> {
    Optional<UserDevice> findByUserIdAndFcmToken(String userId, String fcmToken);

    java.util.List<UserDevice> findAllByUserId(String userId);

    void deleteByFcmToken(String fcmToken);
}
