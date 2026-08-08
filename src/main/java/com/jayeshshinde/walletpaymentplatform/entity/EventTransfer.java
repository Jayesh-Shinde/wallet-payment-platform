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
import java.util.concurrent.ThreadLocalRandom;

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
    @CreationTimestamp
    private Instant notEligibleBefore;
    @Transient
    private boolean isNew = true;
    //Why @Transient is Unnecessary
    //JPA specifications state that all static and final fields are automatically ignored by the persistence provider.
    // static fields belong to the class, not to an individual database row instance.
    // final fields cannot be modified after object construction, which breaks JPA's hydration mechanism.
    private static final long BASE_DELAY_MS = 5000;   // 5s — must clear the 2s poll noise floor
    private static final int MULTIPLIER = 3;
    private static final long JITTER_MS = 1000;

    public EventTransfer(UUID id, EventTransferType eventType, UUID transferId, JsonNode payload) {
        this.id = id;
        this.eventType = eventType;
        this.transferId = transferId;
        this.payload = payload;
        this.status = EventTransferStatus.PENDING;
        this.attempts = 0;
        this.notEligibleBefore = Instant.now();
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
            this.notEligibleBefore = Instant.now()
                    .plusMillis((long) (BASE_DELAY_MS * Math.pow(MULTIPLIER, this.attempts)))
                    .plusMillis(ThreadLocalRandom.current().nextLong(0, JITTER_MS));
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
