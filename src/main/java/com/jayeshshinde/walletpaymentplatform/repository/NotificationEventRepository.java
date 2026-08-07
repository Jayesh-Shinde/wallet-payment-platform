package com.jayeshshinde.walletpaymentplatform.repository;

import com.jayeshshinde.walletpaymentplatform.entity.NotificationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationEventRepository extends JpaRepository<NotificationEvent, UUID> {
}
