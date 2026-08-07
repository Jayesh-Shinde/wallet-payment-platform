package com.jayeshshinde.walletpaymentplatform.entity;

import com.jayeshshinde.walletpaymentplatform.enums.NotificationEventStatus;
import com.jayeshshinde.walletpaymentplatform.enums.NotificationEventType;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
public class NotificationEvent implements Persistable<UUID> {
    @Id
    private UUID id;
    @Enumerated(EnumType.STRING)
    private NotificationEventType eventType;
    @Enumerated(EnumType.STRING)
    private NotificationEventStatus status;
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode payload;
    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
    @Transient
    private boolean isNew = true;

    public NotificationEvent(UUID id, NotificationEventType eventType, NotificationEventStatus status, JsonNode payload) {
        this.id = id;
        this.eventType = eventType;
        this.status = status;
        this.payload = payload;
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false; // Prevents subsequent saves from breaking
    }
}
