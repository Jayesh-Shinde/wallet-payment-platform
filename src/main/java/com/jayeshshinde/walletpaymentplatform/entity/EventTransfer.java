package com.jayeshshinde.walletpaymentplatform.entity;

import com.jayeshshinde.walletpaymentplatform.enums.EventTransferStatus;
import com.jayeshshinde.walletpaymentplatform.enums.EventTransferType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class EventTransfer implements Persistable<UUID> {
    @Id
    private UUID id;
    @Enumerated(EnumType.STRING)
    private EventTransferType eventType;
    private UUID transferId;
    @Enumerated(EnumType.STRING)
    private EventTransferStatus status;
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode payload;
    private int attempts;
    private String lastError;
    private Instant claimedExpiry;
    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
    @Transient
    private boolean isNew = true;

    public EventTransfer(UUID id, EventTransferType eventType, UUID transferId, JsonNode payload) {
        this.id = id;
        this.eventType = eventType;
        this.transferId = transferId;
        this.payload = payload;
        this.status = EventTransferStatus.PENDING;
        this.attempts = 0;
    }

    public void setLastError(String lastError) {
        if (this.status != EventTransferStatus.PROCESSED) {
            this.lastError = lastError;
        } else {
            throw new IllegalStateException("Cannot set last error as status is PROCESSED");
        }

    }

    public void markClaimed() {
        if (this.status == EventTransferStatus.PENDING) {
            this.status = EventTransferStatus.CLAIMED;
        } else {
            throw new IllegalStateException("Event transfer has already been processed or in incorrect state");
        }
    }

    public void markProcessed() {
        if (status == EventTransferStatus.PENDING || status == EventTransferStatus.DEAD_LETTER || status == EventTransferStatus.CLAIMED) {
            this.status = EventTransferStatus.PROCESSED;
        } else {
            throw new IllegalStateException("Event transfer has already been processed or in incorrect state");
        }

    }

    public void markFailed() {
        if (this.status == EventTransferStatus.PENDING || this.status == EventTransferStatus.DEAD_LETTER) {
            this.status = EventTransferStatus.FAILED;
        } else {
            throw new IllegalStateException("Event transfer status can not be marked failed as it is completed");
        }
    }

    public void markDeadLettered() {
        if (this.status == EventTransferStatus.PENDING) {
            this.status = EventTransferStatus.DEAD_LETTER;
        } else {
            throw new IllegalStateException("Event transfer status can not be marked as dead lettered because it is in incorrect state");
        }
    }

    public void incrementAttempts() {
        this.attempts++;
        if (this.attempts == 3) {
            this.status = EventTransferStatus.DEAD_LETTER;
        } else {
            this.status = EventTransferStatus.PENDING;
        }
        this.claimedExpiry = null;
    }

    public void clearClaimedExpiry() {
        this.claimedExpiry = null;
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
