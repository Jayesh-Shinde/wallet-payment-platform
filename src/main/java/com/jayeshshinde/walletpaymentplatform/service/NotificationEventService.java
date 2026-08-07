package com.jayeshshinde.walletpaymentplatform.service;

import com.jayeshshinde.walletpaymentplatform.entity.NotificationEvent;
import com.jayeshshinde.walletpaymentplatform.exceptions.DuplicateNotificationEventException;
import com.jayeshshinde.walletpaymentplatform.repository.NotificationEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationEventService {
    private final NotificationEventRepository notificationEventRepository;

    public void processNotificationEvent(NotificationEvent notificationEvent) {
        try {
            notificationEventRepository.save(notificationEvent);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateNotificationEventException(e.getMessage());
        }

    }
}
