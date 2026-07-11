package com.jayeshshinde.walletpaymentplatform.entity;

import com.jayeshshinde.walletpaymentplatform.enums.LedgerEntryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class LedgerEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID transferId;
    @Enumerated(EnumType.STRING)
    private LedgerEntryType entryType;
    private UUID walletId;
    private Long amount;
    @CreationTimestamp
    private LocalDateTime createdAt;
    private String createdBy; //TODO: JPA Auditing

    public LedgerEntry(UUID transferId, LedgerEntryType entryType, Long amount, UUID walletId) {
        this.transferId = transferId;
        this.entryType = entryType;
        this.amount = amount;
        this.walletId = walletId;
    }

}
