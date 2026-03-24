package com.cognizant.healthcaregov.service;

import com.cognizant.healthcaregov.dao.NotificationRepository;
import com.cognizant.healthcaregov.dao.UserRepository;
import com.cognizant.healthcaregov.dto.NotificationResponse;
import com.cognizant.healthcaregov.entity.Notification;
import com.cognizant.healthcaregov.entity.User;
import com.cognizant.healthcaregov.exception.BadRequestException;
import com.cognizant.healthcaregov.exception.ResourceNotFoundException;
import com.cognizant.healthcaregov.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository         userRepository;
    private final SecurityUtils          securityUtils;


    @Transactional
    public void send(Integer userId, Integer entityId, String message, String category) {
        userRepository.findById(userId).ifPresent(user -> {
            Notification n = new Notification();
            n.setUser(user);
            n.setEntityId(entityId);
            n.setMessage(message);
            n.setCategory(category);
            n.setStatus("Unread");
            notificationRepository.save(n);
            log.info("Notification sent userId={} category={}", userId, category);
        });
    }


    @Transactional(readOnly = true)
    public List<NotificationResponse> getForUser(Integer userId) {
        log.info("Fetching notifications userId={}", userId);

        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));

        // ADMIN may read any user's notifications
        if (!securityUtils.isAdmin()) {
            String callerEmail = securityUtils.getCurrentUserEmail().orElse(null);
            if (!target.getEmail().equalsIgnoreCase(callerEmail)) {
                log.warn("Access denied: {} tried to read notifications of userId={}",
                        callerEmail, userId);
                throw new BadRequestException(
                        "Access denied: you may only view your own notifications.");
            }
        }

        return notificationRepository.findByUserUserID(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getNotificationID(),
                n.getUser().getUserID(),
                n.getEntityId(),
                n.getMessage(),
                n.getCategory(),
                n.getStatus(),
                n.getCreatedDate());
    }
}
