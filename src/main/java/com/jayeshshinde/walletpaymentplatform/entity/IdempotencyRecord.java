package com.jayeshshinde.walletpaymentplatform.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Persistable;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class IdempotencyRecord implements Persistable<UUID> {
    @Id
    private UUID idempotencyKey;
    @JdbcTypeCode(SqlTypes.JSON)
    @Setter
    private JsonNode responseData;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public IdempotencyRecord(UUID idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    @Override
    public @Nullable UUID getId() {
        return idempotencyKey;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
