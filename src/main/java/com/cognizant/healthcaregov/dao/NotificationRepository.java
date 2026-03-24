package com.cognizant.healthcaregov.dao;

import com.cognizant.healthcaregov.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserUserID(Integer userId);
}
