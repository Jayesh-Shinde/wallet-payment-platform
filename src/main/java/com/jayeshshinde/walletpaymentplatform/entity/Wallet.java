package com.jayeshshinde.walletpaymentplatform.entity;

import com.jayeshshinde.walletpaymentplatform.enums.WalletStatus;
import com.jayeshshinde.walletpaymentplatform.enums.WalletType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "user_id")
    private UUID userId;
    @Enumerated(EnumType.STRING)
    private WalletStatus status;
    private UUID lastReconciledLedgerEntryId;
    private Long balance = 0L;
    @Enumerated(EnumType.STRING)
    private WalletType walletType;
    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    public Wallet(UUID userId) {
        this.userId = userId;
        this.walletType = WalletType.USER;
        pendingVerification();
    }

    public void applyReconciledBalance(long newBalance) {
        this.balance = newBalance;
    }

    public void deactivate() {
        this.status = WalletStatus.DEACTIVATED;
    }

    public void pendingVerification() {
        this.status = WalletStatus.PENDING_VERIFICATION;
    }

    public void activate() {
        this.status = WalletStatus.ACTIVE;
    }
}
