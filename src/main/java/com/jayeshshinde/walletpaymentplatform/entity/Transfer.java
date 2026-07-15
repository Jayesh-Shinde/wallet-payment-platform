package com.jayeshshinde.walletpaymentplatform.entity;

import com.jayeshshinde.walletpaymentplatform.enums.TransferStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID fromWalletId;
    private UUID toWalletId;
    private Long amount;
    @Enumerated(EnumType.STRING)
    private TransferStatus status = TransferStatus.INITIATED;
    private String reason;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    private String createdBy; //TODO: JPA Auditing
    private String updatedBy;//TODO: JPA Auditing

    public Transfer(UUID fromWalletId, UUID toWalletId, Long amount) {
        this.fromWalletId = fromWalletId;
        this.toWalletId = toWalletId;
        this.amount = amount;
    }

    public void applyStatusReason(String reason) {
        this.reason = reason;
    }

    public void putOnHold() {
        if (status == TransferStatus.PENDING) {
            this.status = TransferStatus.ONHOLD;
        } else {
            throwIllegalStateException();
        }
    }

    public void initiateTransfer() {
        if (status == TransferStatus.INITIATED || status == TransferStatus.ONHOLD) {
            this.status = TransferStatus.PENDING;
        } else {
            throwIllegalStateException();
        }

    }

    private void throwIllegalStateException() {
        throw new IllegalStateException("Transfer is in incorrect state");
    }

    public void completeTransfer() {
        if (status == TransferStatus.PENDING) {
            this.status = TransferStatus.COMPLETED;
        } else {
            throwIllegalStateException();
        }
    }

    public void failedTransfer() {
        if (status != TransferStatus.COMPLETED) {
            this.status = TransferStatus.FAILED;
        } else {
            throwIllegalStateException();
        }
    }


}
