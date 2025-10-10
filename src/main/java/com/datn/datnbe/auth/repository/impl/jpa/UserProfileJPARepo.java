package com.datn.datnbe.auth.repository.impl.jpa;

import com.datn.datnbe.auth.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileJPARepo extends JpaRepository<UserProfile, String> {

}
